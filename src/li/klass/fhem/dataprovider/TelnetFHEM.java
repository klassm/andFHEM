package li.klass.fhem.dataprovider;

import android.util.Log;
import li.klass.fhem.util.CloseableUtil;
import thor.net.DefaultTelnetTerminalHandler;
import thor.net.TelnetURLConnection;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class TelnetFHEM {
    private static final String host = "192.168.0.1";
    private static final int port = 7072;

    public static final TelnetFHEM INSTANCE = new TelnetFHEM();

    private TelnetFHEM() {}

    public String xmllist() {
        return request("xmllist", "</FHZINFO>");
    }

    public void executeCommand(String command) {
        request(command, null);
    }

    private String request(String command, String delimiter) {
        OutputStream outputStream = null;
        BufferedOutputStream bufferedOutputStream = null;
        PrintWriter printWriter = null;
        InputStream inputStream = null;
        
        try {
            URL url = new URL("telnet", host, port, "", new thor.net.URLStreamHandler());
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
                int ch;
                do {
                    ch = inputStream.read();
                    buffer.append((char) ch);

                    if (ch == delimiter.charAt(stopPointer)) {
                        stopPointer ++;

                    } else {
                        stopPointer = 0;
                    }
                } while(stopPointer < delimiter.length());
                Log.e(TelnetFHEM.class.getName(), "done");
                result = buffer.toString();
            }

            if (urlConnection instanceof  TelnetURLConnection) {
                ((TelnetURLConnection) urlConnection).disconnect();
            }

            return result;
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            CloseableUtil.close(printWriter, bufferedOutputStream, outputStream, inputStream);
        }
    }
}
