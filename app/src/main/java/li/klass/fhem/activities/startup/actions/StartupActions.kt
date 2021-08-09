package li.klass.fhem.activities.startup.actions

import javax.inject.Inject

class StartupActions @Inject constructor(
    checkForCorruptedDeviceListStartupAction: CheckForCorruptedDeviceListStartupAction,
    deleteOldFcmMessagesStartupAction: DeleteOldFcmMessagesStartupAction,
    deviceListUpdateStartupAction: DeviceListUpdateStartupAction
) {
    val actions = listOf(
        deleteOldFcmMessagesStartupAction,
        checkForCorruptedDeviceListStartupAction,
        deviceListUpdateStartupAction
    )
}