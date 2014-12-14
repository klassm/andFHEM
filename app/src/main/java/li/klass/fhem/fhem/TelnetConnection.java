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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import li.klass.fhem.util.CloseableUtil;
import li.klass.fhem.util.StringUtil;

public class TelnetConnection extends FHEMConnection {
    public static final String TAG = TelnetConnection.class.getName();
    public static final TelnetConnection INSTANCE = new TelnetConnection();
    private static final String PASSWORD_PROMPT = "Password: ";

    private static final Logger LOG = LoggerFactory.getLogger(TelnetConnection.class.getName());

    private TelnetConnection() {
    }

    public RequestResult<String> executeCommand(String command) {
        LOG.info("executeTask command {}", command);

        final TelnetClient telnetClient = new TelnetClient();
        telnetClient.setConnectTimeout(getConnectionTimeoutMilliSeconds());

        BufferedOutputStream bufferedOutputStream = null;
        PrintStream printStream = null;

        String errorHost = serverSpec.getIp() + ":" + serverSpec.getPort();
        try {
            telnetClient.connect(serverSpec.getIp(), serverSpec.getPort());

            OutputStream outputStream = telnetClient.getOutputStream();
            InputStream inputStream = telnetClient.getInputStream();

            bufferedOutputStream = new BufferedOutputStream(outputStream);
            printStream = new PrintStream(outputStream);

            boolean passwordSent = false;
            String passwordRead = readUntil(inputStream, PASSWORD_PROMPT);
            if (passwordRead != null && passwordRead.contains(PASSWORD_PROMPT)) {
                LOG.info("sending password");
                writeCommand(printStream, serverSpec.getPassword());
                passwordSent = true;
            }

            writeCommand(printStream, "\n\n");

            if (!waitForFilledStream(inputStream, 5000)) {
                return new RequestResult<>(RequestResultError.HOST_CONNECTION_ERROR);
            }

            // to discard
            String toDiscard = read(inputStream);
            LOG.debug("discarding {}", toDiscard);

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
                return new RequestResult<>(RequestResultError.AUTHENTICATION_ERROR);
            } else if (result == null) {
                return new RequestResult<>(RequestResultError.INVALID_CONTENT);
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

            result = result
                    .replaceAll("Bye...", "")
                    .replaceAll("fhem>", "");
            result = new String(result.getBytes("UTF8"));
            LOG.debug("result is {}", result);

            return new RequestResult<>(result);

        } catch (SocketTimeoutException e) {
            LOG.error("timeout", e);
            setErrorInErrorHolderFor(e, errorHost, command);
            return new RequestResult<>(RequestResultError.CONNECTION_TIMEOUT);
        } catch (UnsupportedEncodingException e) {
            // this may never happen, as UTF8 is known ...
            setErrorInErrorHolderFor(e, errorHost, command);
            throw new IllegalStateException("unsupported encoding", e);
        } catch (SocketException e) {
            // We handle host connection errors directly after connecting to the server by waiting
            // for some token for some seconds. Afterwards, the only possibility for an error
            // is that the FHEM server ends the connection after receiving an invalid password.
            LOG.error("SocketException", e);
            setErrorInErrorHolderFor(e, errorHost, command);
            return new RequestResult<>(RequestResultError.AUTHENTICATION_ERROR);
        } catch (IOException e) {
            LOG.error("IOException", e);
            setErrorInErrorHolderFor(e, errorHost, command);
            return new RequestResult<>(RequestResultError.HOST_CONNECTION_ERROR);
        } finally {
            CloseableUtil.close(printStream, bufferedOutputStream);
        }
    }

    private String readUntil(InputStream inputStream, String... blockers) throws IOException {
        waitForFilledStream(inputStream, 3000);

        StringBuilder buffer = new StringBuilder();
        while (inputStream.available() > 0 || waitForFilledStream(inputStream, 300)) {
            char readChar = (char) inputStream.read();
            buffer.append(readChar);
            for (String blocker : blockers) {
                if (StringUtil.endsWith(buffer, blocker)) return buffer.toString();
            }
        }
        LOG.error("read data, but did not find end token, read content was '{}'");
        return null;
    }

    private void writeCommand(PrintStream printStream, String command) {
        printStream.println(command);
        printStream.flush();
    }

    private boolean waitForFilledStream(InputStream inputStream, int timeToWait) throws IOException {
        int initialFill = inputStream.available();

        long startTime = System.currentTimeMillis();
        while (inputStream.available() == initialFill &&
                (System.currentTimeMillis() - startTime) < timeToWait) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                LOG.debug("interrupted, ignoring", e);
            }
        }
        return inputStream.available() > 0;
    }

    private String read(InputStream inputStream) throws IOException {
        waitForFilledStream(inputStream, 3000);

        StringBuilder buffer = new StringBuilder();
        while (inputStream.available() > 0 || waitForFilledStream(inputStream, 100)) {
            char readChar = (char) inputStream.read();
            buffer.append(readChar);
        }
        return buffer.toString();
    }

    @Override
    public RequestResult<Bitmap> requestBitmap(String relativePath) {
        LOG.debug("get image from relative path '{}'", relativePath);
        return new RequestResult<>(null, null);
    }
}