package li.klass.fhem.settings

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.preference.PreferenceActivity
import com.nbsp.materialfilepicker.ui.FilePickerActivity
import li.klass.fhem.AndFHEMApplication
import li.klass.fhem.R
import li.klass.fhem.activities.ThemeInitializer
import li.klass.fhem.backup.ui.ImportExportUIService
import li.klass.fhem.constants.Actions
import javax.inject.Inject

class SettingsActivity : PreferenceActivity() {

    @Inject
    lateinit var themeInitializer: ThemeInitializer

    @Inject
    lateinit var importExportUIService: ImportExportUIService

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        AndFHEMApplication.application?.daggerComponent?.inject(this)
        themeInitializer.init()

        super.onCreate(savedInstanceState, persistentState)
    }

    override fun onBuildHeaders(target: MutableList<Header>?) {
        loadHeadersFromResource(R.xml.settings_headers, target);
    }

    override fun onStop() {
        super.onStop()
        sendBroadcast(Intent(Actions.REDRAW))
    }

    override fun isValidFragment(fragmentName: String?) = true

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        AndFHEMApplication.application?.daggerComponent?.inject(this)
        if (requestCode == ImportExportUIService.importBackupFilePickerRequestCode && resultCode == Activity.RESULT_OK) {
            val filePath = data!!.getStringExtra(FilePickerActivity.RESULT_FILE_PATH)
            importExportUIService.onImportFileSelected(filePath!!, this)
        }
    }
}