package li.klass.fhem.activities;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import li.klass.fhem.R;

public class PreferencesActivity extends PreferenceActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.layout.preferences);
    }

}