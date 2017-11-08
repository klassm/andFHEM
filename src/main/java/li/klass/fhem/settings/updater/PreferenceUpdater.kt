package li.klass.fhem.settings.updater

import android.content.SharedPreferences
import android.preference.Preference

class PreferenceUpdater(val preferenceKey: String, val action: Action) {

    fun canHandle(preferenceKey: String) = preferenceKey == this.preferenceKey

    fun onChange(preference: Preference, sharedPreferences: SharedPreferences) {
        action.onChange(preference, sharedPreferences)
    }

    interface Action {
        fun onChange(preference: Preference, sharedPreferences: SharedPreferences)
    }
}