package li.klass.fhem.settings.fragments

import android.os.Bundle
import androidx.preference.ListPreference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SeekBarPreference
import li.klass.fhem.R
import li.klass.fhem.devices.list.ui.DeviceListFragment
import li.klass.fhem.settings.SettingsKeys
import li.klass.fhem.settings.updater.ListSummaryProvider

class SettingsAppearanceFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings_appearance, rootKey)

        findPreference<SeekBarPreference>(SettingsKeys.DEVICE_COLUMN_WIDTH)?.apply {
            min = 200
            max = 800
            setDefaultValue(DeviceListFragment.DEFAULT_COLUMN_WIDTH)
        }

        findPreference<ListPreference>(SettingsKeys.STARTUP_VIEW)?.apply {
            summaryProvider = ListSummaryProvider(
                R.string.settingsStartupViewSummary,
                R.array.startupViewsValues,
                R.array.startupViews
            )
        }

        findPreference<ListPreference>(SettingsKeys.GRAPH_DEFAULT_TIMESPAN)?.apply {
            summaryProvider = ListSummaryProvider(
                R.string.settingsDefaultTimespanSummary,
                R.array.graphDefaultTimespanValues,
                R.array.graphDefaultTimespanEntries
            )
        }

        findPreference<SeekBarPreference>(SettingsKeys.DEVICE_LIST_RIGHT_PADDING)?.apply {
            showSeekBarValue = true
        }
        findPreference<SeekBarPreference>(SettingsKeys.DEVICE_COLUMN_WIDTH)?.apply {
            showSeekBarValue = true
        }
    }
}