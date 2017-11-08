package li.klass.fhem.settings.type

import li.klass.fhem.R
import li.klass.fhem.constants.PreferenceKeys
import li.klass.fhem.settings.updater.ListSummaryAction
import li.klass.fhem.settings.updater.PreferenceUpdater
import javax.inject.Inject

class WidgetTypeHandler @Inject constructor()
    : SettingsTypeHandler("widgets", updateListeners) {

    override fun getResource(): Int = R.xml.preferences_widgets

    companion object {
        val updateListeners = listOf(
                PreferenceUpdater(PreferenceKeys.WIDGET_UPDATE_INTERVAL_WLAN,
                        ListSummaryAction(R.string.prefWidgetUpdateTimeWLANSummary, R.array.widgetUpdateTimeValues, R.array.widgetUpdateTimeEntries)),
                PreferenceUpdater(PreferenceKeys.WIDGET_UPDATE_INTERVAL_MOBILE,
                        ListSummaryAction(R.string.prefWidgetUpdateTimeMobileSummary, R.array.widgetUpdateTimeValues, R.array.widgetUpdateTimeEntries))
        )
    }
}