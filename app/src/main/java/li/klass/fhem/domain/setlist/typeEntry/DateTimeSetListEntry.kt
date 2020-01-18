package li.klass.fhem.domain.setlist.typeEntry

import li.klass.fhem.domain.setlist.SetListItem
import li.klass.fhem.domain.setlist.SetListItemType
import java.io.Serializable

data class Config(val timePicker: Boolean,
                  val datePicker: Boolean,
                  val format: String,
                  val step: Int
) : Serializable

class DateTimeSetListEntry(key: String?, val config: Config) : SetListItem(key, SetListItemType.DATETIME) {
    override fun asText(): String = key

    companion object {
        private val formatMapping = mapOf(
                'd' to "dd",
                'm' to "MM",
                'Y' to "yyyy",
                'H' to "HH",
                'i' to "mm"
        )

        private fun mapFormat(format: String) =
                format.toCharArray().map {
                    if (it in 'a'..'z' || it in 'A'..'Z') formatMapping[it] else it.toString()
                }.filterNotNull().joinToString(separator = "")

        fun parseConfig(parts: List<String>): Config {
            val mapped = parts.map { it.split(":") }
                    .filter { it.size == 2 }
                    .map { it[0] to it[1] }
                    .toMap()
            val timePicker = (mapped["timepicker"] ?: "true") == "true"
            val datePicker = (mapped["datepicker"] ?: "true") == "true"
            val format = mapFormat(mapped["format"] ?: "d.m.Y")
            val step = (mapped["step"]?.toIntOrNull() ?: 60).let {
                if (60 % it == 0) it else 60
            }

            return Config(timePicker, datePicker = datePicker, format = format, step = step)
        }
    }
}