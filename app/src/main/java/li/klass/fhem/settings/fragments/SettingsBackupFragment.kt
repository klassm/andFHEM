package li.klass.fhem.settings.fragments

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.Preference.OnPreferenceClickListener
import androidx.preference.PreferenceFragmentCompat
import li.klass.fhem.AndFHEMApplication
import li.klass.fhem.R
import li.klass.fhem.backup.ui.ImportExportUIService
import li.klass.fhem.settings.SettingsKeys
import javax.inject.Inject

class SettingsBackupFragment : PreferenceFragmentCompat() {
    @Inject
    lateinit var importExportUIService: ImportExportUIService

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings_backup, rootKey)
        AndFHEMApplication.application?.daggerComponent?.inject(this)

        findPreference<Preference>(SettingsKeys.EXPORT_SETTINGS)?.apply {
            onPreferenceClickListener = OnPreferenceClickListener {
                activity?.let {
                    importExportUIService.handleExport(it)
                }
                true
            }
        }
        findPreference<Preference>(SettingsKeys.IMPORT_SETTINGS)?.apply {
            onPreferenceClickListener = OnPreferenceClickListener {
                activity?.let {
                    importExportUIService.handleImport(this@SettingsBackupFragment)
                }
                true
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        AndFHEMApplication.application?.daggerComponent?.inject(this)
        if (requestCode == ImportExportUIService.importBackupFilePickerRequestCode && resultCode == Activity.RESULT_OK) {
            val filePath = (data?.clipData ?: data?.data) as Uri
            activity?.let { importExportUIService.onImportFileSelected(filePath, it) }
        }
    }
}