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
import android.content.Intent
import com.google.common.base.Optional
import com.google.common.collect.Lists.newArrayList
import com.google.common.collect.Sets
import li.klass.fhem.appwidget.service.AppWidgetUpdateService
import li.klass.fhem.connection.backend.ConnectionService
import li.klass.fhem.connection.backend.DummyServerSpec
import li.klass.fhem.constants.Actions
import li.klass.fhem.constants.Actions.*
import li.klass.fhem.constants.BundleExtraKeys.*
import li.klass.fhem.domain.core.DeviceType.AT
import li.klass.fhem.domain.core.DeviceType.getDeviceTypeFor
import li.klass.fhem.domain.core.FhemDevice
import li.klass.fhem.domain.core.RoomDeviceList
import li.klass.fhem.service.AbstractService
import li.klass.fhem.service.intent.NotificationIntentService
import li.klass.fhem.settings.SettingsKeys
import li.klass.fhem.update.backend.xmllist.DeviceListParser
import li.klass.fhem.util.ApplicationProperties
import org.slf4j.LoggerFactory
import java.io.Serializable
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoomListService @Inject
constructor() : AbstractService() {

    private val remoteUpdateInProgress = AtomicBoolean(false)

    @Inject
    lateinit var connectionService: ConnectionService

    @Inject
    lateinit var deviceListParser: DeviceListParser

    @Inject
    lateinit var applicationProperties: ApplicationProperties

    @Inject
    lateinit var roomListHolderService: RoomListHolderService

    @Inject
    lateinit var roomListUpdateService: RoomListUpdateService

    fun parseReceivedDeviceStateMap(deviceName: String, updateMap: Map<String, String>,
                                    vibrateUponNotification: Boolean, context: Context) {

        val deviceOptional = getDeviceForName<FhemDevice>(deviceName, Optional.absent<String>(), context)
        if (!deviceOptional.isPresent) {
            return
        }

        val device = deviceOptional.get()
        deviceListParser.fillDeviceWith(device, updateMap, context)

        LOG.info("parseReceivedDeviceStateMap()  : updated {} with {} new values!", device.name, updateMap.size)

        context.startService(Intent(NOTIFICATION_TRIGGER)
                .setClass(context, NotificationIntentService::class.java)
                .putExtra(DEVICE_NAME, deviceName)
                .putExtra(DEVICE, device)
                .putExtra(UPDATE_MAP, updateMap as Serializable)
                .putExtra(VIBRATE, vibrateUponNotification))

        context.sendBroadcast(Intent(DO_UPDATE))

        val updateWidgets = applicationProperties.getBooleanSharedPreference(SettingsKeys.GCM_WIDGET_UPDATE, false)
        if (updateWidgets) {
            context.startService(Intent(Actions.REDRAW_ALL_WIDGETS)
                    .setClass(context, AppWidgetUpdateService::class.java))
        }
    }

    /**
     * Looks for a device with a given name.

     * @param deviceName name of the device
     * *
     * @return found device or null
     */
    @Suppress("UNCHECKED_CAST")
    fun <T : FhemDevice> getDeviceForName(deviceName: String?, connectionId: Optional<String>, context: Context): Optional<T> {
        if (deviceName == null) {
            return Optional.absent<T>()
        }
        return Optional.fromNullable(getAllRoomsDeviceList(connectionId, context).getDeviceFor<FhemDevice>(deviceName) as T?)
    }

    /**
     * Retrieves a [RoomDeviceList] containing all devices, not only the devices of a specific room.
     * The room device list will be a copy of the actual one. Thus, any modifications will have no effect!

     * @return [RoomDeviceList] containing all devices
     */
    fun getAllRoomsDeviceList(connectionId: Optional<String>, context: Context): RoomDeviceList {
        val originalRoomDeviceList = getRoomDeviceList(connectionId, context)
        return RoomDeviceList(originalRoomDeviceList.orNull(), context)
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
     * *
     * @param context context
     */
    fun getRoomDeviceList(connectionId: Optional<String>, context: Context) =
            roomListHolderService.getCachedRoomDeviceListMap(connectionId, context)

    fun resetUpdateProgress(context: Context) {
        LOG.debug("resetUpdateProgress()")
        remoteUpdateInProgress.set(false)
        sendBroadcastWithAction(DISMISS_EXECUTING_DIALOG, context)
    }

    fun getAvailableDeviceNames(connectionId: Optional<String>, context: Context): ArrayList<String> {
        val deviceNames = newArrayList<String>()
        val allRoomsDeviceList = getAllRoomsDeviceList(connectionId, context)

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

     * @param context context
     * *
     * @return list of all room names
     */
    fun getRoomNameList(connectionId: Optional<String>, context: Context): Set<String> {
        val roomDeviceList = getRoomDeviceList(connectionId, context)
        if (!roomDeviceList.isPresent) return emptySet()

        val roomNames = Sets.newHashSet<String>()
        for (device in roomDeviceList.get().allDevices) {
            val type = getDeviceTypeFor(device) ?: continue
            if (device.isSupported && connectionService.mayShowInCurrentConnectionType(type, context) && type != AT) {

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
    fun getDeviceListForRoom(roomName: String, connectionId: Optional<String>, context: Context): RoomDeviceList {
        val roomDeviceList = RoomDeviceList(roomName)

        val allRoomDeviceList = getRoomDeviceList(connectionId, context)
        if (allRoomDeviceList.isPresent) {
            for (device in allRoomDeviceList.get().allDevices) {
                if (device.isInRoom(roomName)) {
                    roomDeviceList.addDevice(device, context)
                }
            }
        }

        return roomDeviceList
    }

    fun checkForCorruptedDeviceList(context: Context) {
        val connections = connectionService.listAll(context)
                .filter { it !is DummyServerSpec }
        connections.forEach { connection ->
            val connectionId = Optional.of(connection.id)
            val corrupted = roomListHolderService.isCorrupted(connectionId, context)
            if (corrupted) {
                roomListUpdateService.updateAllDevices(connectionId, context, updateWidgets = false)
            }
        }
    }

    companion object {

        private val LOG = LoggerFactory.getLogger(RoomListService::class.java)

        val PREFERENCES_NAME = RoomListService::class.java.name

        val NEVER_UPDATE_PERIOD: Long = 0
        val ALWAYS_UPDATE_PERIOD: Long = -1
    }
}
