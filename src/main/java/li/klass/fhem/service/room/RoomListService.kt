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

package li.klass.fhem.service.room

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.ResultReceiver
import com.google.common.base.Optional
import com.google.common.collect.Lists.newArrayList
import com.google.common.collect.Sets
import li.klass.fhem.appwidget.service.AppWidgetUpdateService
import li.klass.fhem.constants.Actions
import li.klass.fhem.constants.Actions.*
import li.klass.fhem.constants.BundleExtraKeys.*
import li.klass.fhem.constants.PreferenceKeys
import li.klass.fhem.constants.ResultCodes
import li.klass.fhem.domain.FHEMWEBDevice
import li.klass.fhem.domain.core.DeviceType.AT
import li.klass.fhem.domain.core.DeviceType.getDeviceTypeFor
import li.klass.fhem.domain.core.FhemDevice
import li.klass.fhem.domain.core.RoomDeviceList
import li.klass.fhem.service.AbstractService
import li.klass.fhem.service.connection.ConnectionService
import li.klass.fhem.service.intent.NotificationIntentService
import li.klass.fhem.util.ApplicationProperties
import li.klass.fhem.util.DateFormatUtil.toReadable
import org.slf4j.LoggerFactory
import java.io.Serializable
import java.util.*
import java.util.Collections.sort
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoomListService @Inject
constructor() : AbstractService() {

    private val remoteUpdateInProgress = AtomicBoolean(false)

    private var resendIntents: MutableList<Intent> = newArrayList()

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

        val updateWidgets = applicationProperties.getBooleanSharedPreference(PreferenceKeys.GCM_WIDGET_UPDATE, false, context)
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
    fun getRoomDeviceList(connectionId: Optional<String>, context: Context): Optional<RoomDeviceList> {
        return roomListHolderService.getCachedRoomDeviceListMap(connectionId, context)
    }

    fun resetUpdateProgress(context: Context) {
        LOG.debug("resetUpdateProgress()")
        remoteUpdateInProgress.set(false)
        resendIntents = newArrayList<Intent>()
        sendBroadcastWithAction(DISMISS_EXECUTING_DIALOG, context)
    }

    fun updateRoomDeviceListIfRequired(updatePeriod: Long, context: Context, connectionId: String? = null, room: String? = null, deviceName: String? = null) {
        val connectionExists = connectionService.exists(connectionId, context)
        val requiresUpdate = connectionExists && shouldUpdate(updatePeriod, Optional.fromNullable(connectionId), context, deviceName != null)
        if (requiresUpdate) {
            LOG.info("updateRoomDeviceListIfRequired() - requiring update")
            if (room != null) {
                roomListUpdateService.updateRoom(room, Optional.fromNullable(connectionId), context, updateWidgets = false)
            } else if (deviceName != null) {
                roomListUpdateService.updateSingleDevice(deviceName, Optional.fromNullable(connectionId), context, updateWidgets = false)
            } else {
                roomListUpdateService.updateAllDevices(Optional.fromNullable(connectionId), context, updateWidgets = false)
            }
        }
    }

    private fun answerError(resendIntent: Intent) {
        val receiver = resendIntent.getParcelableExtra<ResultReceiver>(RESULT_RECEIVER)
        receiver?.send(ResultCodes.ERROR, Bundle())
    }

    private fun resend(intent: Intent, context: Context) {
        LOG.info("resend() : resending {}", intent.action)

        if (intent.getIntExtra(RESEND_TRY, 0) > 2) {
            if (intent.hasExtra(RESULT_RECEIVER)) {
                val receiver = intent.getParcelableExtra<ResultReceiver>(RESULT_RECEIVER)
                receiver.send(ResultCodes.ERROR, Bundle())
                LOG.error("resend() - exceeds maximum attempts, sending error")
            }
        } else {
            context.startService(intent)
        }
    }

    private fun createResendIntent(intent: Intent): Intent {
        val resendIntent = Intent(intent)
        resendIntent.removeExtra(DO_REFRESH)
        resendIntent.removeExtra(UPDATE_PERIOD)

        resendIntent.putExtra(UPDATE_PERIOD, NEVER_UPDATE_PERIOD)
        resendIntent.putExtra(RESEND_TRY, intent.getIntExtra(RESEND_TRY, 0) + 1)

        return resendIntent
    }

    private fun shouldUpdate(updatePeriod: Long, connectionId: Optional<String>, context: Context, hasDevice: Boolean): Boolean {
        if (updatePeriod == ALWAYS_UPDATE_PERIOD) {
            LOG.debug("shouldUpdate() : recommend update, as updatePeriod is set to ALWAYS_UPDATE")
            return true
        }
        if (updatePeriod == NEVER_UPDATE_PERIOD) {
            LOG.debug("shouldUpdate() : recommend no update, as updatePeriod is set to NEVER_UPDATE")
            return false
        } else if (hasDevice) {
            LOG.debug("shouldUpdate() : has explicit device => update always")
            return true
        }

        val lastUpdate = getLastUpdate(connectionId, context)
        val shouldUpdate = lastUpdate + updatePeriod < System.currentTimeMillis()

        LOG.debug("shouldUpdate() : recommend {} update (lastUpdate: {}, updatePeriod: {} min)", if (!shouldUpdate) "no " else "to", toReadable(lastUpdate), updatePeriod / 1000 / 60)

        return shouldUpdate
    }

    fun getLastUpdate(connectionId: Optional<String>, context: Context): Long {
        return roomListHolderService.getLastUpdate(connectionId, context)
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
    fun getRoomNameList(connectionId: Optional<String>, context: Context): ArrayList<String> {
        val roomDeviceList = getRoomDeviceList(connectionId, context)
        if (!roomDeviceList.isPresent) return newArrayList()

        val roomNames = Sets.newHashSet<String>()
        for (device in roomDeviceList.get().allDevices) {
            val type = getDeviceTypeFor(device) ?: continue
            if (device.isSupported && connectionService.mayShowInCurrentConnectionType(type, context) && type != AT) {

                roomNames.addAll(device.rooms)
            }
        }
        roomNames.removeAll(roomDeviceList.get().hiddenRooms)

        val fhemwebDevice = roomListHolderService.findFHEMWEBDevice(roomDeviceList.get(), context)
        return sortRooms(roomNames, fhemwebDevice)
    }

    private fun sortRooms(roomNames: Set<String>, fhemwebDevice: FHEMWEBDevice?): ArrayList<String> {
        val sortRooms = newArrayList<String>()
        if (fhemwebDevice != null && fhemwebDevice.sortRooms != null) {
            sortRooms.addAll(Arrays.asList(*fhemwebDevice.sortRooms.split(SORT_ROOMS_DELIMITER.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()))
        }
        val roomNamesCopy = newArrayList(roomNames)
        sort(roomNamesCopy, sortRoomsComparator(sortRooms))
        return roomNamesCopy
    }

    private fun sortRoomsComparator(sortRooms: List<String>): Comparator<String> {
        return Comparator { lhs, rhs ->
            val lhsIndex = sortRooms.indexOf(lhs)
            val rhsIndex = sortRooms.indexOf(rhs)

            if (lhsIndex == rhsIndex && lhsIndex == -1) {
                // both not in sort list, compare based on names
                lhs.compareTo(rhs)
            } else if (lhsIndex != rhsIndex && lhsIndex != -1 && rhsIndex != -1) {
                // both in sort list, compare indexes
                lhsIndex.compareTo(rhsIndex)
            } else if (lhsIndex == -1) {
                // lhs not in sort list, rhs in sort list
                1
            } else {
                // rhs not in sort list, lhs in sort list
                -1
            }
        }
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
            roomDeviceList.hiddenGroups = allRoomDeviceList.get().hiddenGroups
            roomDeviceList.hiddenRooms = allRoomDeviceList.get().hiddenRooms
        }

        return roomDeviceList
    }

    enum class RemoteUpdateRequired {
        REQUIRED, NOT_REQUIRED
    }

    companion object {

        private val LOG = LoggerFactory.getLogger(RoomListService::class.java)

        val PREFERENCES_NAME = RoomListService::class.java.name

        val LAST_UPDATE_PROPERTY = "LAST_UPDATE"

        val NEVER_UPDATE_PERIOD: Long = 0
        val ALWAYS_UPDATE_PERIOD: Long = -1

        val SORT_ROOMS_DELIMITER = " "
    }
}
