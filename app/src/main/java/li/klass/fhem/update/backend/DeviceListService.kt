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

package li.klass.fhem.update.backend

import android.content.Context
import li.klass.fhem.constants.Actions.DISMISS_EXECUTING_DIALOG
import li.klass.fhem.domain.core.FhemDevice
import li.klass.fhem.domain.core.RoomDeviceList
import li.klass.fhem.service.AbstractService
import li.klass.fhem.update.backend.xmllist.DeviceListParser
import org.slf4j.LoggerFactory
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeviceListService @Inject
constructor(
        private val deviceListParser: DeviceListParser,
        private val deviceListCacheService: DeviceListCacheService
) : AbstractService() {

    private val remoteUpdateInProgress = AtomicBoolean(false)

    fun parseReceivedDeviceStateMap(deviceName: String, updateMap: Map<String, String>, connectionId: String) {
        getDeviceForName(deviceName, connectionId)?.let {
            deviceListParser.fillDeviceWith(it, updateMap)
            LOG.info("parseReceivedDeviceStateMap()  : updated {} with {} new values!", it.name, updateMap.size)
        }
    }

    /**
     * Looks for a device with a given name.

     * @param deviceName name of the device
     * *
     * @return found device or null
     */
    @Suppress("UNCHECKED_CAST")
    fun getDeviceForName(deviceName: String, connectionId: String? = null): FhemDevice? =
            getAllRoomsDeviceList(connectionId).getDeviceFor(deviceName)

    /**
     * Retrieves a [RoomDeviceList] containing all devices, not only the devices of a specific room.
     * The room device list will be a copy of the actual one. Thus, any modifications will have no effect!

     * @return [RoomDeviceList] containing all devices
     */
    fun getAllRoomsDeviceList(connectionId: String? = null): RoomDeviceList {
        val originalRoomDeviceList = getRoomDeviceList(connectionId)
        return RoomDeviceList(originalRoomDeviceList)
    }

    /**
     * Loads the currently cached [li.klass.fhem.domain.core.RoomDeviceList]. If the cached
     * device list has not yet been loaded, it will be loaded from the cache object.
     *
     *
     *
     * Watch out: Any modifications will be saved within the internal representation. Don't use
     * this method from client code!

     * @return Currently cached [li.klass.fhem.domain.core.RoomDeviceList].
     */
    fun getRoomDeviceList(connectionId: String? = null): RoomDeviceList? =
            deviceListCacheService.getCachedRoomDeviceListMap(connectionId)

    fun resetUpdateProgress(context: Context) {
        LOG.debug("resetUpdateProgress()")
        remoteUpdateInProgress.set(false)
        sendBroadcastWithAction(DISMISS_EXECUTING_DIALOG, context)
    }

    fun getAvailableDeviceNames(connectionId: String? = null): List<String> {
        val allRoomsDeviceList = getAllRoomsDeviceList(connectionId)
        return allRoomsDeviceList.allDevices
                .map {
                    it.name + "|" +
                            emptyOrValue(it.alias) + "|" +
                            emptyOrValue(it.widgetName)
                }
    }

    private fun emptyOrValue(value: String?): String {
        if (value == null) return ""
        return value
    }

    /**
     * Retrieves a list of all room names.

     * @return list of all room names
     */
    fun getRoomNameList(connectionId: String? = null): Set<String> {
        val roomDeviceList = getRoomDeviceList(connectionId)
        return (roomDeviceList?.allDevices ?: emptySet())
                .flatMap { it.getRooms() }
                .toSet()
    }

    /**
     * Retrieves the [RoomDeviceList] for a specific room name.

     * @param roomName room name used for searching.
     * *
     * @return found [RoomDeviceList] or null
     */
    fun getDeviceListForRoom(roomName: String, connectionId: String? = null): RoomDeviceList {
        val allDevices = (getRoomDeviceList(connectionId)
                ?.allDevices ?: emptySet())
                .filter { it.isInRoom(roomName) }
        return RoomDeviceList(roomName).add(allDevices)
    }

    companion object {

        private val LOG = LoggerFactory.getLogger(DeviceListService::class.java)

        const val NEVER_UPDATE_PERIOD: Long = 0
        const val ALWAYS_UPDATE_PERIOD: Long = -1
    }
}
