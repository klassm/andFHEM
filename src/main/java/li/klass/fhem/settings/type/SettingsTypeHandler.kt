package li.klass.fhem.settings.type

import android.app.Activity
import android.content.SharedPreferences
import android.preference.Preference
import li.klass.fhem.settings.updater.SettingsUpdater

abstract class SettingsTypeHandler(private val fragmentKey: String,
                                   private val settingsUpdaters: List<SettingsUpdater> = emptyList()
) {
    abstract fun getResource(): Int

    fun canHandle(fragmentKey: String) = this.fragmentKey == fragmentKey

    fun onPreferenceChange(sharedPreferences: SharedPreferences, preference: Preference) {
        settingsUpdaters.find { it.canHandle(preference.key) }
                ?.onChange(preference, sharedPreferences)
    }

    fun initializeWith(sharedPreferences: SharedPreferences, preferenceFinder: (String) -> Preference, activity: Activity) {
        settingsUpdaters.forEach { it.onChange(preferenceFinder(it.preferenceKey), sharedPreferences) }

        initWith(sharedPreferences, preferenceFinder, activity)
    }

    open fun initWith(sharedPreferences: SharedPreferences, preferenceFinder: (String) -> Preference, activity: Activity) {}
}