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

package li.klass.fhem.domain.core

import li.klass.fhem.domain.EventMap
import li.klass.fhem.domain.EventMapParser
import li.klass.fhem.domain.setlist.SetList
import li.klass.fhem.update.backend.xmllist.DeviceNode
import li.klass.fhem.update.backend.xmllist.DeviceNode.DeviceNodeType
import li.klass.fhem.update.backend.xmllist.XmlListDevice
import li.klass.fhem.update.backend.xmllist.webCmd
import org.apache.commons.lang3.StringUtils
import org.joda.time.DateTime
import java.io.Serializable
import java.util.*

class FhemDevice(val xmlListDevice: XmlListDevice) : Serializable, Comparable<FhemDevice> {

    val eventMap: EventMap by lazy { EventMap(EventMapParser.parse(xmlListDevice.getAttribute("eventMap") ?: "")) }
    val setList: SetList by lazy { SetList.parse(xmlListDevice.getHeader("sets") ?: "") }
    val devStateIcons: DevStateIcons by lazy { DevStateIcons.parse(xmlListDevice.getAttribute("devStateIcon")) }

    val aliasOrName: String
        get() = andFHEMAlias ?: alias ?: name

    val name: String
        get() = xmlListDevice.name

    var roomConcatenated: String
        get() {
            val (_, _, value) = xmlListDevice.attributes["room"] ?: return "Unsorted"
            return value
        }
        set(roomsConcatenated) {
            xmlListDevice.attributes.put("room", DeviceNode(DeviceNodeType.ATTR, "room", roomsConcatenated, null as DateTime?))
        }

    val state: String
        get() {
            val xmlListDevice = xmlListDevice
            val state = xmlListDevice.getInternal("STATE")
            return state ?: (xmlListDevice.getState("state", false)) ?: ""
        }

    val internalState: String
        get() {
            val state = state
            return eventMap.getKeyFor(state) ?: return state
        }

    val alias: String?
        get() = StringUtils.trimToNull(xmlListDevice.getAttribute("alias"))

    private val andFHEMAlias: String?
        get() = StringUtils.trimToNull(xmlListDevice.getAttribute("andFHEM_alias"))

    /**
     * Generate an array of available target states, but respect any set event maps.
     *
     * @return array of available target states
     */
    val availableTargetStatesEventMapTexts: Array<String>
        get() {
            val sortedKeys = setList.sortedKeys
            val eventMapKeys = mutableListOf<String>()
            sortedKeys.mapTo(eventMapKeys) { eventMap.getKeyOr(it, it) }
            return eventMapKeys.toTypedArray()
        }

    val webCmd: List<String>
        get() = xmlListDevice.webCmd

    val widgetName: String
        get() = xmlListDevice.getAttribute("widget_name") ?: aliasOrName

    val internalDeviceGroupOrGroupAttributes: List<String>
        get() {
            return (xmlListDevice.getAttribute("group") ?: "").split(",")
                    .filter { it.isNotBlank() }
                    .toList()
        }


    /**
     * Checks whether a device is in a given room.
     *
     * @param room room to check
     * @return true if the device is in the room
     */
    fun isInRoom(room: String): Boolean = getRooms().contains(room)

    fun isInAnyRoomsOf(rooms: Set<String>): Boolean =
            getRooms().intersect(rooms).isNotEmpty()

    fun getRooms() = roomConcatenated.split(",")

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false

        val another = other as FhemDevice?
        val name = name
        return name == another?.name

    }

    override fun hashCode(): Int {
        val name = name
        return name.hashCode()
    }

    fun getEventMapStateFor(state: String): String = eventMap.getValueOr(state, state)

    fun getReverseEventMapStateFor(state: String): String? = eventMap.getKeyOr(state, state)

    override fun toString(): String {
        val name = name
        val alias = alias
        return "FhemDevice{" +
                ", name='" + name + '\'' +
                ", alias='" + alias + '\'' +
                ", eventMap=" + eventMap +
                '}'
    }

    override fun compareTo(other: FhemDevice): Int {
        return other.aliasOrName.compareTo(aliasOrName)
    }

    companion object {
        val BY_NAME: Comparator<FhemDevice> = Comparator { o1, o2 ->
            val comparableAttribute = o1.xmlListDevice.getAttribute("sortby") ?: o1.aliasOrName
            val otherComparableAttribute = o2.xmlListDevice.getAttribute("sortby") ?: o2.aliasOrName

            comparableAttribute.compareTo(otherComparableAttribute)
        }
    }
}
