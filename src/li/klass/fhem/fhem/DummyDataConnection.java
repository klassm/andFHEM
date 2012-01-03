package li.klass.fhem.fhem;

import android.util.Log;
import li.klass.fhem.util.CloseableUtil;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DummyDataConnection implements FHEMConnection {
    public static final DummyDataConnection INSTANCE = new DummyDataConnection();

    private DummyDataConnection() {
    }
    
    @Override
    public String xmllist() {

        InputStream inputStream = null;
        try {
            inputStream = DummyDataConnection.class.getResource("dummyData.xml").openStream();
            String content = IOUtils.toString(inputStream);
            content = content.replaceAll("\n", "");
            content = content.replaceAll("  ", "");
            return content;
        } catch (IOException e) {
            Log.e(DummyDataConnection.class.getName(), "cannot read file", e);
            throw new RuntimeException(e);
        } finally {
            CloseableUtil.close(inputStream);
        }
    }

    @Override
    public String fileLogData(String logName, Date fromDate, Date toDate, String columnSpec) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String today = simpleDateFormat.format(new Date());

        return today + "_00:16:48 4.2" +
                today + "_01:19:21 5.2" +
                today + "_02:21:53 5.2" +
                today + "_03:24:26 6.2" +
                today + "_04:26:58 7.3" +
                today + "_05:32:03 8.2" +
                today + "_06:37:08 9.3" +
                today + "_07:39:41 8.3" +
                today + "_08:42:13 6.3" +
                today + "_09:44:46 5.3" +
                today + "_10:49:51 4.3" +
                today + "_11:52:23 3.3" +
                today + "_12:54:56 2.3" +
                today + "_13:57:28 1.3" +
                "#" + columnSpec;
    }

    @Override
    public void executeCommand(String command) {
        Log.d(DummyDataConnection.class.getName(), "execute command " + command);
    }
}
