package li.klass.fhem.data;

import li.klass.fhem.data.provider.DummyDataProvider;
import li.klass.fhem.data.provider.FHEMDataProvider;
import li.klass.fhem.data.provider.TelnetProvider;
import li.klass.fhem.util.ApplicationProperties;

public class DataProviderSwitch {
    public static final DataProviderSwitch INSTANCE = new DataProviderSwitch();

    private DataProviderSwitch() {
    }

    public FHEMDataProvider getCurrentProvider() {
        if (ApplicationProperties.INSTANCE.isDummyMode()) {
            return DummyDataProvider.INSTANCE;
        } else {
            return TelnetProvider.INSTANCE;
        }
    }
}
