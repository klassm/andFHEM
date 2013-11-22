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

import org.apache.commons.io.IOUtils;
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

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.security.KeyStore;
import java.util.zip.GZIPInputStream;

public class FHEMWEBConnection extends FHEMConnection {

    public static final int CONNECTION_TIMEOUT = 3000;
    public static final int SOCKET_TIMEOUT = 20000;
    public static final String TAG = FHEMWEBConnection.class.getName();
    private DefaultHttpClient client = null;

    public static final String FHEMWEB_URL = "FHEMWEB_URL";
    public static final String FHEMWEB_USERNAME = "FHEMWEB_USERNAME";
    public static final String FHEMWEB_PASSWORD = "FHEMWEB_PASSWORD";

    public static final FHEMWEBConnection INSTANCE = new FHEMWEBConnection();

    @Override
    public RequestResult<String> executeCommand(String command) {
        String urlSuffix = null;
        try {
            urlSuffix = "?XHR=1&cmd=" + URLEncoder.encode(command, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "unsupported encoding", e);
        }

        RequestResult<InputStream> response = executeRequest(urlSuffix, client);
        if (response.error != null) {
            return new RequestResult<String>(response.error);
        }

        try {
            String content = IOUtils.toString(response.content);
            if (content.contains("<title>") || content.contains("<div id=")) {
                Log.e(TAG, "found strange content: " + content);
                return new RequestResult<String>(RequestResultError.INVALID_CONTENT);
            }

            return new RequestResult<String>(content);
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public RequestResult<Bitmap> requestBitmap(String relativePath) {
        RequestResult<InputStream> response = executeRequest(relativePath, client);
        if (response.error != null) {
            return new RequestResult<Bitmap>(response.error);
        }
        Bitmap bitmap = BitmapFactory.decodeStream(response.content);
        return new RequestResult<Bitmap>(bitmap);
    }

    private RequestResult<InputStream> executeRequest(String urlSuffix,
                                                      DefaultHttpClient client) {
        if (client == null) {
            client = createNewHTTPClient(CONNECTION_TIMEOUT, SOCKET_TIMEOUT);
        }
        try {
            HttpGet request = new HttpGet();
            request.addHeader("Accept-Encoding", "gzip");

            String url = serverSpec.getUrl() + urlSuffix;

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
                Log.d(TAG, "found error " + errorResult.error.getClass().getSimpleName() + " for " +
                        "status code " + statusCode);
                return errorResult;
            }

            InputStream contentStream = response.getEntity().getContent();
            Header contentEncoding = response.getFirstHeader("Content-Encoding");
            if (contentEncoding != null && contentEncoding.getValue().equalsIgnoreCase("gzip")) {
                contentStream = new GZIPInputStream(contentStream);
            }
            return new RequestResult<InputStream>(contentStream);
        } catch (ConnectTimeoutException e) {
            return new RequestResult<InputStream>(RequestResultError.CONNECTION_TIMEOUT);
        } catch (ClientProtocolException e) {
            return new RequestResult<InputStream>(RequestResultError.HOST_CONNECTION_ERROR);
        } catch (IOException e) {
            return new RequestResult<InputStream>(RequestResultError.HOST_CONNECTION_ERROR);
        } catch (URISyntaxException e) {
            throw new IllegalStateException("cannot parse URL " + urlSuffix, e);
        }
    }

    public String getPassword() {
        return serverSpec.getPassword();
    }

    private DefaultHttpClient createNewHTTPClient(int connectionTimeout,
                                                  int socketTimeout) {
        try {
            KeyStore trustStore;
            SSLSocketFactory socketFactory;

            trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(null, null);


            socketFactory = new CustomSSLSocketFactory(trustStore);
            socketFactory.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);


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
            return new DefaultHttpClient();
        }
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
        return new RequestResult<InputStream>(error);
    }
}
