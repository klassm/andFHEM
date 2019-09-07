package li.klass.fhem.settings.updater

import android.content.Context
import android.content.SharedPreferences
import android.preference.Preference
import org.apache.commons.lang3.ArrayUtils

class ListSummaryAction(
        private val summaryTemplate: Int,
        private val valuesArrayResource: Int,
        private val textArrayResource: Int
) : SettingsUpdater.Action {
    override fun onChange(preference: Preference, sharedPreferences: SharedPreferences) {
        val currentValue = sharedPreferences.getString(preference.key, null)
        preference.summary = nameForArrayValueFormatted(valuesArrayResource, textArrayResource,
                currentValue, summaryTemplate, preference.context)
    }

    private fun nameForArrayValueFormatted(valuesArrayResource: Int, textArrayResource: Int,
                                           value: String?, summaryTemplate: Int,
                                           context: Context): String =
            String.format(context.getString(summaryTemplate), nameForArrayValue(valuesArrayResource, textArrayResource, value, context))

    private fun nameForArrayValue(valuesArrayResource: Int, textArrayResource: Int, value: String?,
                                  context: Context): String? {
        val index = ArrayUtils.indexOf(context.resources.getStringArray(valuesArrayResource), value)
        if (index == -1) {
            return null
        }

        return context.resources.getStringArray(textArrayResource)[index]
    }
}