package li.klass.fhem.settings.type

import li.klass.fhem.R
import li.klass.fhem.constants.PreferenceKeys
import li.klass.fhem.settings.updater.ListSummaryAction
import li.klass.fhem.settings.updater.PreferenceUpdater
import javax.inject.Inject

class AutoUpdateTypeHandler @Inject constructor()
    : SettingsTypeHandler("auto_update", updateHandlers) {

    override fun getResource(): Int = R.xml.preferences_auto_update

    companion object {
        val updateHandlers = listOf(
                PreferenceUpdater(PreferenceKeys.AUTO_UPDATE_TIME,
                        ListSummaryAction(R.string.prefAutoUpdateSummary, R.array.updateRoomListTimeValues, R.array.updateRoomListTimeEntries))
        )
    }
}