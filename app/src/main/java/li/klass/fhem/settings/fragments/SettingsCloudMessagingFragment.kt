package li.klass.fhem.settings.fragments

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import li.klass.fhem.R

class SettingsCloudMessagingFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings_cloud_messaging, rootKey)
    }
}