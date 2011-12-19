package li.klass.fhem.data.provider;

import java.util.Date;

public interface FHEMDataProvider {
    String xmllist();
    String fileLogData(String logName, Date fromDate, Date toDate, String columnSpec);
    void executeCommand(String command);
}
