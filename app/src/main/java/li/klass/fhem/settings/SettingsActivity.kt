package li.klass.fhem.settings

import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.os.PersistableBundle
import android.preference.PreferenceActivity
import li.klass.fhem.AndFHEMApplication
import li.klass.fhem.R
import li.klass.fhem.activities.ThemeInitializer
import li.klass.fhem.constants.Actions
import javax.inject.Inject

class SettingsActivity : PreferenceActivity() {

    @Inject
    lateinit var themeInitializer: ThemeInitializer

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

    override fun onApplyThemeResource(theme: Resources.Theme?, resid: Int, first: Boolean) {
        super.onApplyThemeResource(theme, resid, first)
    }

    override fun isValidFragment(fragmentName: String?) = true
}