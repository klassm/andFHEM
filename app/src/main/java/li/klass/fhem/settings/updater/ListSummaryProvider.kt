package li.klass.fhem.settings.updater

import android.content.Context
import androidx.preference.ListPreference
import androidx.preference.Preference.SummaryProvider
import androidx.preference.PreferenceManager
import org.apache.commons.lang3.ArrayUtils

class ListSummaryProvider(
    private val summaryTemplate: Int,
    private val valuesArrayResource: Int,
    private val textArrayResource: Int
) : SummaryProvider<ListPreference> {
    override fun provideSummary(preference: ListPreference): CharSequence {
        val currentValue = PreferenceManager.getDefaultSharedPreferences(preference.context)
            .getString(preference.key, null)
        return nameForArrayValueFormatted(
            valuesArrayResource, textArrayResource,
            currentValue, summaryTemplate, preference.context
        )
    }

    private fun nameForArrayValueFormatted(
        valuesArrayResource: Int, textArrayResource: Int,
        value: String?, summaryTemplate: Int,
        context: Context
    ): String =
        String.format(
            context.getString(summaryTemplate),
            nameForArrayValue(valuesArrayResource, textArrayResource, value, context)
        )

    private fun nameForArrayValue(
        valuesArrayResource: Int, textArrayResource: Int, value: String?,
        context: Context
    ): String? {
        val index = ArrayUtils.indexOf(context.resources.getStringArray(valuesArrayResource), value)
        if (index == -1) {
            return null
        }

        return context.resources.getStringArray(textArrayResource)[index]
    }
}