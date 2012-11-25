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

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import li.klass.fhem.AndFHEMApplication;
import li.klass.fhem.constants.Actions;
import li.klass.fhem.exception.AndFHEMException;
import li.klass.fhem.exception.HostConnectionException;
import li.klass.fhem.exception.TimeoutException;
import li.klass.fhem.service.room.DeviceListParser;
import li.klass.fhem.util.CloseableUtil;
import org.apache.http.conn.ConnectTimeoutException;
import thor.net.DefaultTelnetTerminalHandler;
import thor.net.TelnetURLConnection;
import thor.net.URLStreamHandler;

import java.io.*;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;

import static li.klass.fhem.constants.BundleExtraKeys.DO_REFRESH;

public class TelnetConnection implements FHEMConnection {
    public static final String TELNET_URL = "TELNET_URL";
    public static final String TELNET_PORT = "TELNET_PORT";
    public static final String TELNET_PASSWORD = "TELNET_PASSWORD";

    public static final String FHEM_PROMPT = "fhem>";

    private static final String DEFAULT_HOST = "";
    private static final int DEFAULT_PORT = 0;

    public static final TelnetConnection INSTANCE = new TelnetConnection();
    private static final String PASSWORD_PROMPT = "Password: ";
    public static final String TAG = TelnetConnection.class.getName();

    private EventReceiver eventReceiver;
    private static final int UPDATE_INTENT_WAITTIME = 75;

    private TelnetConnection() {}

    public String xmllist() {
        return request("xmllist");
    }

    @Override
    public String fileLogData(String logName, Date fromDate, Date toDate, String columnSpec) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH:mm");
        String command = "get " + logName + " - - " + dateFormat.format(fromDate) + " " + dateFormat.format(toDate) + " " + columnSpec;

        return request(command);
    }

    public String executeCommand(String command) {
        return request(command);
    }

    @Override
    public Bitmap requestBitmap(String relativePath) {
        Log.e(TAG, "get image: " + relativePath);
        return null;
    }

    private String request(String command) {
        Log.i(TAG, "executeTask command " + command);

        OutputStream outputStream = null;
        BufferedOutputStream bufferedOutputStream = null;
        PrintWriter printWriter = null;
        InputStream inputStream = null;

        try {
            URL url = new URL("telnet", getHost(), getPort(), "", new thor.net.URLStreamHandler());
            URLConnection urlConnection = url.openConnection();
            urlConnection.connect();

            if (urlConnection instanceof TelnetURLConnection) {
                ((TelnetURLConnection)urlConnection).
                        setTelnetTerminalHandler(new DefaultTelnetTerminalHandler());
            }
            urlConnection.setConnectTimeout(4000);

            inputStream = urlConnection.getInputStream();


            outputStream = urlConnection.getOutputStream();
            bufferedOutputStream = new BufferedOutputStream(outputStream);
            printWriter = new PrintWriter(bufferedOutputStream);

            handlePasswordVerification(inputStream, printWriter);

            printWriter.write(command + "\r\n");
            printWriter.write("exit" + "\r\n");
            printWriter.flush();

            int ch;
            StringBuilder buffer = new StringBuilder();
            while ((ch = inputStream.read()) != -1) {
                buffer.append((char) ch);
            }

            if (urlConnection instanceof  TelnetURLConnection) {
                ((TelnetURLConnection) urlConnection).disconnect();
            }

            String returnValue = buffer.toString();
            int startPos = returnValue.indexOf(", try help");
            if (startPos != -1) {
                returnValue = returnValue.substring(startPos + ", try help".length());
            }
            if (returnValue.endsWith("Bye...")) {
                returnValue = buffer.substring(0, buffer.length() - "Bye...".length());
            }
            Log.d(TAG, "result is :: " + returnValue);
            return returnValue;
        } catch (AndFHEMException e) {
            throw e;
        } catch (ConnectTimeoutException e) {
            Log.e(TAG, "timeout", e);
            throw new TimeoutException(e);
        } catch (SocketTimeoutException e) {
            Log.e(TAG, "timeout", e);
            throw new TimeoutException(e);
        } catch (Exception e) {
            Log.e(TAG, "error occurred", e);
            throw new HostConnectionException(e);
        } finally {
            CloseableUtil.close(printWriter, bufferedOutputStream, outputStream, inputStream);
        }
    }

    private boolean handlePasswordVerification(InputStream inputStream, PrintWriter printWriter) throws Exception {
        String password = getPassword();
        if (password == null || "".equals(password)) return true;

        printWriter.write(password + "\n\r");
        printWriter.flush();

        return waitForPasswordPrompt(inputStream);
    }

    private boolean waitForPasswordPrompt(InputStream inputStream) throws Exception {
        int ch;
        int passwordPointer = 0;
        int fhemPromptPointer = 0;

        boolean validCharacter;
        while ((ch = inputStream.read()) != -1) {
            validCharacter = false;
            if (ch == PASSWORD_PROMPT.charAt(passwordPointer)) {
                passwordPointer ++;
                validCharacter = true;
            } else {
                passwordPointer = 0;
            }

            if (passwordPointer >= PASSWORD_PROMPT.length()) {
                return true;
            }

            if (ch == FHEM_PROMPT.charAt(fhemPromptPointer)) {
                fhemPromptPointer++;
                validCharacter = true;
            } else {
                fhemPromptPointer = 0;
            }

            if (fhemPromptPointer >= FHEM_PROMPT.length()) {
                return false;
            }

            if (! validCharacter) {
                return  false;
            }
        }
        return false;
    }

    private String getHost() {
        String host = getPreferenceString(TELNET_URL, DEFAULT_HOST);
        Log.d(TAG, "telnet host is '" + host + "'");
        return host;
    }

    private int getPort() {
        String value = getPreferenceString(TELNET_PORT, String.valueOf(DEFAULT_PORT));
        Log.d(TAG, "telnet port is '" + value + "'");
        return Integer.valueOf(value);
    }

    private String getPassword() {
        String password = getPreferenceString(TELNET_PASSWORD, "");
        String logMessage = password.equals("") ? "has no password" : "has password";
        Log.d(TAG, "telnet connection " + logMessage + " configured");
        return password;
    }

    private String getPreferenceString(String key, String defaultValue) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(AndFHEMApplication.getContext());
        return sharedPreferences.getString(key, defaultValue);
    }

    @Override
    public void startEventReceiver() {
        if (eventReceiver == null || eventReceiver.isCancelled()) {
            eventReceiver = new EventReceiver();
            eventReceiver.execute();
        }
    }

    @Override
    public void stopEventReceiver() {
        eventReceiver.cancel(false);
    }

    private class EventReceiver extends AsyncTask<Void, Void, Void> {

        private StringBuilder buffer = new StringBuilder();
        private Handler handler = new Handler();
        private Runnable updateIntentSender = new Runnable() {
            @Override
            public void run() {
                Intent refreshIntent = new Intent(Actions.DO_UPDATE);
                refreshIntent.putExtra(DO_REFRESH, false);
                AndFHEMApplication.getContext().sendBroadcast(refreshIntent);

                Log.d(TAG, "event received. Update intent started.");
            }
        };

        @Override
        protected Void doInBackground(Void... params) {
            Log.i(TAG, "event receiver started");

            OutputStream outputStream = null;
            BufferedOutputStream bufferedOutputStream = null;
            PrintWriter printWriter = null;
            InputStream inputStream = null;
            InputStreamReader inputStreamReader = null;

            try {
                URL url = new URL("telnet", getHost(), getPort(), "", new URLStreamHandler());
                URLConnection urlConnection = url.openConnection();
                urlConnection.connect();

                if (! (urlConnection instanceof  TelnetURLConnection)) {
                    Log.e(TAG, "expected a " + TelnetURLConnection.class.getSimpleName() + ", " +
                            "but got a " + urlConnection.getClass().getSimpleName());
                    return null;
                }

                TelnetURLConnection telnetURLConnection = (TelnetURLConnection) urlConnection;

                telnetURLConnection.setTelnetTerminalHandler(new DefaultTelnetTerminalHandler() {
                    @Override
                    public void LineFeed() {
                        handler.removeCallbacks(updateIntentSender);

                        String line = buffer.toString();
                        buffer = new StringBuilder();
                        try {
                            DeviceListParser.INSTANCE.parseEvent(line.trim());
                        } catch (Exception e) {
                            Log.e(TAG, "event parse error", e);
                        }

                        handler.postDelayed(updateIntentSender, UPDATE_INTENT_WAITTIME);
                    }
                });

                urlConnection.setConnectTimeout(0);

                inputStream = urlConnection.getInputStream();

                outputStream = urlConnection.getOutputStream();
                bufferedOutputStream = new BufferedOutputStream(outputStream);
                printWriter = new PrintWriter(bufferedOutputStream);

                handlePasswordVerification(inputStream, printWriter);

                printWriter.write("inform timer\r\n");
                printWriter.flush();

                int ch;
                while ((ch = inputStream.read()) != -1) {
                    buffer.append((char) ch);
                }

                if (urlConnection instanceof TelnetURLConnection) {
                    telnetURLConnection.disconnect();
                }
            } catch (AndFHEMException e) {
                throw e;
            } catch (ConnectTimeoutException e) {
                Log.e(TAG, "timeout", e);
                throw new TimeoutException(e);
            } catch (SocketTimeoutException e) {
                Log.e(TAG, "timeout", e);
                throw new TimeoutException(e);
            } catch (Exception e) {
                Log.e(TAG, "error occurred", e);
                throw new HostConnectionException(e);
            } finally {
                CloseableUtil.close(printWriter, bufferedOutputStream,
                        outputStream, inputStream, inputStreamReader);
            }

            Log.i(TAG, "event receiver stopped");

            return null;
        }
    }
}
