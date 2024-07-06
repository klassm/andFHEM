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
import android.content.Intent
import li.klass.fhem.appindex.AppIndexIntentService
import li.klass.fhem.connection.backend.ConnectionService
import li.klass.fhem.connection.backend.DataConnectionSwitch
import li.klass.fhem.connection.backend.DummyServerSpec
import li.klass.fhem.constants.Actions
import li.klass.fhem.constants.BundleExtraKeys
import li.klass.fhem.domain.core.RoomDeviceList
import li.klass.fhem.update.backend.command.execution.Command
import li.klass.fhem.update.backend.command.execution.CommandExecutionService
import li.klass.fhem.update.backend.xmllist.DeviceListParser
import org.joda.time.DateTime
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeviceListUpdateService @Inject constructor(
        private val commandExecutionService: CommandExecutionService,
        private val deviceListParser: DeviceListParser,
        private val deviceListCacheService: DeviceListCacheService,
        private val connectionService: ConnectionService,
        private val dataConnectionSwitch: DataConnectionSwitch,
        private val application: Application
) {

    fun updateSingleDevice(deviceName: String, connectionId: String? = null): UpdateResult =
            executeXmllistPartial(connectionId, deviceName, object : BeforeRoomListUpdateModifier {
                override fun update(cached: RoomDeviceList, newlyLoaded: RoomDeviceList) {
                    if (newlyLoaded.getDeviceFor(deviceName) != null) {
                        sendUpdatedBroadcastFor(newlyLoaded, connectionId)
                        cached.getDeviceFor(deviceName)?.let { cached.removeDevice(it) }
                    }
                }
            })

    fun updateRoom(roomName: String, connectionId: String? = null): UpdateResult {
        val toUpdate = if (roomName == "Unsorted") "" else roomName
        return executeXmllistPartial(connectionId, "room=$toUpdate", object : BeforeRoomListUpdateModifier {
            override fun update(cached: RoomDeviceList, newlyLoaded: RoomDeviceList) {
                sendUpdatedBroadcastFor(newlyLoaded, connectionId)
                cached.allDevices.filter { it.isInRoom(roomName) }.forEach { cached.removeDevice(it) }
            }
        })
    }

    fun updateAllDevices(connectionId: String? = null): UpdateResult {
        return executeXmllist(connectionId, "", object : UpdateHandler {
            override fun handle(cached: RoomDeviceList, parsed: RoomDeviceList): RoomDeviceList {
                sendUpdatedBroadcastFor(parsed, connectionId)
                return parsed
            }
        })
    }

    fun getLastUpdate(connectionId: String?): DateTime? =
            deviceListCacheService.getLastUpdate(connectionId)

    private fun update(connectionId: String?, result: RoomDeviceList?): Boolean = when {
        result != null -> {
            val success = deviceListCacheService.storeDeviceListMap(result, connectionId)
            if (success) LOG.info("update - update was successful, sending result")
            success
        }
        else -> {
            LOG.info("update - update was not successful, sending empty device list"); false
        }
    }

    private fun executeXmllistPartial(connectionId: String?, devSpec: String,
                                      beforeRoomListUpdateModifier: BeforeRoomListUpdateModifier): UpdateResult {
        LOG.info("executeXmllistPartial(connection={}, devSpec={}) - fetching xmllist from remote", connectionId, devSpec)
        if (deviceListCacheService.isCorrupted()) {
            LOG.error("executeXmllistPartial - ignoring partial update as device list is broken, updating all devices instead")
            return updateAllDevices(connectionId)
        }
        return executeXmllist(connectionId, " $devSpec", object : UpdateHandler {
            override fun handle(cached: RoomDeviceList, parsed: RoomDeviceList): RoomDeviceList {
                beforeRoomListUpdateModifier.update(cached, parsed)
                cached.addAllDevicesOf(parsed)
                return cached
            }
        })
    }

    @Synchronized
    private fun executeXmllist(connectionId: String?, xmllistSuffix: String, updateHandler: UpdateHandler):
            UpdateResult {

        applicationContext.sendBroadcast(Intent(Actions.SHOW_EXECUTING_DIALOG))
        try {
            val command = Command("xmllist$xmllistSuffix", connectionId)
            val result = commandExecutionService.executeSync(command)
            val roomDeviceList = result?.let { parseResult(connectionId, applicationContext, it, updateHandler) }


            return when (roomDeviceList != null && update(connectionId, roomDeviceList)) {
                true -> {
                    updateIndex()
                    UpdateResult.Success(roomDeviceList)
                }
                else -> UpdateResult.Error
            }
        } catch (e: Exception) {
            LOG.warn("Error while updating", e)
            return UpdateResult.Error
        } finally {
            applicationContext.sendBroadcast(Intent(Actions.DISMISS_EXECUTING_DIALOG))
        }
    }

    private fun updateIndex() {
        try {
            applicationContext.startService(Intent("com.google.firebase.appindexing.UPDATE_INDEX")
                    .setClass(applicationContext, AppIndexIntentService::class.java))
        } catch (e: Exception) {
            LOG.debug("cannot update app index, probably because we are in background", e)
        }
    }

    private fun parseResult(connectionId: String?, context: Context, result: String, updateHandler: UpdateHandler): RoomDeviceList? {
        val connection = dataConnectionSwitch.getProviderFor(connectionId).server.id
        val parsed = deviceListParser.parseAndWrapExceptions(result, context, connection)
        val cached = deviceListCacheService.getCachedRoomDeviceListMap(connectionId)
        if (parsed != null) {
            val newDeviceList = updateHandler.handle(cached ?: parsed, parsed)
            deviceListCacheService.storeDeviceListMap(newDeviceList, connectionId)
            return newDeviceList
        }
        return null
    }

    fun checkForCorruptedDeviceList() {
        try {
            connectionService.listAll()
                    .filter { it !is DummyServerSpec }
                    .forEach { connection ->
                        val corrupted = deviceListCacheService.isCorrupted(connection.id)
                        LOG.info("checkForCorruptedDeviceList - checking ${connection.name}, corrupted=$corrupted")
                        if (corrupted) {
                            LOG.info("checkForCorruptedDeviceList - could not load device list for ${connection.name}, requesting update")
                            updateAllDevices(connection.id)
                        }
                    }
        } catch (e: Exception) {
            LOG.error("checkForCorruptedDeviceList - error while checking for corrupted device lists", e)
        }
    }

    private fun sendUpdatedBroadcastFor(roomDeviceList: RoomDeviceList, connectionId: String?) {
        val updatedDevices = roomDeviceList.allDevices.map { it.name }
        val connection = connectionId ?: connectionService.getSelectedId()
        application.sendBroadcast(Intent(Actions.DEVICES_UPDATED)
                .putExtra(BundleExtraKeys.UPDATED_DEVICE_NAMES, ArrayList(updatedDevices))
                .putExtra(BundleExtraKeys.CONNECTION_ID, connection))
    }

    private val applicationContext: Context get() = application.applicationContext

    private interface UpdateHandler {
        fun handle(cached: RoomDeviceList, parsed: RoomDeviceList): RoomDeviceList
    }

    @FunctionalInterface
    private interface BeforeRoomListUpdateModifier {
        fun update(cached: RoomDeviceList, newlyLoaded: RoomDeviceList)
    }

    sealed class UpdateResult {
        class Success(val roomDeviceList: RoomDeviceList?) : UpdateResult()
        object Error : UpdateResult()
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(DeviceListUpdateService::class.java)
    }
}
