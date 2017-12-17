package li.klass.fhem.domain

import java.io.Serializable

class EventMap(internal val map: Map<String, String>) : Serializable {
    fun getValueFor(key: String): String? = map[key]

    fun getOr(key: String, default: String): String = map[key] ?: default

    fun getKeyFor(value: String): String? = map.entries.find({ e -> e.value == value })?.key

    fun getKeyOr(value: String, default: String): String? = getKeyFor(value) ?: default

    fun put(key: String, value: String): EventMap = EventMap(map.plus(Pair(key, value)))

    fun contains(key: String): Boolean = map.contains(key)
}