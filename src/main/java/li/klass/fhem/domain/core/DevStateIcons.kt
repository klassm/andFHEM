package li.klass.fhem.domain.core

class DevStateIcons(val definitions: Map<Regex, String>) {

    fun iconFor(value: String): String? {
        return definitions
                .filter { it.key.matches(value) }
                .map { it.value }
                .firstOrNull()
    }

    companion object {
        fun parse(text: String?): DevStateIcons {
            val definitions = (text ?: "").split(" ")
                    .map { it.split(":") }
                    .filter { it.size == 2 }
                    .map { Pair(Regex(it[0]), it[1]) }
                    .toMap()
            return DevStateIcons(definitions)
        }
    }
}