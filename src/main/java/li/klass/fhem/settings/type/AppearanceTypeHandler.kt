package li.klass.fhem.settings.type

import android.app.Activity
import android.content.SharedPreferences
import android.preference.Preference
import li.klass.fhem.R
import li.klass.fhem.constants.PreferenceKeys
import li.klass.fhem.fragments.core.DeviceListFragment
import li.klass.fhem.settings.updater.IntSummaryAction
import li.klass.fhem.settings.updater.ListSummaryAction
import li.klass.fhem.settings.updater.PreferenceUpdater
import li.klass.fhem.widget.preference.SeekBarPreference
import javax.inject.Inject

class AppearanceTypeHandler @Inject constructor()
    : SettingsTypeHandler("appearance", updateHandlers) {

    override fun getResource(): Int = R.xml.preferences_appearance

    override fun initWith(sharedPreferences: SharedPreferences, preferenceFinder: (String) -> Preference, activity: Activity) {
        val deviceColumnWidthPreference = preferenceFinder(PreferenceKeys.DEVICE_COLUMN_WIDTH) as SeekBarPreference
        deviceColumnWidthPreference.setMinimumValue(200)
        deviceColumnWidthPreference.setDefaultValue(DeviceListFragment.DEFAULT_COLUMN_WIDTH)
        deviceColumnWidthPreference.setMaximumValue(800)
    }

    companion object {
        val updateHandlers = listOf(
                PreferenceUpdater(PreferenceKeys.STARTUP_VIEW,
                        ListSummaryAction(R.string.prefStartupViewSummary, R.array.startupViewsValues, R.array.startupViews)),
                PreferenceUpdater(PreferenceKeys.GRAPH_DEFAULT_TIMESPAN,
                        ListSummaryAction(R.string.prefDefaultTimespanSummary, R.array.graphDefaultTimespanValues, R.array.graphDefaultTimespanEntries)),
                PreferenceUpdater(PreferenceKeys.DEVICE_LIST_RIGHT_PADDING,
                        IntSummaryAction(R.string.prefDeviceListPaddingRightSummary)),
                PreferenceUpdater(PreferenceKeys.DEVICE_COLUMN_WIDTH,
                        IntSummaryAction(R.string.prefDeviceColumnWidthSummary))
        )
    }
}