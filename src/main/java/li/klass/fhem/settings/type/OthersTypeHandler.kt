package li.klass.fhem.settings.type

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.preference.Preference
import android.provider.Settings
import li.klass.fhem.R
import li.klass.fhem.settings.SettingsKeys
import javax.inject.Inject

class OthersTypeHandler @Inject constructor()
    : SettingsTypeHandler("others") {

    override fun getResource(): Int = R.xml.settings_others

    override fun initWith(sharedPreferences: SharedPreferences, preferenceFinder: (String) -> Preference, activity: Activity) {
        val voiceCommands = preferenceFinder(SettingsKeys.VOICE_COMMANDS)
        voiceCommands.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            activity.startActivity(intent)
            true
        }
    }
}