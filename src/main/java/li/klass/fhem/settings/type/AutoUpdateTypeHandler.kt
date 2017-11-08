package li.klass.fhem.settings.type

import li.klass.fhem.R
import li.klass.fhem.settings.SettingsKeys
import li.klass.fhem.settings.updater.ListSummaryAction
import li.klass.fhem.settings.updater.SettingsUpdater
import javax.inject.Inject

class AutoUpdateTypeHandler @Inject constructor()
    : SettingsTypeHandler("auto_update", updateHandlers) {

    override fun getResource(): Int = R.xml.settings_auto_update

    companion object {
        val updateHandlers = listOf(
                SettingsUpdater(SettingsKeys.AUTO_UPDATE_TIME,
                        ListSummaryAction(R.string.settingsAutoUpdateSummary, R.array.updateRoomListTimeValues, R.array.updateRoomListTimeEntries))
        )
    }
}