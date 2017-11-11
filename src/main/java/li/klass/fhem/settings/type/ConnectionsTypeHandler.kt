package li.klass.fhem.settings.type

import android.app.Activity
import android.content.SharedPreferences
import android.preference.Preference
import android.widget.Toast
import de.duenndns.ssl.MemorizingTrustManager
import li.klass.fhem.R
import li.klass.fhem.connection.backend.FHEMConnection
import li.klass.fhem.service.CommandExecutionService
import li.klass.fhem.settings.SettingsKeys
import li.klass.fhem.settings.updater.IntSummaryAction
import li.klass.fhem.settings.updater.SettingsUpdater
import li.klass.fhem.widget.preference.SeekBarPreference
import java.security.KeyStoreException
import java.util.logging.Level
import java.util.logging.Logger
import javax.inject.Inject

class ConnectionsTypeHandler @Inject constructor()
    : SettingsTypeHandler("connection", updateHandlers) {

    override fun getResource(): Int = R.xml.settings_connection

    override fun initWith(sharedPreferences: SharedPreferences, preferenceFinder: (String) -> Preference, activity: Activity) {
        preferenceFinder(SettingsKeys.CLEAR_TRUSTED_CERTIFICATES).onPreferenceClickListener = Preference.OnPreferenceClickListener {
            val mtm = MemorizingTrustManager(it.context)
            mtm.certificates.iterator().forEach {
                try {
                    mtm.deleteCertificate(it)
                    logger.log(Level.INFO, "Deleting certificate for {} ", it)
                } catch (e: KeyStoreException) {
                    logger.log(Level.SEVERE, "Could not delete certificate", e)
                }
            }
            Toast.makeText(it.context, it.context.getString(R.string.settingsClearTrustedCertificatesFinished), Toast.LENGTH_SHORT).show()
            true
        }
        val connectionTimeoutPreference = preferenceFinder(SettingsKeys.CONNECTION_TIMEOUT) as SeekBarPreference
        connectionTimeoutPreference.setMinimumValue(1)
        connectionTimeoutPreference.setDefaultValue(FHEMConnection.CONNECTION_TIMEOUT_DEFAULT_SECONDS)

        val commandExecutionRetriesPreference = preferenceFinder(SettingsKeys.COMMAND_EXECUTION_RETRIES) as SeekBarPreference
        commandExecutionRetriesPreference.setDefaultValue(CommandExecutionService.DEFAULT_NUMBER_OF_RETRIES)
    }

    companion object {
        val updateHandlers = listOf(
                SettingsUpdater(SettingsKeys.CONNECTION_TIMEOUT,
                        IntSummaryAction(R.string.settingsConnectionTimeoutSummary)),
                SettingsUpdater(SettingsKeys.COMMAND_EXECUTION_RETRIES,
                        IntSummaryAction(R.string.settingsCommandExecutionRetriesSummary))
        )

        private val logger = Logger.getLogger(ConnectionsTypeHandler::class.java.name)
    }
}