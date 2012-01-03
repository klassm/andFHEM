package li.klass.fhem.fhem;

import li.klass.fhem.util.ApplicationProperties;

public class DataConnectionSwitch {
    public static final DataConnectionSwitch INSTANCE = new DataConnectionSwitch();

    private DataConnectionSwitch() {
    }

    public FHEMConnection getCurrentProvider() {
        if (ApplicationProperties.INSTANCE.isDummyMode()) {
            return DummyDataConnection.INSTANCE;
        } else {
            return TelnetConnection.INSTANCE;
        }
    }
}
