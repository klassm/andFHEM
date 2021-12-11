package li.klass.fhem.settings.fragments

import android.os.Bundle
import androidx.preference.ListPreference
import androidx.preference.PreferenceFragmentCompat
import li.klass.fhem.R
import li.klass.fhem.settings.SettingsKeys
import li.klass.fhem.settings.updater.ListSummaryProvider

class SettingsAutoUpdateFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings_auto_update, rootKey)

        findPreference<ListPreference>(SettingsKeys.AUTO_UPDATE_TIME)?.apply {
            summaryProvider = ListSummaryProvider(
                R.string.settingsAutoUpdateSummary,
                R.array.updateRoomListTimeValues,
                R.array.updateRoomListTimeEntries
            )
        }
    }
}