package li.klass.fhem.settings.type

import android.app.Activity
import android.content.SharedPreferences
import android.preference.Preference
import li.klass.fhem.settings.updater.PreferenceUpdater

abstract class SettingsTypeHandler(private val fragmentKey: String,
                                   private val preferenceUpdaters: List<PreferenceUpdater> = emptyList()
) {
    abstract fun getResource(): Int

    fun canHandle(fragmentKey: String) = this.fragmentKey == fragmentKey

    fun onPreferenceChange(sharedPreferences: SharedPreferences, preference: Preference) {
        preferenceUpdaters.find { it.canHandle(preference.key) }
                ?.onChange(preference, sharedPreferences)
    }

    fun initializeWith(sharedPreferences: SharedPreferences, preferenceFinder: (String) -> Preference, activity: Activity) {
        preferenceUpdaters.forEach { it.onChange(preferenceFinder(it.preferenceKey), sharedPreferences) }

        initWith(sharedPreferences, preferenceFinder, activity)
    }

    open fun initWith(sharedPreferences: SharedPreferences, preferenceFinder: (String) -> Preference, activity: Activity) {}
}