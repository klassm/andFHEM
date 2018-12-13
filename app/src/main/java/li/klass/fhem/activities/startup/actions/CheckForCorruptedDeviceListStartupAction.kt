package li.klass.fhem.activities.startup.actions

import li.klass.fhem.R
import li.klass.fhem.update.backend.DeviceListUpdateService
import javax.inject.Inject

class CheckForCorruptedDeviceListStartupAction @Inject constructor(
        private val deviceListUpdateService: DeviceListUpdateService
) : StartupAction(R.string.currentStatus_checkForCorruptedDeviceList) {
    override suspend fun run() {
        deviceListUpdateService.checkForCorruptedDeviceList()
    }
}