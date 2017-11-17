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

import com.google.common.base.Optional
import com.google.common.collect.Lists.newArrayList
import li.klass.fhem.domain.setlist.typeEntry.NoArgSetListEntry
import li.klass.fhem.domain.setlist.typeEntry.NotFoundSetListEntry
import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.StringUtils.isEmpty
import java.io.Serializable
import java.util.*

class SetList constructor(val entries: Map<String, SetListEntry>) : Serializable {

    val sortedKeys: List<String>
        get() {
            val keys = newArrayList(entries.keys)
            Collections.sort(keys)
            return keys
        }


    operator fun get(key: String): SetListEntry {
        return if (entries.containsKey(key))
            entries[key]!!
        else
            NotFoundSetListEntry(key)
    }

    fun contains(vararg keys: String): Boolean {
        for (key in keys) {
            if (!entries.containsKey(key)) {
                return false
            }
        }
        return true
    }

    fun size(): Int {
        return entries.size
    }

    override fun toString(): String {
        val keys = sortedKeys

        val parts = newArrayList<String>()
        for (key in keys) {
            val value = entries[key] as SetListEntry
            parts.add(value.asText())
        }

        return StringUtils.join(parts, " ")
    }

    fun getFirstPresentStateOf(vararg states: String): String? {
        return states.asSequence().firstOrNull { contains(it) }
    }

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

            val setListEntry = handle(key, value)
            if (setListEntry.isPresent) {
                return Pair(key, setListEntry.get())
            }
            return null
        }

        private fun handle(key: String, value: String): Optional<SetListItem> {
            val parts = value.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

            val type = findType(parts)
            return type.getSetListItemFor(key, parts)
        }

        private fun findType(parts: Array<String>): SetListItemType {
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