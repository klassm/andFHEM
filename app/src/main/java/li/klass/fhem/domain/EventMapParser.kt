package li.klass.fhem.domain

object EventMapParser {
    fun parseEventMap(content: String): EventMap = EventMap(parse(content))

    fun parse(content: String): Map<String, String> {
        val delimiter = determineDelimiter(content)

        return content.split(delimiter)
                .map { part -> part.split(":") }
                .filter { part -> part.size == 2 }
                .map { part -> Pair(part[0], part[1]) }
                .toMap()
    }

    private fun determineDelimiter(content: String): String = when {
        content.startsWith("/") -> "/"
        content.startsWith(",") -> ","
        else -> " "
    }
}