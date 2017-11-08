package li.klass.fhem.settings.type

import android.app.Activity
import android.content.SharedPreferences
import android.preference.Preference
import li.klass.fhem.R
import li.klass.fhem.constants.PreferenceKeys
import li.klass.fhem.error.ErrorHolder
import javax.inject.Inject

class ErrorsTypeHandler @Inject constructor()
    : SettingsTypeHandler("errors") {

    override fun initWith(sharedPreferences: SharedPreferences, preferenceFinder: (String) -> Preference, activity: Activity) {

        preferenceFinder(PreferenceKeys.SEND_LAST_ERROR).onPreferenceClickListener = Preference.OnPreferenceClickListener {
            ErrorHolder.sendLastErrorAsMail(it.context)
            true
        }

        preferenceFinder(PreferenceKeys.SEND_APP_LOG).onPreferenceClickListener = Preference.OnPreferenceClickListener {
            ErrorHolder.sendApplicationLogAsMail(it.context)
            true
        }
    }

    override fun getResource(): Int = R.xml.preferences_errors
}