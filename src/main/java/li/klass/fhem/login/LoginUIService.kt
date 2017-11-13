package li.klass.fhem.login

import android.content.Context
import li.klass.fhem.settings.SettingsKeys
import li.klass.fhem.util.ApplicationProperties
import li.klass.fhem.util.DateTimeProvider
import li.klass.fhem.util.preferences.SharedPreferencesService
import org.apache.commons.lang3.StringUtils
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.Minutes
import org.joda.time.Minutes.minutes
import javax.inject.Inject

class LoginUIService @Inject constructor(
        val applicationProperties: ApplicationProperties,
        val sharedPreferencesService: SharedPreferencesService,
        val dateTimeProvider: DateTimeProvider
) {
    fun doLoginIfRequired(context: Context, loginStrategy: LoginStrategy) {
        val requiredPassword = readPassword()
        val lastLogin = readLastLogin(context)
        val now = dateTimeProvider.now()
        val isStillLoggedIn = Minutes.minutesBetween(lastLogin, now).isLessThan(loginTime)

        if (requiredPassword == null || isStillLoggedIn) {
            loginStrategy.onLoginSuccess()
            return
        }

        loginStrategy.requireLogin(context, { enteredPassword ->
            checkPassword(enteredPassword, requiredPassword, loginStrategy, context)
        })

    }

    private fun checkPassword(enteredPassword: String, requiredPassword: String, loginStrategy: LoginStrategy, context: Context) {
        val isCorrect = enteredPassword == requiredPassword

        if (isCorrect) {
            sharedPreferencesService.getSharedPreferencesEditor(sharedPreferenceName, context)
                    .putLong(lastLogin, System.currentTimeMillis()).apply()
            loginStrategy.onLoginSuccess()
        } else {
            loginStrategy.onLoginFailure()
        }
    }

    private fun readLastLogin(context: Context) = DateTime(sharedPreferencesService.getPreferences(sharedPreferenceName, context).getLong(lastLogin, 0), DateTimeZone.UTC)

    private fun readPassword(): String? =
            StringUtils.trimToNull(applicationProperties.getStringSharedPreference(SettingsKeys.STARTUP_PASSWORD, null))

    companion object {
        val sharedPreferenceName = "login"
        val lastLogin = "lastLogin"
        val loginTime = minutes(3)
    }

    interface LoginStrategy {
        fun requireLogin(context: Context, checkLogin: (String) -> Unit)

        fun onLoginSuccess()

        fun onLoginFailure()
    }
}