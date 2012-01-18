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

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import li.klass.fhem.AndFHEMApplication;
import li.klass.fhem.exception.HostConnectionException;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import java.net.URI;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FHEMWebConnection implements FHEMConnection {

    private DefaultHttpClient client;

    public static final String FHEMWEB_URL = "FHEMWEB_URL";
    public static final String FHEMWEB_USERNAME = "FHEMWEB_USERNAME";
    public static final String FHEMWEB_PASSWORD = "FHEMWEB_PASSWORD";

    public static final FHEMWebConnection INSTANCE = new FHEMWebConnection();

    private FHEMWebConnection() {
        HttpParams httpParams = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParams, 3000);
        HttpConnectionParams.setSoTimeout(httpParams, 10000);
        client = new DefaultHttpClient(httpParams);
    }

    @Override
    public String xmllist() {
        return request("xmllist");
    }

    @Override
    public String fileLogData(String logName, Date fromDate, Date toDate, String columnSpec) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String command = new StringBuilder().append("get ").append(logName).append(" - - ")
                .append(dateFormat.format(fromDate)).append(" ")
                .append(dateFormat.format(toDate)).append(" ")
                .append(columnSpec).toString();

        return request(command).replaceAll("#" + columnSpec, "").replaceAll("[\\r\\n]", "");
    }

    @Override
    public void executeCommand(String command) {
        request(command);
    }

    private String request(String command) {


        String content;
        try {
            HttpGet request = new HttpGet();
            String urlString = getURL() + "?cmd=" + URLEncoder.encode(command);
            Log.e(FHEMWebConnection.class.getName(), "accessing URL " + urlString);
            URI uri = new URI(urlString);

            client.getCredentialsProvider().setCredentials(new AuthScope(uri.getHost(), uri.getPort()),
                    new UsernamePasswordCredentials(getUsername(), getPassword()));

            request.setURI(uri);

            HttpResponse response = client.execute(request);
            content = IOUtils.toString(response.getEntity().getContent());
        } catch (Exception e) {
            throw new HostConnectionException(e);
        }

        String start = "<pre>";
        String end = "</pre>";

        int preStart = content.indexOf(start);
        int preEnd = content.indexOf(end);

        if (preStart == -1 || preEnd == -1) {
            throw new HostConnectionException();
        }

        content = content.substring(preStart + start.length(), preEnd);
        content = content.replaceAll("&lt;", "<");
        content = content.replaceAll("&gt;", ">");
        content = content.replaceAll("&quot;", "");

        return content;


    }

    private String getURL() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(AndFHEMApplication.getContext());
        String url = sharedPreferences.getString(FHEMWEB_URL, null);
        if (url.lastIndexOf("/") == url.length() - 1) {
            return url.substring(0, url.length() -1);
        }
        return url;
    }

    private String getUsername() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(AndFHEMApplication.getContext());
        return sharedPreferences.getString(FHEMWEB_USERNAME, "");
    }

    private String getPassword() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(AndFHEMApplication.getContext());
        return sharedPreferences.getString(FHEMWEB_PASSWORD, "");
    }
}
