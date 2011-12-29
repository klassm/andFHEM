package li.klass.fhem.data.provider;

import android.util.Log;
import li.klass.fhem.util.CloseableUtil;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

public class DummyDataProvider implements FHEMDataProvider {
    public static final DummyDataProvider INSTANCE = new DummyDataProvider();

    private DummyDataProvider() {
    }
    
    @Override
    public String xmllist() {
        InputStream inputStream = null;
        try {
            inputStream = DummyDataProvider.class.getResource("dummyData.xml").openStream();
            String content = IOUtils.toString(inputStream);
            content = content.replaceAll("\n", "");
            content = content.replaceAll("  ", "");
            return content;
        } catch (IOException e) {
            Log.e(DummyDataProvider.class.getName(), "cannot read file", e);
            throw new RuntimeException(e);
        } finally {
            CloseableUtil.close(inputStream);
        }
    }

    @Override
    public String fileLogData(String logName, Date fromDate, Date toDate, String columnSpec) {
        return "2011-12-23_00:16:48 4.2" +
                "2011-12-23_01:19:21 5.2" +
                "2011-12-23_02:21:53 5.2" +
                "2011-12-23_03:24:26 6.2" +
                "2011-12-23_04:26:58 7.3" +
                "2011-12-23_05:32:03 8.2" +
                "2011-12-23_06:37:08 9.3" +
                "2011-12-23_07:39:41 8.3" +
                "2011-12-23_08:42:13 6.3" +
                "2011-12-23_09:44:46 5.3" +
                "2011-12-23_10:49:51 4.3" +
                "2011-12-23_11:52:23 3.3" +
                "2011-12-23_12:54:56 2.3" +
                "2011-12-23_13:57:28 1.3" +
                "#" + columnSpec;
    }

    @Override
    public void executeCommand(String command) {
        Log.d(DummyDataProvider.class.getName(), "execute command " + command);
    }
}
