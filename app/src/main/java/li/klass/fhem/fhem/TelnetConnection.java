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
import android.util.Log;

import org.apache.commons.net.telnet.TelnetClient;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import li.klass.fhem.util.CloseableUtil;
import li.klass.fhem.util.StringUtil;

public class TelnetConnection extends FHEMConnection {
    public static final String TELNET_URL = "TELNET_URL";
    public static final String TELNET_PORT = "TELNET_PORT";
    public static final String TELNET_PASSWORD = "TELNET_PASSWORD";
    private static final String PASSWORD_PROMPT = "Password: ";
    public static final String TAG = TelnetConnection.class.getName();

    public static final TelnetConnection INSTANCE = new TelnetConnection();

    private TelnetConnection() {
    }

    public RequestResult<String> executeCommand(String command) {
        Log.i(TAG, "executeTask command " + command);

        final TelnetClient telnetClient = new TelnetClient();
        telnetClient.setConnectTimeout(4000);

        OutputStream outputStream = null;
        BufferedOutputStream bufferedOutputStream = null;
        PrintStream printStream = null;
        InputStream inputStream = null;

        try {
            telnetClient.connect(serverSpec.getIp(), serverSpec.getPort());

            outputStream = telnetClient.getOutputStream();
            inputStream = telnetClient.getInputStream();

            bufferedOutputStream = new BufferedOutputStream(outputStream);
            printStream = new PrintStream(outputStream);

            // If we don't receive an initial token during the first seconds, we haven't got
            // a valid connection.
            if (! waitForFilledStream(inputStream, 5000)) {
                return new RequestResult<String>(RequestResultError.HOST_CONNECTION_ERROR);
            }

            boolean passwordSent = false;
            String passwordRead = readUntil(inputStream, PASSWORD_PROMPT);
            if (passwordRead.contains(PASSWORD_PROMPT)) {
                Log.i(TAG, "sending password");
                writeCommand(printStream, serverSpec.getPassword());
                passwordSent = true;
            }

            writeCommand(printStream, command);

            // If we send an xmllist, we are done when finding the closing FHZINFO tag.
            // If another command is used, the tag ending delimiter is obsolete, not found and
            // therefore not used. We just read until the stream ends.
            String result;
            if (command.equals("xmllist")) {
                result = readUntil(inputStream, "</FHZINFO>");
            } else {
                result = read(inputStream);
            }

            if (result == null && passwordSent) {
                return new RequestResult<String>(RequestResultError.AUTHENTICATION_ERROR);
            } else if (result == null) {
                return new RequestResult<String>(RequestResultError.INVALID_CONTENT);
            }

            telnetClient.disconnect();

            int startPos = result.indexOf(", try help");
            if (startPos != -1) {
                result = result.substring(startPos + ", try help".length());
            }

            startPos = result.indexOf("<");
            if (startPos != -1) {
                result = result.substring(startPos);
            }

            result = result.replaceAll("Bye...", "");
            result = new String(result.getBytes("UTF8"));
            Log.d(TAG, "result is :: " + result);

            return new RequestResult<String>(result);

        } catch (SocketTimeoutException e) {
            Log.e(TAG, "timeout", e);
            return new RequestResult<String>(RequestResultError.CONNECTION_TIMEOUT);
        } catch (UnsupportedEncodingException e) {
            // this may never happen, as UTF8 is known ...
            throw new IllegalStateException("unsupported encoding", e);
        } catch (SocketException e) {
            // We handle host connection errors directly after connecting to the server by waiting
            // for some token for some seconds. Afterwards, the only possibility for an error
            // is that the FHEM server ends the connection after receiving an invalid password.
            Log.e(TAG, "SocketException", e);
            return new RequestResult<String>(RequestResultError.AUTHENTICATION_ERROR);
        } catch (IOException e) {
            Log.e(TAG, "IOException", e);
            return new RequestResult<String>(RequestResultError.HOST_CONNECTION_ERROR);
        } finally {
            CloseableUtil.close(printStream, bufferedOutputStream,
                    outputStream, inputStream);
        }
    }

    @Override
    public RequestResult<Bitmap> requestBitmap(String relativePath) {
        Log.e(TAG, "get image: " + relativePath);
        return new RequestResult<Bitmap>(null, null);
    }

    private String readUntil(InputStream inputStream, String... blockers) throws IOException {
        waitForFilledStream(inputStream, 1000);

        int ch;
        StringBuilder buffer = new StringBuilder();
        while ((ch = inputStream.read()) != -1) {
            buffer.append((char) ch);
            for (String blocker : blockers) {
                if (StringUtil.endsWith(buffer, blocker)) return buffer.toString();
            }
        }
        return null;
    }

    private String read(InputStream inputStream) throws IOException {
        waitForFilledStream(inputStream, 1000);

        int ch;
        StringBuilder buffer = new StringBuilder();
        while ((ch = inputStream.read()) != -1) {
            char readChar = (char) ch;
            System.out.println(ch + " " + readChar);
            buffer.append(readChar);
        }
        return buffer.toString();
    }

    private void writeCommand(PrintStream printStream, String command) {
        printStream.println(command);
        printStream.flush();
    }

    private boolean waitForFilledStream(InputStream inputStream, int timeToWait) throws IOException {
        long startTime = System.currentTimeMillis();
        while(inputStream.available() == 0 &&
                (System.currentTimeMillis() - startTime) < timeToWait) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Log.e(TAG, "interrupted, ignoring", e);
            }
        }
        return inputStream.available() > 0;
    }
}