package li.klass.fhem.data.provider;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import li.klass.fhem.AndFHEMApplication;
import li.klass.fhem.util.CloseableUtil;
import thor.net.DefaultTelnetTerminalHandler;
import thor.net.TelnetURLConnection;

import java.io.BufferedOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;

public class TelnetProvider implements FHEMDataProvider {
    private static final String DEFAULT_HOST = "";
    private static final int DEFAULT_PORT = 0;

    public static final TelnetProvider INSTANCE = new TelnetProvider();

    private TelnetProvider() {}

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
                Log.e(TelnetProvider.class.getName(), "done");
                result = buffer.toString();
            }

            if (urlConnection instanceof  TelnetURLConnection) {
                ((TelnetURLConnection) urlConnection).disconnect();
            }

            return result;
        } catch (Exception e) {
            throw new RuntimeException(e);
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
