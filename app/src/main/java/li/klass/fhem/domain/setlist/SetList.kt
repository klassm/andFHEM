/*
 * AndFHEM - Open Source Android application to control a FHEM home automation
 * server.
 *
 * Copyright (c) 2011, Matthias Klass or third-party contributors as
 * indicated by the @author tags or express copyright attribution
 * statements applied by the authors.  All third-party contributions are
 * distributed under license by Red Hat Inc.
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU GENERAL PUBLIC LICENSE, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU GENERAL PUBLIC LICENSE
 * for more details.
 *
 * You should have received a copy of the GNU GENERAL PUBLIC LICENSE
 * along with this distribution; if not, write to:
 *   Free Software Foundation, Inc.
 *   51 Franklin Street, Fifth Floor
 *   Boston, MA  02110-1301  USA
 */

package li.klass.fhem.domain.setlist

import li.klass.fhem.domain.setlist.typeEntry.NoArgSetListEntry
import li.klass.fhem.domain.setlist.typeEntry.NotFoundSetListEntry
import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.StringUtils.isEmpty
import java.io.Serializable

class SetList constructor(val entries: Map<String, SetListEntry>) : Serializable {

    val sortedKeys: List<String>
        get() = entries.keys.sorted()

    operator fun get(key: String, ignoreCase: Boolean = true): SetListEntry {
        val matches = entries.filter { it.key.equals(key, ignoreCase) }
        return if (matches.isEmpty()) NotFoundSetListEntry(key) else matches.iterator().next().value
    }

    fun contains(vararg keys: String): Boolean = contains(keys.toList())

    fun contains(keys: Iterable<String>): Boolean =
            keys.any { entries.containsKey(it) }

    fun existingStatesOf(toSearch: Set<String>): Set<String> =
            entries.keys.filter { toSearch.contains(it) }.toSet()

    fun size(): Int = entries.size

    fun isEmpty(): Boolean = size() == 0

    override fun toString(): String {
        val keys = sortedKeys

        return keys
                .map { entries[it] as SetListEntry }
                .map { it.asText() }
                .joinToString(separator = " ")
    }

    fun getFirstPresentStateOf(vararg states: String): String? =
            states.asSequence().firstOrNull { contains(it) }

    companion object {
        fun parse(inputText: String): SetList {
            if (isEmpty(inputText)) return SetList(emptyMap())

            val parts = inputText.trim()
                    .split(" ".toRegex()).toTypedArray()

            return SetList(parts.map { handlePart(it) }
                    .filterNotNull()
                    .toMap())
        }

        private fun handlePart(part: String): Pair<String, SetListEntry>? {
            var myPart = part
            if (myPart.matches("[^:]+:noArg$".toRegex())) {
                myPart = myPart.replace(":noArg$".toRegex(), "")
            }
            if (!myPart.contains(":")) {
                return Pair(myPart, NoArgSetListEntry(myPart))
            }

            val keyValue = myPart.split(":".toRegex(), 2).toTypedArray()

            var key: String? = StringUtils.trimToNull(keyValue[0])
            key = if (key == null) "state" else key
            val value = if (keyValue.size == 2) keyValue[1] else ""
            if (StringUtils.isEmpty(value)) return null

            return handle(key, value)?.let { Pair(key, it) }
        }

        private fun handle(key: String, value: String): SetListItem? {
            val parts = value.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toList()

            val type = findType(parts)
            return type.getSetListItemFor(key, parts)
        }

        private fun findType(parts: List<String>): SetListItemType {
            for (type in SetListItemType.values()) {
                if (type.supports(parts)) {
                    return type
                }
            }
            return if (parts.isEmpty() || parts.isNotEmpty() && "colorpicker".equals(parts[0], ignoreCase = true))
                SetListItemType.NO_ARG
            else
                SetListItemType.GROUP
        }
    }
}