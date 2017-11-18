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

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.google.common.base.Optional
import com.google.common.collect.Iterables.partition
import li.klass.fhem.constants.Actions.DO_REMOTE_UPDATE
import li.klass.fhem.constants.BundleExtraKeys.CONNECTION_ID
import li.klass.fhem.constants.BundleExtraKeys.DEVICE_NAME
import li.klass.fhem.domain.core.FhemDevice
import li.klass.fhem.service.intent.RoomListUpdateIntentService
import li.klass.fhem.update.backend.DeviceListUpdateService
import li.klass.fhem.update.backend.command.execution.Command
import li.klass.fhem.update.backend.command.execution.CommandExecutionService
import li.klass.fhem.update.backend.device.configuration.DeviceConfiguration.TO_DELAY_FOR_UPDATE_AFTER_COMMAND
import li.klass.fhem.util.StateToSet
import li.klass.fhem.util.Tasker
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GenericDeviceService @Inject constructor(
        private val commandExecutionService: CommandExecutionService,
        private val deviceListUpdateService: DeviceListUpdateService
) {

    @JvmOverloads
    fun setState(device: FhemDevice, targetState: String, connectionId: Optional<String>, context: Context, invokeUpdate: Boolean = true) {
        val toSet = device.formatTargetState(targetState)

        commandExecutionService.executeSafely(Command("set " + device.name + " " + toSet, connectionId), context, invokePostCommandActions(device, context, invokeUpdate, "state", toSet, connectionId))

        device.xmlListDevice.setState("STATE", targetState)
        device.xmlListDevice.setInternal("STATE", targetState)
    }

    fun setSubState(device: FhemDevice, subStateName: String, value: String, connectionId: Optional<String>, context: Context, invokeDeviceUpdate: Boolean) {
        var stateName = subStateName
        var myValue = value
        if (device.deviceConfiguration.isPresent) {
            val configuration = device.deviceConfiguration.get()
            val toReplace = configuration.getCommandReplaceFor(stateName)
            for ((key, value1) in toReplace) {
                myValue = myValue
                        .replace(("([ ,])" + key).toRegex(), "$1$value1")
                        .replace(("^" + key).toRegex(), value1)
            }
            val subStateReplaceForSubState = configuration.getSubStateReplaceFor(stateName)
            if (subStateReplaceForSubState.isPresent) {
                stateName = subStateReplaceForSubState.get()
            }

            device.xmlListDevice.setState(stateName, myValue)
        }

        val command = Command("set " + device.name + " " + stateName + " " + myValue, connectionId)
        commandExecutionService.executeSafely(command, context,
                invokePostCommandActions(device, context, invokeDeviceUpdate, stateName, myValue, connectionId))
    }

    private fun invokePostCommandActions(device: FhemDevice, context: Context, invokeUpdate: Boolean, stateName: String, toSet: String, connectionId: Optional<String>): CommandExecutionService.ResultListener {
        return object : CommandExecutionService.SuccessfulResultListener() {
            override fun onResult(result: String) {

                if (invokeUpdate) {
                    update(device, context, connectionId)
                }

                Tasker.sendTaskerNotifyIntent(context, device.name, stateName, toSet)
                Tasker.requestQuery(context)
                device.xmlListDevice.setState(stateName, toSet)
            }
        }
    }

    fun setSubStates(device: FhemDevice, statesToSet: List<StateToSet>, connectionId: Optional<String>, context: Context) {
        if ("FHT".equals(device.xmlListDevice.type, ignoreCase = true) && statesToSet.size > 1) {
            setSubStatesForFHT(device, statesToSet, connectionId, context)
        } else {
            for (toSet in statesToSet) {
                setSubState(device, toSet.key, toSet.value, connectionId, context, false)
            }
        }
        update(device, context, connectionId)
    }

    private fun setSubStatesForFHT(device: FhemDevice, statesToSet: List<StateToSet>, connectionId: Optional<String>, context: Context) {
        val partitions = partition(statesToSet, 8)
        partitions.map { fhtConcat(it) }
                .forEach { setState(device, it, connectionId, context, false) }
    }

    private fun fhtConcat(toConcat: List<StateToSet>?): String =
            (toConcat ?: emptyList<StateToSet>())
                    .joinToString(separator = " ") { it.key + " " + it.value }

    fun update(device: FhemDevice, context: Context, connectionId: Optional<String>) {
        val delay = device.deviceConfiguration.transform(TO_DELAY_FOR_UPDATE_AFTER_COMMAND).or(0)
        val updateIntent = Intent(DO_REMOTE_UPDATE)
                .putExtra(DEVICE_NAME, device.name)
                .putExtra(CONNECTION_ID, connectionId.orNull())
                .setClass(context, RoomListUpdateIntentService::class.java)
        if (delay > 0) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.set(AlarmManager.RTC, (delay!! * 1000).toLong(), PendingIntent.getService(context, 0, updateIntent, PendingIntent.FLAG_UPDATE_CURRENT))
        } else {
            context.startService(updateIntent)
        }
    }
}
