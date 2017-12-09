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

package li.klass.fhem.update.backend.xmllist

import com.google.common.base.Optional
import li.klass.fhem.domain.core.DevStateIcons
import li.klass.fhem.domain.setlist.SetList
import org.apache.commons.lang3.StringUtils.trimToNull
import org.joda.time.DateTime
import java.io.Serializable

class XmlListDevice(
        val type: String,
        val attributes: MutableMap<String, DeviceNode>,
        val states: MutableMap<String, DeviceNode>,
        val internals: MutableMap<String, DeviceNode>,
        val headers: MutableMap<String, DeviceNode>

) : Serializable {

    val creationTime = DateTime.now()!!
    val setList: SetList = SetList.Companion.parse(getHeader("sets").or(""))
    val devStateIcons = DevStateIcons.parse(getAttribute("devStateIcon").orNull())

    val name: String get() = internals["NAME"]?.value ?: "??"

    private fun containsInternal(key: String): Boolean = internals.containsKey(key)

    fun containsState(state: String): Boolean = containsAnyOfStates(setOf(state))

    fun containsAnyOfStates(toFind: Collection<String>): Boolean =
            toFind.any { states.containsKey(it) }

    fun containsAttribute(attribute: String): Boolean = attributes.containsKey(attribute)

    private fun containsHeader(header: String): Boolean = headers.containsKey(header)

    fun getState(state: String, ignoreCase: Boolean = false): Optional<String> {
        val matches = states.filterKeys { it.equals(state, ignoreCase) }
        return if (matches.isEmpty()) Optional.absent() else Optional.of(matches.values.first().value)
    }

    fun getFirstStateOf(toFind: Collection<String>): Optional<String> {
        return toFind
                .map { getState(it) }
                .firstOrNull { it.isPresent }
                ?: Optional.absent()
    }

    fun getAttribute(attribute: String): Optional<String> {
        return if (containsAttribute(attribute)) {
            Optional.fromNullable(attributes[attribute]?.value)
        } else Optional.absent()
    }

    fun getHeader(header: String): Optional<String> {
        return if (containsHeader(header)) {
            Optional.fromNullable(headers[header]?.value)
        } else Optional.absent()
    }

    fun getInternal(key: String): Optional<String> {
        return if (containsInternal(key)) {
            Optional.fromNullable(internals[key]?.value)
        } else Optional.absent()
    }

    fun setState(key: String, value: String) {
        states.put(key, DeviceNode(DeviceNode.DeviceNodeType.STATE, key, value, measuredNow()))
    }

    fun setInternal(key: String, value: String) {
        internals.put(key, DeviceNode(DeviceNode.DeviceNodeType.INT, key, value, measuredNow()))
    }

    fun setHeader(key: String, value: String) {
        headers.put(key, DeviceNode(DeviceNode.DeviceNodeType.HEADER, key, value, measuredNow()))
    }

    fun setAttribute(key: String, value: String?) {
        val toSet = trimToNull(value)
        if (toSet == null) {
            attributes.remove(key)
            return
        }
        if (!toSet.equals(getAttribute(key).orNull(), ignoreCase = true)) {
            attributes.put(key, DeviceNode(DeviceNode.DeviceNodeType.ATTR, key, toSet, measuredNow()))
        }
    }

    override fun toString(): String {
        return "XmlListDevice{" +
                "type='" + type + '\'' +
                ", attributes=" + attributes +
                ", states=" + states +
                ", internals=" + internals +
                '}'
    }

    fun attributeValueFor(key: String): Optional<String> {
        return if (attributes.containsKey(key)) {
            Optional.fromNullable(attributes[key]?.value)
        } else Optional.absent()
    }

    fun stateValueFor(key: String): Optional<String> {
        return if (states.containsKey(key)) {
            Optional.fromNullable(states[key]?.value)
        } else Optional.absent()
    }

    private fun measuredNow(): String = DateTime().toString(DATE_PATTERN)

    companion object {
        private val DATE_PATTERN = "yyyy-MM-dd HH:mm:ss"
    }
}
