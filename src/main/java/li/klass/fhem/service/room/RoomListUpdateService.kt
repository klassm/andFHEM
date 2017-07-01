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
import com.google.common.base.Optional
import li.klass.fhem.domain.core.RoomDeviceList
import li.klass.fhem.service.Command
import li.klass.fhem.service.CommandExecutionService
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoomListUpdateService @Inject constructor(val commandExecutionService: CommandExecutionService,
                                                val deviceListParser: DeviceListParser,
                                                val roomListHolderService: RoomListHolderService) {

    fun updateSingleDevice(deviceName: String, connectionId: Optional<String>, context: Context): UpdateResult {
        return executeXmllistPartial(connectionId, context, deviceName)
    }

    fun updateRoom(roomName: String, connectionId: Optional<String>, context: Context): UpdateResult {
        return executeXmllistPartial(connectionId, context, "room=" + roomName)
    }

    fun updateAllDevices(connectionId: Optional<String>, context: Context): UpdateResult {
        return executeXmllist(connectionId, context, "", object : UpdateHandler {
            override fun handle(cached: RoomDeviceList, parsed: RoomDeviceList): RoomDeviceList {
                return parsed
            }
        })
    }

    private fun update(context: Context, connectionId: Optional<String>, result: Optional<RoomDeviceList>): Boolean {
        var success = false
        if (result.isPresent) {
            success = roomListHolderService.storeDeviceListMap(result.get(), connectionId, context)
            if (success) LOG.info("update - update was successful, sending result")
        } else {
            LOG.info("update - update was not successful, sending empty device list")
        }
        return success
    }

    private fun executeXmllistPartial(connectionId: Optional<String>, context: Context, devSpec: String): UpdateResult {
        LOG.info("executeXmllist(devSpec={}) - fetching xmllist from remote", devSpec)
        return executeXmllist(connectionId, context, " " + devSpec, object : UpdateHandler {
            override fun handle(cached: RoomDeviceList, parsed: RoomDeviceList): RoomDeviceList {
                cached.addAllDevicesOf(parsed, context)
                return cached
            }
        })
    }

    private fun executeXmllist(connectionId: Optional<String>, context: Context, xmllistSuffix: String, updateHandler: UpdateHandler):
            UpdateResult {
        val command = Command("xmllist" + xmllistSuffix, connectionId)
        val result = commandExecutionService.executeSync(command, context)
        val roomDeviceList = parseResult(connectionId, context, result, updateHandler)
        val success = update(context, connectionId, roomDeviceList)

        return when (success) {
            true -> UpdateResult.Success(roomDeviceList.orNull())
            else -> UpdateResult.Error()
        }
    }

    private fun parseResult(connectionId: Optional<String>, context: Context, result: String, updateHandler: UpdateHandler): Optional<RoomDeviceList> {
        val parsed = Optional.fromNullable(deviceListParser.parseAndWrapExceptions(result, context))
        val cached = roomListHolderService.getCachedRoomDeviceListMap(connectionId, context)
        if (parsed.isPresent) {
            val newDeviceList = updateHandler.handle(cached.or(parsed.get()), parsed.get())
            roomListHolderService.storeDeviceListMap(newDeviceList, connectionId, context)
            return Optional.of(newDeviceList)
        }
        return Optional.absent<RoomDeviceList>()
    }

    interface RoomListUpdateListener {
        fun onUpdateFinished(result: Boolean)
    }

    private interface UpdateHandler {
        fun handle(cached: RoomDeviceList, parsed: RoomDeviceList): RoomDeviceList
    }

    sealed class UpdateResult {
        class Success(val roomDeviceList: RoomDeviceList?) : UpdateResult()
        class Error : UpdateResult()
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(RoomListUpdateService::class.java)
    }
}
