package li.klass.fhem.settings.fragments

import android.os.Bundle
import androidx.preference.ListPreference
import androidx.preference.PreferenceFragmentCompat
import li.klass.fhem.R
import li.klass.fhem.settings.SettingsKeys
import li.klass.fhem.settings.updater.ListSummaryProvider

class SettingsWidgetsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings_widgets, rootKey)

        findPreference<ListPreference>(SettingsKeys.WIDGET_UPDATE_INTERVAL_WLAN)?.apply {
            summaryProvider = ListSummaryProvider(
                R.string.settingsWidgetUpdateTimeWLANSummary,
                R.array.widgetUpdateTimeValues,
                R.array.widgetUpdateTimeEntries
            )
        }
        findPreference<ListPreference>(SettingsKeys.WIDGET_UPDATE_INTERVAL_MOBILE)?.apply {
            summaryProvider = ListSummaryProvider(
                R.string.settingsWidgetUpdateTimeMobileSummary,
                R.array.widgetUpdateTimeValues,
                R.array.widgetUpdateTimeEntries
            )
        }
    }
}