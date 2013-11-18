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
import java.net.URLEncoder;
import java.security.KeyStore;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.zip.GZIPInputStream;

import li.klass.fhem.exception.AndFHEMException;
import li.klass.fhem.exception.AuthenticationException;
import li.klass.fhem.exception.FHEMStrangeContentException;
import li.klass.fhem.exception.HostConnectionException;
import li.klass.fhem.exception.TimeoutException;

public class FHEMWEBConnection extends FHEMConnection {

    public static final int CONNECTION_TIMEOUT = 3000;
    public static final int SOCKET_TIMEOUT = 20000;
    public static final String TAG = FHEMWEBConnection.class.getName();
    private DefaultHttpClient client;

    public static final String FHEMWEB_URL = "FHEMWEB_URL";
    public static final String FHEMWEB_USERNAME = "FHEMWEB_USERNAME";
    public static final String FHEMWEB_PASSWORD = "FHEMWEB_PASSWORD";

    public static final FHEMWEBConnection INSTANCE = new FHEMWEBConnection();

    @Override
    public String xmllist() {
        return requestCommandResponse("xmllist");
    }

    @Override
    public String fileLogData(String logName, Date fromDate, Date toDate,
                              String columnSpec) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH:mm");
        String command = new StringBuilder().append("get ").append(logName)
                .append(" - - ").append(dateFormat.format(fromDate))
                .append(" ").append(dateFormat.format(toDate)).append(" ")
                .append(columnSpec).toString();

        return requestCommandResponse(command).replaceAll("#" + columnSpec, "");
    }

    @Override
    public String executeCommand(String command) {
        return requestCommandResponse(command);
    }

    @Override
    public Bitmap requestBitmap(String relativePath) {
        InputStream response = executeRequest(relativePath, client);
        return BitmapFactory.decodeStream(response);
    }

    private String requestCommandResponse(String command) {
        String urlSuffix = null;
        try {
            urlSuffix = "?XHR=1&cmd=" + URLEncoder.encode(command, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "unsupported encoding", e);
        }

        InputStream response = executeRequest(urlSuffix, client);
        try {
            String content = IOUtils.toString(response);
            if (content.contains("<title>") || content.contains("<div id=")) {
                Log.e(TAG, "found strange content: " + content);
                throw new FHEMStrangeContentException();
            }
            return content;
        } catch (IOException e) {
            throw new HostConnectionException(e);
        }
    }

    private InputStream executeRequest(String urlSuffix,
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

            if (statusCode == 401) {
                Log.d(TAG, "cannot authenticate (401 access denied)");
                throw new AuthenticationException(response.getStatusLine()
                        .toString());
            }

            InputStream contentStream = response.getEntity().getContent();
            Header contentEncoding = response.getFirstHeader("Content-Encoding");
            if (contentEncoding != null && contentEncoding.getValue().equalsIgnoreCase("gzip")) {
                return new GZIPInputStream(contentStream);
            }
            return contentStream;
        } catch (AndFHEMException e) {
            throw e;
        } catch (ConnectTimeoutException e) {
            throw new TimeoutException(e);
        } catch (Exception e) {
            throw new HostConnectionException(e);
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
}
