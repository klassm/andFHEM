package li.klass.fhem.settings.type

import android.app.Activity
import android.content.SharedPreferences
import android.os.Build
import android.preference.Preference
import li.klass.fhem.R
import li.klass.fhem.devices.list.ui.DeviceListFragment
import li.klass.fhem.settings.SettingsKeys
import li.klass.fhem.settings.updater.IntSummaryAction
import li.klass.fhem.settings.updater.ListSummaryAction
import li.klass.fhem.settings.updater.SettingsUpdater
import li.klass.fhem.widget.preference.SeekBarPreference
import javax.inject.Inject

class AppearanceTypeHandler @Inject constructor()
    : SettingsTypeHandler("appearance", updateHandlers) {

    override fun getResource(): Int = R.xml.settings_appearance

    override fun initWith(sharedPreferences: SharedPreferences, preferenceFinder: (String) -> Preference, activity: Activity) {
        val deviceColumnWidthPreference = preferenceFinder(SettingsKeys.DEVICE_COLUMN_WIDTH) as SeekBarPreference
        deviceColumnWidthPreference.setMinimumValue(200)
        deviceColumnWidthPreference.setDefaultValue(DeviceListFragment.DEFAULT_COLUMN_WIDTH)
        deviceColumnWidthPreference.setMaximumValue(800)

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            preferenceFinder(SettingsKeys.THEME).isEnabled = false
        }
    }

    companion object {
        val updateHandlers = listOf(
                SettingsUpdater(SettingsKeys.STARTUP_VIEW,
                        ListSummaryAction(R.string.settingsStartupViewSummary, R.array.startupViewsValues, R.array.startupViews)),
                SettingsUpdater(SettingsKeys.GRAPH_DEFAULT_TIMESPAN,
                        ListSummaryAction(R.string.settingsDefaultTimespanSummary, R.array.graphDefaultTimespanValues, R.array.graphDefaultTimespanEntries)),
                SettingsUpdater(SettingsKeys.DEVICE_LIST_RIGHT_PADDING,
                        IntSummaryAction(R.string.settingsDeviceListPaddingRightSummary)),
                SettingsUpdater(SettingsKeys.DEVICE_COLUMN_WIDTH,
                        IntSummaryAction(R.string.settingsDeviceColumnWidthSummary))
        )
    }
}