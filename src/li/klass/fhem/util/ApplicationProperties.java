package li.klass.fhem.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import li.klass.fhem.AndFHEMApplication;

public class ApplicationProperties {
    public static final ApplicationProperties INSTANCE = new ApplicationProperties();

    private ApplicationProperties() {
    }

    public boolean getProperty(String key, boolean defaultValue) {
        SharedPreferences preferences = getPreferences();
        return preferences.getBoolean(key, defaultValue);
    }
    
    public void setProperty(String key, boolean value) {
        SharedPreferences preferences = getPreferences();
        preferences.edit().putBoolean(key, value).commit();
    }

    private SharedPreferences getPreferences() {
        Context context = AndFHEMApplication.getContext();
        return context.getSharedPreferences(AndFHEMApplication.class.getName(), Context.MODE_PRIVATE);
    }

    public boolean isDummyMode() {
        return PreferenceManager.getDefaultSharedPreferences(AndFHEMApplication.getContext()).getBoolean("prefUseDummyData", true);
    }
}
