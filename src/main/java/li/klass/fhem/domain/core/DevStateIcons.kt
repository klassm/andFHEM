package li.klass.fhem.domain.core

import java.io.Serializable
import java.util.regex.Pattern

class DevStateIcons(val definitions: Map<Pattern, DevStateIcon>) : Serializable {

    fun iconFor(value: String): DevStateIcon? {
        return definitions
                .filter { it.key.matcher(value).matches() }
                .map { it.value }
                .firstOrNull()
    }


    fun anyNoFhemwebLinkOf(states: Iterable<String>): Boolean =
            states.map { iconFor(it) }
                    .filterNotNull()
                    .any { it.noFhemWebLink }

    data class DevStateIcon(val image: String, val noFhemWebLink: Boolean) : Serializable

    companion object {
        fun parse(text: String?): DevStateIcons {
            val definitions = (text ?: "").split(" ")
                    .map { it.split(":") }
                    .filter { it.size >= 2 }
                    .map {
                        Pair(Pattern.compile(it[0]), DevStateIcon(it[1],
                                noFhemWebLink = it.elementAtOrNull(2) == "noFhemwebLink"))
                    }
                    .toMap()
            return DevStateIcons(definitions)
        }
    }
}