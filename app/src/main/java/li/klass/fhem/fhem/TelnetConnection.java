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
import java.net.SocketTimeoutException;

import li.klass.fhem.exception.AndFHEMException;
import li.klass.fhem.exception.AuthenticationException;
import li.klass.fhem.exception.FHEMStrangeContentException;
import li.klass.fhem.exception.HostConnectionException;
import li.klass.fhem.exception.TimeoutException;
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

            String passwordRead = readUntil(inputStream, PASSWORD_PROMPT);
            boolean passwordSent = false;
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
                throw new AuthenticationException();
            } else if (result == null) {
                throw new FHEMStrangeContentException();
            }

            writeCommand(printStream, "exit");

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
            return result;
        } catch (AndFHEMException e) {
            throw e;
        } catch (SocketTimeoutException e) {
            Log.e(TAG, "timeout", e);
            throw new TimeoutException(e);
        } catch (Exception e) {
            Log.e(TAG, "error occurred", e);
            throw new HostConnectionException(e);
        } finally {
            CloseableUtil.close(printStream, bufferedOutputStream,
                    outputStream, inputStream);
        }
    }

    private String readUntil(InputStream inputStream, String... blockers) throws IOException {
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
        int ch;
        StringBuilder buffer = new StringBuilder();
        while ((ch = inputStream.read()) != -1) {
            buffer.append((char) ch);
        }
        return buffer.toString();
    }

    private void writeCommand(PrintStream printStream, String command) {
        printStream.println(command);
        printStream.flush();
    }
}