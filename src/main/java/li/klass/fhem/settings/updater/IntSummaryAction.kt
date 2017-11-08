package li.klass.fhem.settings.updater

import android.content.SharedPreferences
import android.preference.Preference

class IntSummaryAction(
        private val summaryTemplate: Int
) : PreferenceUpdater.Action {
    override fun onChange(preference: Preference, sharedPreferences: SharedPreferences) {
        val template = preference.context.getString(summaryTemplate)
        preference.summary = String.format(template, sharedPreferences.getInt(preference.key, 0))
    }
}