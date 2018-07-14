package li.klass.fhem.activities.startup.actions

import li.klass.fhem.R
import li.klass.fhem.fcm.history.data.FcmHistoryService
import li.klass.fhem.settings.SettingsKeys
import li.klass.fhem.util.ApplicationProperties
import javax.inject.Inject

class DeleteOldFcmMessagesStartupAction @Inject constructor(
        private val applicationProperties: ApplicationProperties,
        private val fcmHistoryService: FcmHistoryService
) : StartupAction(R.string.currentStatus_deleteFcmHistory) {

    override fun run() {
        val retentionDays = Integer.parseInt(applicationProperties.getStringSharedPreference(SettingsKeys.FCM_KEEP_MESSAGES_DAYS, "-1"))
        fcmHistoryService.deleteContentOlderThan(retentionDays)
    }
}