package li.klass.fhem.settings.type

import android.app.Activity
import android.content.SharedPreferences
import android.preference.Preference
import li.klass.fhem.R
import li.klass.fhem.settings.SettingsKeys
import li.klass.fhem.ui.service.importExport.ImportExportUIService
import javax.inject.Inject

class BackupTypeHandler @Inject constructor(
        private val importExportUIService: ImportExportUIService
) : SettingsTypeHandler("backup") {

    override fun getResource(): Int = R.xml.settings_backup

    override fun initWith(sharedPreferences: SharedPreferences, preferenceFinder: (String) -> Preference, activity: Activity) {
        val exportPreference = preferenceFinder(SettingsKeys.EXPORT_SETTINGS)
        exportPreference.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            importExportUIService.handleExport(activity)
            true
        }

        val importPreference = preferenceFinder(SettingsKeys.IMPORT_SETTINGS)
        importPreference.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            importExportUIService.handleImport(activity)
            true
        }
    }
}