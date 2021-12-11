package li.klass.fhem.settings.fragments

import android.os.Bundle
import android.widget.Toast
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SeekBarPreference
import de.duenndns.ssl.MemorizingTrustManager
import li.klass.fhem.R
import li.klass.fhem.connection.backend.FHEMConnection
import li.klass.fhem.settings.SettingsKeys
import li.klass.fhem.update.backend.command.execution.CommandExecutionService
import java.security.KeyStoreException
import java.util.logging.Level
import java.util.logging.Logger

class SettingsConnectionsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings_connection, rootKey)

        findPreference<Preference>(SettingsKeys.CLEAR_TRUSTED_CERTIFICATES)?.apply {
            setOnPreferenceClickListener {
                val mtm = MemorizingTrustManager(context)
                mtm.certificates.iterator().forEach {
                    try {
                        mtm.deleteCertificate(it)
                        logger.log(Level.INFO, "Deleting certificate for {} ", it)
                    } catch (e: KeyStoreException) {
                        logger.log(Level.SEVERE, "Could not delete certificate", e)
                    }
                }
                Toast.makeText(
                    context,
                    context.getString(R.string.settingsClearTrustedCertificatesFinished),
                    Toast.LENGTH_SHORT
                ).show()
                true
            }

        }

        findPreference<SeekBarPreference>(SettingsKeys.CONNECTION_TIMEOUT)?.apply {
            min = 1
            setDefaultValue(FHEMConnection.CONNECTION_TIMEOUT_DEFAULT_SECONDS)
            showSeekBarValue = true
        }
        findPreference<SeekBarPreference>(SettingsKeys.COMMAND_EXECUTION_RETRIES)?.apply {
            showSeekBarValue = true
            setDefaultValue(CommandExecutionService.DEFAULT_NUMBER_OF_RETRIES)
        }

    }

    companion object {
        private val logger = Logger.getLogger(SettingsConnectionsFragment::class.java.name)
    }
}