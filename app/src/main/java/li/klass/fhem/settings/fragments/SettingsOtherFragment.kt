package li.klass.fhem.settings.fragments

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import li.klass.fhem.R

class SettingsOtherFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings_others, rootKey)
    }
}