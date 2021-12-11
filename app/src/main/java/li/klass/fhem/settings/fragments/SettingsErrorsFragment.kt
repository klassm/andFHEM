package li.klass.fhem.settings.fragments

import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import li.klass.fhem.R
import li.klass.fhem.error.ErrorHolder
import li.klass.fhem.settings.SettingsKeys

class SettingsErrorsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings_errors, rootKey)

        findPreference<Preference>(SettingsKeys.SEND_LAST_ERROR)?.apply {
            onPreferenceClickListener = Preference.OnPreferenceClickListener {
                activity?.let { ErrorHolder.sendLastErrorAsMail(it) }
                true
            }
        }
        findPreference<Preference>(SettingsKeys.SEND_APP_LOG)?.apply {
            onPreferenceClickListener = Preference.OnPreferenceClickListener {
                activity?.let { ErrorHolder.sendApplicationLogAsMail(it) }
                true
            }
        }
    }
}