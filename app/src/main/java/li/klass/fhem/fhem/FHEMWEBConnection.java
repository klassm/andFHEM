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

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.google.common.io.CharStreams;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.security.KeyStore;
import java.util.zip.GZIPInputStream;

import li.klass.fhem.error.ErrorHolder;
import li.klass.fhem.util.CloseableUtil;

public class FHEMWEBConnection extends FHEMConnection {

    public static final int SOCKET_TIMEOUT = 20000;
    public static final String TAG = FHEMWEBConnection.class.getName();
    public static final FHEMWEBConnection INSTANCE = new FHEMWEBConnection();
    private DefaultHttpClient client = null;

    @Override
    public RequestResult<String> executeCommand(String command) {
        Log.i(TAG, "executeTask command " + command);

        String urlSuffix = null;
        try {
            urlSuffix = "?XHR=1&cmd=" + URLEncoder.encode(command, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "unsupported encoding", e);
        }


        InputStreamReader reader = null;
        try {
            RequestResult<InputStream> response = executeRequest(urlSuffix, client, command);
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
                                                      DefaultHttpClient client, String command) {
        String url = null;
        if (client == null) {
            client = createNewHTTPClient(getConnectionTimeoutMilliSeconds(), SOCKET_TIMEOUT);
        }
        InputStream contentStream;
        try {
            HttpGet request = new HttpGet();
            request.addHeader("Accept-Encoding", "gzip");

            url = serverSpec.getUrl() + urlSuffix;

            Log.i(TAG, "accessing URL " + url);
            URI uri = new URI(url);

            client.getCredentialsProvider().setCredentials(
                    new AuthScope(uri.getHost(), uri.getPort()),
                    new UsernamePasswordCredentials(serverSpec.getUsername(),
                            getPassword()));

            request.setURI(uri);

            HttpResponse response = client.execute(request);
            int statusCode = response.getStatusLine().getStatusCode();
            Log.d(TAG, "response status code is " + statusCode);

            RequestResult<InputStream> errorResult = handleHttpStatusCode(statusCode);
            if (errorResult != null) {
                String msg = "found error " + errorResult.error.getDeclaringClass().getSimpleName() + " for " +
                        "status code " + statusCode;
                Log.d(TAG, msg);
                ErrorHolder.setError(null, msg);
                return errorResult;
            }

            contentStream = response.getEntity().getContent();
            Header contentEncoding = response.getFirstHeader("Content-Encoding");
            if (contentEncoding != null && contentEncoding.getValue().equalsIgnoreCase("gzip")) {
                contentStream = new GZIPInputStream(contentStream);
            }
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
        } catch (URISyntaxException e) {
            Log.i(TAG, "invalid URL syntax", e);
            setErrorInErrorHolderFor(e, url, command);
            throw new IllegalStateException("cannot parse URL " + urlSuffix, e);
        }
    }

    private DefaultHttpClient createNewHTTPClient(int connectionTimeout, int socketTimeout) {
        try {
            SSLSocketFactory socketFactory;

            if (serverSpec.isClientCertificateEnabled()) {
                socketFactory = createClientCertSocketFactory();
            } else {
                socketFactory = createDefaultSocketFactory();
            }

            HttpParams params = new BasicHttpParams();
            HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
            HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);
            HttpConnectionParams.setConnectionTimeout(params, connectionTimeout);
            HttpConnectionParams.setSoTimeout(params, socketTimeout);

            SchemeRegistry registry = new SchemeRegistry();
            registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
            registry.register(new Scheme("https", socketFactory, 443));

            ClientConnectionManager ccm = new ThreadSafeClientConnManager(params, registry);

            return new DefaultHttpClient(ccm, params);
        } catch (Exception e) {
            Log.e(TAG, "error while creating client", e);
            return new DefaultHttpClient();
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

    private SSLSocketFactory createClientCertSocketFactory() throws Exception {
        return new ClientCertSSLSocketFactory(null,
                new File(serverSpec.getClientCertificatePath()),
                serverSpec.getClientCertificatePassword(),
                new File(serverSpec.getServerCertificatePath())
        );
    }

    private SSLSocketFactory createDefaultSocketFactory() throws Exception {
        KeyStore trustStore;
        SSLSocketFactory socketFactory;
        trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
        trustStore.load(null, null);

        socketFactory = new TrustAllSSLSocketFactory(trustStore);
        return socketFactory;
    }

    @Override
    public RequestResult<Bitmap> requestBitmap(String relativePath) {
        RequestResult<InputStream> response = executeRequest(relativePath, client, "request bitmap");
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
        // Reset the client, so that it will be recreated upon the next request. This
        // enables us to change between client cert and not client cert support.
        client = null;
    }
}
