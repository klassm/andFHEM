package li.klass.fhem.settings.type

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.preference.Preference
import android.provider.Settings
import android.widget.Toast
import de.duenndns.ssl.MemorizingTrustManager
import li.klass.fhem.R
import li.klass.fhem.constants.PreferenceKeys
import li.klass.fhem.fhem.FHEMConnection
import li.klass.fhem.service.CommandExecutionService
import li.klass.fhem.settings.updater.IntSummaryAction
import li.klass.fhem.settings.updater.ListSummaryAction
import li.klass.fhem.settings.updater.PreferenceUpdater
import li.klass.fhem.settings.updater.StringSummaryAction
import li.klass.fhem.ui.service.importExport.ImportExportUIService
import li.klass.fhem.widget.preference.SeekBarPreference
import java.security.KeyStoreException
import java.util.logging.Level
import java.util.logging.Logger
import javax.inject.Inject

class OthersTypeHandler @Inject constructor(
        private val importExportUIService: ImportExportUIService
) : SettingsTypeHandler("others", updateHandlers) {

    override fun getResource(): Int = R.xml.preferences_others

    override fun initWith(sharedPreferences: SharedPreferences, preferenceFinder: (String) -> Preference, activity: Activity) {
        preferenceFinder(PreferenceKeys.CLEAR_TRUSTED_CERTIFICATES).onPreferenceClickListener = Preference.OnPreferenceClickListener {
            val mtm = MemorizingTrustManager(it.context)
            mtm.certificates.iterator().forEach {
                try {
                    mtm.deleteCertificate(it)
                    logger.log(Level.INFO, "Deleting certificate for {} ", it)
                } catch (e: KeyStoreException) {
                    logger.log(Level.SEVERE, "Could not delete certificate", e)
                }
            }
            Toast.makeText(it.context, it.context.getString(R.string.prefClearTrustedCertificatesFinished), Toast.LENGTH_SHORT).show()
            true
        }

        val exportPreference = preferenceFinder(PreferenceKeys.EXPORT_SETTINGS)
        exportPreference.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            importExportUIService.handleExport(activity)
            true
        }

        val importPreference = preferenceFinder(PreferenceKeys.IMPORT_SETTINGS)
        importPreference.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            importExportUIService.handleImport(activity)
            true
        }

        val connectionTimeoutPreference = preferenceFinder(PreferenceKeys.CONNECTION_TIMEOUT) as SeekBarPreference
        connectionTimeoutPreference.setMinimumValue(1)
        connectionTimeoutPreference.setDefaultValue(FHEMConnection.CONNECTION_TIMEOUT_DEFAULT_SECONDS)

        val commandExecutionRetriesPreference = preferenceFinder(PreferenceKeys.COMMAND_EXECUTION_RETRIES) as SeekBarPreference
        commandExecutionRetriesPreference.setDefaultValue(CommandExecutionService.Companion.DEFAULT_NUMBER_OF_RETRIES)

        val voiceCommands = preferenceFinder(PreferenceKeys.VOICE_COMMANDS)
        voiceCommands.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            activity.startActivity(intent)
            true
        }
    }

    companion object {
        val updateHandlers = listOf(
                PreferenceUpdater(PreferenceKeys.AUTO_UPDATE_TIME,
                        ListSummaryAction(R.string.prefAutoUpdateSummary, R.array.updateRoomListTimeValues, R.array.updateRoomListTimeEntries)),
                PreferenceUpdater(PreferenceKeys.CONNECTION_TIMEOUT,
                        IntSummaryAction(R.string.prefConnectionTimeoutSummary)),
                PreferenceUpdater(PreferenceKeys.COMMAND_EXECUTION_RETRIES,
                        IntSummaryAction(R.string.prefCommandExecutionRetriesSummary)),
                PreferenceUpdater(PreferenceKeys.FHEMWEB_DEVICE_NAME,
                        StringSummaryAction(R.string.prefFHEMWEBDeviceNameSummary))
        )

        private val logger = Logger.getLogger(OthersTypeHandler::class.java.name)
    }
}