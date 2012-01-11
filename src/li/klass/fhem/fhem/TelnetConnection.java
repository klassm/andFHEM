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
import li.klass.fhem.util.CloseableUtil;
import thor.net.DefaultTelnetTerminalHandler;
import thor.net.TelnetURLConnection;

import java.io.BufferedOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TelnetConnection implements FHEMConnection {
    private static final String DEFAULT_HOST = "";
    private static final int DEFAULT_PORT = 0;

    public static final TelnetConnection INSTANCE = new TelnetConnection();

    private TelnetConnection() {}

    public String xmllist() {
        return request("xmllist", "</FHZINFO>");
    }

    @Override
    public String fileLogData(String logName, Date fromDate, Date toDate, String columnSpec) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String command = new StringBuilder().append("get ").append(logName).append(" - - ")
                .append(dateFormat.format(fromDate)).append(" ")
                .append(dateFormat.format(toDate)).append(" ")
                .append(columnSpec).toString();

        return request(command, "#" + columnSpec);
    }

    public void executeCommand(String command) {
        request(command, null);
    }

    private String request(String command, String delimiter) {
        Log.e(TelnetConnection.class.getName(), "executeTask command " + command + " with delimiter " + delimiter);

        OutputStream outputStream = null;
        BufferedOutputStream bufferedOutputStream = null;
        PrintWriter printWriter = null;
        InputStream inputStream = null;

        try {
            URL url = new URL("telnet", getHost(), getPort(), "", new thor.net.URLStreamHandler());
            URLConnection urlConnection=url.openConnection();
            urlConnection.connect();

            if (urlConnection instanceof TelnetURLConnection) {
                ((TelnetURLConnection)urlConnection).
                        setTelnetTerminalHandler(new DefaultTelnetTerminalHandler());
            }

            inputStream = urlConnection.getInputStream();

            outputStream = urlConnection.getOutputStream();
            bufferedOutputStream = new BufferedOutputStream(outputStream);
            printWriter = new PrintWriter(bufferedOutputStream);
            printWriter.write(command + "\r\n");
            printWriter.flush();

            String result = null;
            int stopPointer = 0;
            if (delimiter != null) {
                StringBuilder buffer = new StringBuilder();
                do {
                    int ch = inputStream.read();
                    buffer.append((char) ch);

                    if (ch == delimiter.charAt(stopPointer)) {
                        stopPointer ++;

                    } else {
                        stopPointer = 0;
                    }
                } while(stopPointer < delimiter.length());
                result = buffer.toString();
            }

            if (urlConnection instanceof  TelnetURLConnection) {
                ((TelnetURLConnection) urlConnection).disconnect();
            }

            return result;
        } catch (Exception e) {
            Log.e(TelnetConnection.class.getName(), "error occurred", e);
            throw new HostConnectionException(e);
        } finally {
            CloseableUtil.close(printWriter, bufferedOutputStream, outputStream, inputStream);
        }
    }

    private String getHost() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(AndFHEMApplication.getContext());
        return sharedPreferences.getString("URL", DEFAULT_HOST);
    }

    private int getPort() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(AndFHEMApplication.getContext());
        return Integer.valueOf(sharedPreferences.getString("PORT", String.valueOf(DEFAULT_PORT)));
    }
}
