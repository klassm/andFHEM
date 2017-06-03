package li.klass.fhem.domain

import java.io.Serializable

class EventMap(internal val map: Map<String, String>) : Serializable {
    fun getValueFor(key: String): String? {
        return map.get(key)
    }

    fun getOr(key: String, default: String): String {
        return map.get(key) ?: default
    }

    fun getKeyFor(value: String): String? {
        return map.entries.find({ e -> e.value == value })?.key
    }

    fun getKeyOr(value: String, default: String): String? {
        return getKeyFor(value) ?: default
    }

    fun put(key: String, value: String): EventMap {
        return EventMap(map.plus(Pair(key, value)))
    }

    fun contains(key: String): Boolean {
        return map.contains(key)
    }
}