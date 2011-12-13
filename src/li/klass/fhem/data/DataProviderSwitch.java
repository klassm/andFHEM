package li.klass.fhem.data;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import li.klass.fhem.AndFHEMApplication;
import li.klass.fhem.data.provider.DummyDataProvider;
import li.klass.fhem.data.provider.FHEMDataProvider;
import li.klass.fhem.data.provider.TelnetProvider;

public class DataProviderSwitch {
    public static final DataProviderSwitch INSTANCE = new DataProviderSwitch();

    private DataProviderSwitch() {
    }
    private SharedPreferences getPreferences() {

        return PreferenceManager.getDefaultSharedPreferences(AndFHEMApplication.getContext());
    }

    public FHEMDataProvider getCurrentProvider() {
        boolean useDummyData = getPreferences().getBoolean("prefUseDummyData", true);
        if (useDummyData) {
            return DummyDataProvider.INSTANCE;
        } else {
            return TelnetProvider.INSTANCE;
        }
    }
}
