package li.klass.fhem.activities.startup.actions

import javax.inject.Inject

class StartupActions @Inject constructor(
        checkForCorruptedDeviceListStartupAction: CheckForCorruptedDeviceListStartupAction,
        deleteOldFcmMessagesStartupAction: DeleteOldFcmMessagesStartupAction,
        googleBillingStartupAction: GoogleBillingStartupAction,
        deviceListUpdateStartupAction: DeviceListUpdateStartupAction
) {
    val actions = listOf(
            googleBillingStartupAction,
            deleteOldFcmMessagesStartupAction,
            checkForCorruptedDeviceListStartupAction,
            deviceListUpdateStartupAction
    )
}