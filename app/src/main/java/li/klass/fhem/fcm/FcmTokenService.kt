package li.klass.fhem.fcm

import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.messaging.FirebaseMessaging
import li.klass.fhem.AndFHEMApplication
import li.klass.fhem.settings.SettingsKeys
import li.klass.fhem.util.ApplicationProperties
import li.klass.fhem.util.Logging
import li.klass.fhem.util.awaitCallback
import javax.inject.Inject

class FcmTokenService @Inject constructor(
        val applicationProperties: ApplicationProperties
) : Logging {
    var app: FirebaseApp? = null

    init {
        applicationProperties.listen(SettingsKeys.FCM_SENDER_ID) {
            createApp()
        }
        createApp()
    }

    private fun createApp() {
        if (app != null) {
            app?.delete()
            app = null
        }
        val senderId = applicationProperties.getStringSharedPreference(SettingsKeys.FCM_SENDER_ID)
        if (senderId == null) {
            logger.info("getRegistrationId - no value for senderId found")
        }
        val options = FirebaseOptions.Builder(FirebaseApp.getInstance().options)
                .setGcmSenderId(senderId)
                .build()
        app = FirebaseApp.initializeApp(AndFHEMApplication.application!!, options, "andFHEM_fcm")
    }

    suspend fun getRegistrationId(): String? {
        val messaging = app?.get(FirebaseMessaging::class.java) ?: return null
        return awaitCallback { callback ->
            val task = messaging.token
            task.addOnSuccessListener {
                callback.onComplete(it)
            }
            task.addOnFailureListener {
                callback.onException(it)
            }
        }
    }
}