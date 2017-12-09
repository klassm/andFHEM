package li.klass.fhem.settings.type

import li.klass.fhem.R
import li.klass.fhem.settings.SettingsKeys
import li.klass.fhem.settings.updater.ListSummaryAction
import li.klass.fhem.settings.updater.SettingsUpdater
import javax.inject.Inject

class WidgetTypeHandler @Inject constructor()
    : SettingsTypeHandler("widgets", updateListeners) {

    override fun getResource(): Int = R.xml.settings_widgets

    companion object {
        val updateListeners = listOf(
                SettingsUpdater(SettingsKeys.WIDGET_UPDATE_INTERVAL_WLAN,
                        ListSummaryAction(R.string.settingsWidgetUpdateTimeWLANSummary, R.array.widgetUpdateTimeValues, R.array.widgetUpdateTimeEntries)),
                SettingsUpdater(SettingsKeys.WIDGET_UPDATE_INTERVAL_MOBILE,
                        ListSummaryAction(R.string.settingsWidgetUpdateTimeMobileSummary, R.array.widgetUpdateTimeValues, R.array.widgetUpdateTimeEntries))
        )
    }
}