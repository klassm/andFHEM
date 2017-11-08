package li.klass.fhem.settings

import android.content.Intent
import android.preference.PreferenceActivity
import li.klass.fhem.R
import li.klass.fhem.constants.Actions

class SettingsActivity : PreferenceActivity() {
    override fun onBuildHeaders(target: MutableList<Header>?) {
        loadHeadersFromResource(R.xml.preferences_headers, target);
    }

    override fun onStop() {
        super.onStop()
        sendBroadcast(Intent(Actions.REDRAW))
    }

    override fun isValidFragment(fragmentName: String?) = true
}