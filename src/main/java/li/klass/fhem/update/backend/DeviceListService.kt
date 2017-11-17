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

import android.app.Application
import android.content.Context
import com.google.common.base.Optional
import com.google.common.collect.Lists.newArrayList
import com.google.common.collect.Sets
import li.klass.fhem.connection.backend.ConnectionService
import li.klass.fhem.connection.backend.DummyServerSpec
import li.klass.fhem.constants.Actions.DISMISS_EXECUTING_DIALOG
import li.klass.fhem.domain.core.DeviceType.AT
import li.klass.fhem.domain.core.DeviceType.getDeviceTypeFor
import li.klass.fhem.domain.core.FhemDevice
import li.klass.fhem.domain.core.RoomDeviceList
import li.klass.fhem.service.AbstractService
import li.klass.fhem.update.backend.xmllist.DeviceListParser
import org.slf4j.LoggerFactory
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeviceListService @Inject
constructor(
        private val connectionService: ConnectionService,
        private val deviceListParser: DeviceListParser,
        private val deviceListHolderService: DeviceListHolderService,
        private val deviceListUpdateService: DeviceListUpdateService,
        private val application: Application
) : AbstractService() {

    private val remoteUpdateInProgress = AtomicBoolean(false)

    fun parseReceivedDeviceStateMap(deviceName: String, updateMap: Map<String, String>) {

        val deviceOptional = getDeviceForName<FhemDevice>(deviceName)
        if (!deviceOptional.isPresent) {
            return
        }

        val device = deviceOptional.get()
        deviceListParser.fillDeviceWith(device, updateMap, applicationContext)

        LOG.info("parseReceivedDeviceStateMap()  : updated {} with {} new values!", device.name, updateMap.size)
    }

    /**
     * Looks for a device with a given name.

     * @param deviceName name of the device
     * *
     * @return found device or null
     */
    @Suppress("UNCHECKED_CAST")
    fun <T : FhemDevice> getDeviceForName(deviceName: String?, connectionId: String? = null): Optional<T> {
        if (deviceName == null) {
            return Optional.absent<T>()
        }
        return Optional.fromNullable(getAllRoomsDeviceList(connectionId).getDeviceFor<FhemDevice>(deviceName) as T?)
    }

    /**
     * Retrieves a [RoomDeviceList] containing all devices, not only the devices of a specific room.
     * The room device list will be a copy of the actual one. Thus, any modifications will have no effect!

     * @return [RoomDeviceList] containing all devices
     */
    fun getAllRoomsDeviceList(connectionId: String? = null): RoomDeviceList {
        val originalRoomDeviceList = getRoomDeviceList(connectionId)
        return RoomDeviceList(originalRoomDeviceList.orNull(), applicationContext)
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
    fun getRoomDeviceList(connectionId: String? = null): Optional<RoomDeviceList> =
            deviceListHolderService.getCachedRoomDeviceListMap(Optional.fromNullable(connectionId), applicationContext)

    fun resetUpdateProgress(context: Context) {
        LOG.debug("resetUpdateProgress()")
        remoteUpdateInProgress.set(false)
        sendBroadcastWithAction(DISMISS_EXECUTING_DIALOG, context)
    }

    fun getAvailableDeviceNames(connectionId: Optional<String>): ArrayList<String> {
        val deviceNames = newArrayList<String>()
        val allRoomsDeviceList = getAllRoomsDeviceList(connectionId.orNull())

        for (device in allRoomsDeviceList.allDevices) {
            deviceNames.add(device.name + "|" +
                    emptyOrValue(device.alias) + "|" +
                    emptyOrValue(device.widgetName))
        }

        return deviceNames
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
        if (!roomDeviceList.isPresent) return emptySet()

        val roomNames = Sets.newHashSet<String>()
        for (device in roomDeviceList.get().allDevices) {
            val type = getDeviceTypeFor(device) ?: continue
            if (device.isSupported && connectionService.mayShowInCurrentConnectionType(type, applicationContext) && type != AT) {

                roomNames.addAll(device.rooms)
            }
        }

        return roomNames
    }

    /**
     * Retrieves the [RoomDeviceList] for a specific room name.

     * @param roomName room name used for searching.
     * *
     * @return found [RoomDeviceList] or null
     */
    fun getDeviceListForRoom(roomName: String, connectionId: String? = null): RoomDeviceList {
        val roomDeviceList = RoomDeviceList(roomName)

        val allRoomDeviceList = getRoomDeviceList(connectionId)
        if (allRoomDeviceList.isPresent) {
            allRoomDeviceList.get().allDevices
                    .filter { it.isInRoom(roomName) }
                    .forEach { roomDeviceList.addDevice(it, applicationContext) }
        }

        return roomDeviceList
    }

    fun checkForCorruptedDeviceList() {
        val connections = connectionService.listAll(applicationContext)
                .filter { it !is DummyServerSpec }
        connections.forEach { connection ->
            val connectionId = Optional.of(connection.id)
            val corrupted = deviceListHolderService.isCorrupted(connectionId, applicationContext)
            if (corrupted) {
                deviceListUpdateService.updateAllDevices(connection.id)
            }
        }
    }

    private val applicationContext: Context get() = application.applicationContext

    companion object {

        private val LOG = LoggerFactory.getLogger(DeviceListService::class.java)

        val PREFERENCES_NAME = DeviceListService::class.java.name!!

        val NEVER_UPDATE_PERIOD: Long = 0
        val ALWAYS_UPDATE_PERIOD: Long = -1
    }
}
