package li.klass.fhem.fhem;

import java.util.Date;

public interface FHEMConnection {
    String xmllist();
    String fileLogData(String logName, Date fromDate, Date toDate, String columnSpec);
    void executeCommand(String command);
}
