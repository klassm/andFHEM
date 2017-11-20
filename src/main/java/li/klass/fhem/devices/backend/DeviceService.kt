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

package li.klass.fhem.devices.backend

import android.content.Context
import android.content.Intent
import com.google.common.base.Strings
import li.klass.fhem.constants.Actions
import li.klass.fhem.domain.core.FhemDevice
import li.klass.fhem.update.backend.DeviceListService
import li.klass.fhem.update.backend.command.execution.Command
import li.klass.fhem.update.backend.command.execution.CommandExecutionService
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Class accumulating all device actions like renaming, moving or deleting.
 */
@Singleton
class DeviceService @Inject
constructor(
        private val commandExecutionService: CommandExecutionService,
        private val deviceListService: DeviceListService
) {

    /**
     * Rename a device.
     *
     * @param device  concerned device
     * @param newName new device name
     * @param context context
     */
    fun renameDevice(device: FhemDevice, newName: String, context: Context) {
        commandExecutionService.executeSafely(Command("rename " + device.name + " " + newName), context, object : CommandExecutionService.SuccessfulResultListener() {
            override fun onResult(result: String) {
                device.xmlListDevice.setInternal("NAME", newName)
            }
        })

    }

    /**
     * Deletes a device.
     *
     * @param device concerned device
     */
    fun deleteDevice(device: FhemDevice, context: Context) {
        commandExecutionService.executeSafely(Command("delete " + device.name!!), context, object : CommandExecutionService.SuccessfulResultListener() {
            override fun onResult(result: String) {
                deviceListService.getRoomDeviceList()
                        ?.removeDevice(device)
            }
        })
    }

    /**
     * Sets an alias for a device.
     *
     * @param device  concerned device
     * @param alias   new alias to set
     * @param context context
     */
    fun setAlias(device: FhemDevice, alias: String, context: Context) {
        val command = if (Strings.isNullOrEmpty(alias))
            "deleteattr " + device.name + " alias"
        else
            "attr " + device.name + " alias " + alias
        commandExecutionService.executeSafely(Command(command), context, object : CommandExecutionService.SuccessfulResultListener() {
            override fun onResult(result: String) {
                device.xmlListDevice.setAttribute("alias", alias)
            }
        })
    }

    /**
     * Moves a device.
     *
     * @param device              concerned device
     * @param newRoomConcatenated new room to move the concerned device to.
     * @param context             context
     */
    fun moveDevice(device: FhemDevice, newRoomConcatenated: String, context: Context) {
        commandExecutionService.executeSafely(Command("attr " + device.name + " room " + newRoomConcatenated), context, object : CommandExecutionService.SuccessfulResultListener() {
            override fun onResult(result: String) {
                device.roomConcatenated = newRoomConcatenated
                context.sendBroadcast(Intent(Actions.DO_UPDATE))
            }
        })
    }
}
