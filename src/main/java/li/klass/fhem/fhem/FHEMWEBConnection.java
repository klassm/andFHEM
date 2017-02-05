/*
 * AndFHEM - Open Source Android application to control a FHEM home automation
 * server.
 *
 * Copyright (c) 2011, Matthias Klass or third-party contributors as
 * indicated by the @author tags or express copyright attribution
 * statements applied by the authors.  All third-party contributions are
 * distributed under license by Red Hat Inc.
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU GENERAL PUBLIC LICENSE, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU GENERAL PUBLIC LICENSE
 * for more details.
 *
 * You should have received a copy of the GNU GENERAL PUBLIC LICENSE
 * along with this distribution; if not, write to:
 *   Free Software Foundation, Inc.
 *   51 Franklin Street, Fifth Floor
 *   Boston, MA  02110-1301  USA
 */

package li.klass.fhem.fhem;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.CharStreams;

import org.apache.http.conn.ConnectTimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.KeyStore;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.X509TrustManager;

import de.duenndns.ssl.MemorizingTrustManager;
import li.klass.fhem.AndFHEMApplication;
import li.klass.fhem.error.ErrorHolder;
import li.klass.fhem.fhem.connection.FHEMServerSpec;
import li.klass.fhem.util.ApplicationProperties;

import static com.google.common.base.MoreObjects.firstNonNull;
import static li.klass.fhem.fhem.RequestResultError.AUTHENTICATION_ERROR;
import static li.klass.fhem.fhem.RequestResultError.BAD_REQUEST;
import static li.klass.fhem.fhem.RequestResultError.HOST_CONNECTION_ERROR;
import static li.klass.fhem.fhem.RequestResultError.INTERNAL_ERROR;
import static li.klass.fhem.fhem.RequestResultError.INTERNAL_SERVER_ERROR;
import static li.klass.fhem.fhem.RequestResultError.INVALID_CONTENT;
import static li.klass.fhem.fhem.RequestResultError.NOT_FOUND;
import static li.klass.fhem.util.CloseableUtil.close;

public class FHEMWEBConnection extends FHEMConnection {

    public static final int SOCKET_TIMEOUT = 20000;
    private static final Logger LOG = LoggerFactory.getLogger(FHEMWEBConnection.class);
    public static final Map<Integer, RequestResultError> STATUS_CODE_MAP = ImmutableMap.<Integer, RequestResultError>builder()
            .put(400, BAD_REQUEST)
            .put(401, AUTHENTICATION_ERROR)
            .put(403, AUTHENTICATION_ERROR)
            .put(404, NOT_FOUND)
            .put(500, INTERNAL_SERVER_ERROR)
            .build();

    static {
        HttpsURLConnection.setDefaultSSLSocketFactory(new NoSSLv3Factory());
    }

    public FHEMWEBConnection(FHEMServerSpec fhemServerSpec, ApplicationProperties applicationProperties) {
        super(fhemServerSpec, applicationProperties);
    }

    @Override
    public RequestResult<String> executeCommand(String command, Context context) {
        LOG.info("executeTask command " + command);

        String urlSuffix = generateUrlSuffix(command);

        InputStreamReader reader = null;
        try {
            RequestResult<InputStream> response = executeRequest(urlSuffix);
            if (response.error != null) {
                return new RequestResult<>(response.error);
            }

            reader = new InputStreamReader(response.content, Charsets.UTF_8);
            String content = CharStreams.toString(reader);
            if (content.contains("<title>") || content.contains("<div id=")) {
                LOG.error("found strange content: " + content);
                ErrorHolder.setError("found strange content in URL " + urlSuffix + ": \r\n\r\n" + content);
                return new RequestResult<>(INVALID_CONTENT);
            }

            return new RequestResult<>(content);
        } catch (Exception e) {
            LOG.error("cannot handle result", e);
            return new RequestResult<>(INTERNAL_ERROR);
        } finally {
            close(reader);
        }
    }

    protected String generateUrlSuffix(String command) {
        String urlSuffix = null;
        try {
            urlSuffix = "?XHR=1&cmd=" + URLEncoder.encode(command, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            LOG.error("unsupported encoding", e);
        }
        return urlSuffix;
    }

    public RequestResult<InputStream> executeRequest(String urlSuffix) {
        return executeRequest(serverSpec.getUrl(), urlSuffix, false);
    }

    private RequestResult<InputStream> executeRequest(String serverUrl, String urlSuffix, boolean isRetry) {
        initSslContext();
        String url = null;
        HttpURLConnection connection;
        try {
            url = serverUrl + urlSuffix;
            URL requestUrl = new URL(url);

            LOG.info("accessing URL {}", url);
            connection = (HttpURLConnection) requestUrl.openConnection();
            connection.setConnectTimeout(SOCKET_TIMEOUT);
            connection.setReadTimeout(SOCKET_TIMEOUT);
            connection.setDoOutput(false);

            String authString = (serverSpec.getUsername() + ":" + serverSpec.getPassword());
            connection.addRequestProperty("Authorization", "Basic " +
                    Base64.encodeToString(authString.getBytes(), Base64.NO_WRAP));
            int statusCode = connection.getResponseCode();
            LOG.debug("response status code is " + statusCode);

            RequestResult<InputStream> errorResult = handleHttpStatusCode(statusCode);
            if (errorResult != null) {
                String msg = "found error " + errorResult.error.getDeclaringClass().getSimpleName() + " for " +
                        "status code " + statusCode;
                LOG.debug(msg);
                ErrorHolder.setError(null, msg);

                close(connection);

                return errorResult;
            }

            return new RequestResult<>((InputStream) new BufferedInputStream(connection.getInputStream()));
        } catch (ConnectTimeoutException e) {
            LOG.info("connection timed out", e);
            return handleError(urlSuffix, isRetry, url, e);
        } catch (IOException e) {
            LOG.info("cannot connect to host", e);
            return handleError(urlSuffix, isRetry, url, e);
        }
    }

    private RequestResult<InputStream> handleError(String urlSuffix, boolean isRetry, String url, Exception e) {
        setErrorInErrorHolderFor(e, url, urlSuffix);
        return handleRetryIfRequired(isRetry, new RequestResult<InputStream>(HOST_CONNECTION_ERROR), urlSuffix);
    }

    private RequestResult<InputStream> handleRetryIfRequired(boolean isCurrentRequestRetry, RequestResult<InputStream> previousResult,
                                                             String urlSuffix) {
        if (!serverSpec.canRetry() || isCurrentRequestRetry) {
            return previousResult;
        }
        return retryRequest(urlSuffix);
    }

    private RequestResult<InputStream> retryRequest(String urlSuffix) {
        LOG.info("retrying request for alternate URL");
        return executeRequest(serverSpec.getAlternateUrl(), urlSuffix, true);
    }

    public String getPassword() {
        return serverSpec.getPassword();
    }

    static RequestResult<InputStream> handleHttpStatusCode(int statusCode) {

        RequestResultError error = STATUS_CODE_MAP.get(statusCode);
        if (error == null) return null;

        LOG.info("handleHttpStatusCode() : encountered http status code {}", statusCode);
        return new RequestResult<>(error);
    }

    @Override
    public RequestResult<Bitmap> requestBitmap(String relativePath) {
        RequestResult<InputStream> response = executeRequest(relativePath);
        if (response.error != null) {
            return new RequestResult<>(response.error);
        }
        try {
            return new RequestResult<>(BitmapFactory.decodeStream(response.content));
        } finally {
            close(response.content);
        }
    }

    protected void initSslContext() {
        try {
            SSLContext sslContext = SSLContext.getInstance("TLS");
            MemorizingTrustManager memorizingTrustManager = new MemorizingTrustManager(AndFHEMApplication.getContext());
            KeyManager[] clientKeys = null;
            if (serverSpec.getClientCertificatePath() != null) {
                File clientCertificate = new File(serverSpec.getClientCertificatePath());
                String clientCertificatePassword = serverSpec.getClientCertificatePassword();
                if (clientCertificate.exists()) {
                    final KeyStore keyStore = loadPKCS12KeyStore(clientCertificate, clientCertificatePassword);
                    KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("X509");
                    keyManagerFactory.init(keyStore, firstNonNull(clientCertificatePassword, "").toCharArray());
                    clientKeys = keyManagerFactory.getKeyManagers();
                }
            }
            sslContext.init(clientKeys, new X509TrustManager[]{memorizingTrustManager}, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier(
                    memorizingTrustManager.wrapHostnameVerifier(HttpsURLConnection.getDefaultHostnameVerifier()));
        } catch (Exception e) {
            LOG.error("Error initializing HttpsUrlConnection", e);
        }
    }

    private KeyStore loadPKCS12KeyStore(File certificateFile, String clientCertPassword) throws Exception {
        KeyStore keyStore = null;
        FileInputStream fileInputStream = null;
        try {
            keyStore = KeyStore.getInstance("PKCS12");
            fileInputStream = new FileInputStream(certificateFile);
            keyStore.load(fileInputStream, clientCertPassword.toCharArray());
        } finally {
            close(fileInputStream);
        }
        return keyStore;
    }
}
