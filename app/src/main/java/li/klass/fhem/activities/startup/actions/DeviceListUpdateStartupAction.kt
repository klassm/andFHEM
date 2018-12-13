package li.klass.fhem.activities.startup.actions

import li.klass.fhem.R
import li.klass.fhem.appwidget.update.AppWidgetUpdateService
import li.klass.fhem.settings.SettingsKeys
import li.klass.fhem.update.backend.DeviceListUpdateService
import li.klass.fhem.util.ApplicationProperties
import org.slf4j.LoggerFactory
import javax.inject.Inject

class DeviceListUpdateStartupAction @Inject constructor(
        private val deviceListUpdateService: DeviceListUpdateService,
        private val applicationProperties: ApplicationProperties,
        private val appWidgetUpdateService: AppWidgetUpdateService
) : StartupAction(R.string.currentStatus_loadingDeviceList) {
    override suspend fun run() {
        val updateOnApplicationStart = applicationProperties.getBooleanSharedPreference(SettingsKeys.UPDATE_ON_APPLICATION_START, false)
        if (updateOnApplicationStart) {
            executeRemoteUpdate()
        }
        deviceListUpdateService.checkForCorruptedDeviceList()
    }

    private fun executeRemoteUpdate() {
        val result = deviceListUpdateService.updateAllDevices()
        appWidgetUpdateService.updateAllWidgets()

        when (result) {
            is DeviceListUpdateService.UpdateResult.Success -> {
                logger.debug("executeRemoteUpdate() : device list was loaded")
            }
            else -> {
                logger.error("executeRemoteUpdate() : cannot load device list")
            }
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(DeviceListUpdateStartupAction::class.java)
    }
}