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
import android.util.Log;

import com.google.common.io.CharStreams;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.conn.ConnectTimeoutException;

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

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.X509TrustManager;

import de.duenndns.ssl.MemorizingTrustManager;
import li.klass.fhem.AndFHEMApplication;
import li.klass.fhem.error.ErrorHolder;
import li.klass.fhem.util.CloseableUtil;

import static com.google.common.base.Objects.firstNonNull;

public class FHEMWEBConnection extends FHEMConnection {

    public static final int SOCKET_TIMEOUT = 20000;
    public static final String TAG = FHEMWEBConnection.class.getName();
    public static final FHEMWEBConnection INSTANCE = new FHEMWEBConnection();

    public FHEMWEBConnection() {

    }

    @Override
    public RequestResult<String> executeCommand(String command, Context context) {
        Log.i(TAG, "executeTask command " + command);

        String urlSuffix = null;
        try {
            urlSuffix = "?XHR=1&cmd=" + URLEncoder.encode(command, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "unsupported encoding", e);
        }


        InputStreamReader reader = null;
        try {
            RequestResult<InputStream> response = executeRequest(urlSuffix, command);
            if (response.error != null) {
                return new RequestResult<>(response.error);
            }

            reader = new InputStreamReader(response.content);
            String content = CharStreams.toString(reader);
            if (content.contains("<title>") || content.contains("<div id=")) {
                Log.e(TAG, "found strange content: " + content);
                ErrorHolder.setError("found strange content in URL " + urlSuffix + ": \r\n\r\n" + content);
                return new RequestResult<>(RequestResultError.INVALID_CONTENT);
            }

            return new RequestResult<>(content);
        } catch (Exception e) {
            Log.e(TAG, "cannot handle result", e);
            return new RequestResult<>(RequestResultError.INTERNAL_ERROR);
        } finally {
            CloseableUtil.close(reader);
        }
    }

    private RequestResult<InputStream> executeRequest(String urlSuffix,
                                                      String command) {
        String url = null;
        try {
            url = serverSpec.getUrl() + urlSuffix;
            URL requestUrl = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) requestUrl.openConnection();
            connection.setConnectTimeout(SOCKET_TIMEOUT);
            connection.setReadTimeout(SOCKET_TIMEOUT);
            String authString = (serverSpec.getUsername() + ":" + serverSpec.getPassword());
            connection.addRequestProperty("Authorization", "Basic " +
                    Base64.encodeToString(authString.getBytes(), Base64.NO_WRAP));
            Log.i(TAG, "accessing URL " + url);
            int statusCode = connection.getResponseCode();
            Log.d(TAG, "response status code is " + statusCode);
            RequestResult<InputStream> errorResult = handleHttpStatusCode(statusCode);
            if (errorResult != null) {
                String msg = "found error " + errorResult.error.getDeclaringClass().getSimpleName() + " for " +
                        "status code " + statusCode;
                Log.d(TAG, msg);
                ErrorHolder.setError(null, msg);
                return errorResult;
            }
            InputStream contentStream = new BufferedInputStream(connection.getInputStream());
            return new RequestResult<>(contentStream);
        } catch (ConnectTimeoutException e) {
            Log.i(TAG, "connection timed out", e);
            setErrorInErrorHolderFor(e, url, command);
            return new RequestResult<>(RequestResultError.CONNECTION_TIMEOUT);
        } catch (ClientProtocolException e) {
            String errorText = "cannot connect, invalid URL? (" + url + ")";
            setErrorInErrorHolderFor(e, url, command);
            ErrorHolder.setError(e, errorText);
            return new RequestResult<>(RequestResultError.HOST_CONNECTION_ERROR);
        } catch (IOException e) {
            Log.i(TAG, "cannot connect to host", e);
            setErrorInErrorHolderFor(e, url, command);
            return new RequestResult<>(RequestResultError.HOST_CONNECTION_ERROR);
        }
    }

    public String getPassword() {
        return serverSpec.getPassword();
    }

    static RequestResult<InputStream> handleHttpStatusCode(int statusCode) {
        RequestResultError error;
        switch (statusCode) {
            case 400:
                error = RequestResultError.BAD_REQUEST;
                break;
            case 401:
                error = RequestResultError.AUTHENTICATION_ERROR;
                break;
            case 403:
                error = RequestResultError.AUTHENTICATION_ERROR;
                break;
            case 404:
                error = RequestResultError.NOT_FOUND;
                break;
            case 500:
                error = RequestResultError.INTERNAL_SERVER_ERROR;
                break;
            default:
                return null;

        }
        Log.i(TAG, "handleHttpStatusCode() : encountered http status code " + statusCode);
        return new RequestResult<>(error);
    }

    @Override
    public RequestResult<Bitmap> requestBitmap(String relativePath) {
        RequestResult<InputStream> response = executeRequest(relativePath, "request bitmap");
        if (response.error != null) {
            return new RequestResult<>(response.error);
        }
        try {
            Bitmap bitmap = BitmapFactory.decodeStream(response.content);
            return new RequestResult<>(bitmap);
        } finally {
            CloseableUtil.close(response.content);
        }
    }

    @Override
    protected void onSetServerSpec() {
        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            MemorizingTrustManager mtm = new MemorizingTrustManager(AndFHEMApplication.getContext());
            KeyManager[] clientKeys = null;
            if (serverSpec.getClientCertificatePath() != null) {
                File clientCertificate = new File(serverSpec.getClientCertificatePath());
                String clientCertificatePassword = serverSpec.getClientCertificatePassword();
                if (clientCertificate.exists()) {
                    final KeyStore keyStore = loadPKCS12KeyStore(clientCertificate, clientCertificatePassword);
                    KeyManagerFactory kmf = KeyManagerFactory.getInstance("X509");
                    kmf.init(keyStore, firstNonNull(clientCertificatePassword, "").toCharArray());
                    clientKeys = kmf.getKeyManagers();
                }
            }
            sc.init(clientKeys, new X509TrustManager[]{mtm}, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier(
                    mtm.wrapHostnameVerifier(HttpsURLConnection.getDefaultHostnameVerifier()));
        } catch (Exception e) {
            Log.e(TAG, "Error initializing HttpsUrlConnection", e);
        }
    }

    private KeyStore loadPKCS12KeyStore(File certificateFile, String clientCertPassword) throws Exception {
        KeyStore keyStore = null;
        FileInputStream fis = null;
        try {
            keyStore = KeyStore.getInstance("PKCS12");
            fis = new FileInputStream(certificateFile);
            keyStore.load(fis, clientCertPassword.toCharArray());
        } finally {
            CloseableUtil.close(fis);
        }
        return keyStore;
    }
}
