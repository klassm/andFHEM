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
        return "";
    }

    @Override
    public void executeCommand(String command) {
        Log.d(DummyDataProvider.class.getName(), "execute command " + command);
    }
}
