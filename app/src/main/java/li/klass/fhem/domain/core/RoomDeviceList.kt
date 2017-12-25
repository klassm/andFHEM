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

import android.util.Log
import com.google.common.collect.FluentIterable.from
import com.google.common.collect.ImmutableSet
import com.google.common.collect.Lists.newArrayList
import com.google.common.collect.Maps.newHashMap
import com.google.common.collect.Sets.newHashSet
import li.klass.fhem.update.backend.xmllist.XmlListDevice
import java.io.Serializable
import java.util.*

/**
 * Class to hold devices for a certain room.
 */
class RoomDeviceList(val roomName: String) : Serializable {
    /**
     * Actual devices.
     */
    private val deviceMap = newHashMap<String, MutableSet<FhemDevice>>()

    constructor(roomDeviceList: RoomDeviceList?) : this(roomDeviceList?.roomName ?: "") {

        if (roomDeviceList != null) {
            for (device in roomDeviceList.allDevices) {
                addDevice(device)
            }
        }
    }

    /**
     * Gets an amount of devices for a given functionality. Devices that may not be shown within
     * the current connection or devices that are not supported are not included into the return
     * value.

     * @param functionality device functionality to filter.
     * *
     * @return list of devices matching the functionality.
     */
    fun getDevicesOfFunctionality(functionality: String): List<FhemDevice> =
            getDevicesOfFunctionality(functionality, true)

    /**
     * Gets devices of a certain group. At-devices are always excluded, as they are not shown
     * in any kind of normal view.

     * @param group            group to filter.
     * *
     * @param respectSupported set the parameter to false to also include devices that
     * *                         are not supported
     * *
     * @return list of devices matching the group.
     */
    private fun getDevicesOfFunctionality(group: String,
                                          respectSupported: Boolean): List<FhemDevice> {
        val deviceSet = getOrCreateDeviceList<FhemDevice>(group)
        val deviceList = newArrayList<FhemDevice>()
        deviceSet.filterTo(deviceList) { !respectSupported || it.isSupported }

        try {
            Collections.sort(deviceList, FhemDevice.BY_NAME)
        } catch (e: Exception) {
            Log.e(RoomDeviceList::class.java.name, "cannot sort", e)
        }

        return deviceList
    }

    private fun <T : FhemDevice> getOrCreateDeviceList(group: String): MutableSet<T> {
        if (!deviceMap.containsKey(group)) {
            deviceMap.put(group, HashSet())
        }
        @Suppress("UNCHECKED_CAST")
        return deviceMap[group] as MutableSet<T>
    }

    fun getDevicesOfType(deviceType: String): List<FhemDevice> =
            allDevices.filter { it.xmlListDevice?.type == deviceType }.sortedWith(FhemDevice.BY_NAME)

    val allDevices: Set<FhemDevice>
        get() {
            val devices = newHashSet<FhemDevice>()
            val devicesCollection = newArrayList(deviceMap.values)
            for (deviceHashSet in devicesCollection) {
                devices.addAll(deviceHashSet)
            }
            return Collections.unmodifiableSet(devices)
        }

    val allDevicesAsXmllistDevice: ImmutableSet<XmlListDevice>
        get() = from(allDevices).transform(FhemDevice.TO_XMLLIST_DEVICE).toSet()

    fun <T : FhemDevice> removeDevice(device: T) {
        val groups = device.getInternalDeviceGroupOrGroupAttributes()
        for (group in groups) {
            deviceMap[group]?.remove(device)
        }
    }

    fun <D : FhemDevice> getDeviceFor(deviceName: String): D? {
        val allDevices = allDevices
        allDevices
                .filter { it.name == deviceName }
                .forEach {
                    @Suppress("UNCHECKED_CAST")
                    return it as D
                }
        return null
    }

    val isEmptyOrOnlyContainsDoNotShowDevices: Boolean
        get() {

            return deviceMap.values
                    .flatMap { it }
                    .none { it.isSupported }
        }

    fun <T : FhemDevice> add(devices: Collection<T?>): RoomDeviceList {
        devices.forEach {
            addDevice(it)
        }
        return this
    }

    fun <T : FhemDevice> addDevice(device: T?): RoomDeviceList {
        if (device == null || !device.isSupported) {
            return this
        }

        val groups = device.getInternalDeviceGroupOrGroupAttributes()
        for (group in groups) {
            val groupList = getOrCreateDeviceList<FhemDevice>(group)
            groupList.remove(device)
            groupList.add(device)
        }

        return this
    }

    fun addAllDevicesOf(roomDeviceList: RoomDeviceList): RoomDeviceList {
        val allDevices = roomDeviceList.allDevices
        for (device in allDevices) {
            addDevice(device)
        }
        return this
    }

    fun filter(filter: (FhemDevice) -> Boolean): RoomDeviceList {
        val newList = RoomDeviceList(roomName)
        allDevices.filter(filter)
                .forEach { newList.addDevice(it) }
        return newList
    }

    fun getDeviceGroups(): Set<String> =
            allDevices.flatMap { it.getInternalDeviceGroupOrGroupAttributes() as List<String> }.toSet()

    companion object {

        /**
         * Name of the room that contains _all_ devices.
         */
        val ALL_DEVICES_ROOM = "ALL_DEVICES_LIST"
    }
}
