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
import android.app.Application
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
import li.klass.fhem.update.backend.device.configuration.DeviceConfigurationProvider
import li.klass.fhem.update.backend.xmllist.XmlListDevice
import li.klass.fhem.util.StateToSet
import li.klass.fhem.util.Tasker
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GenericDeviceService @Inject constructor(
        private val commandExecutionService: CommandExecutionService,
        private val application: Application,
        private val deviceConfigurationProvider: DeviceConfigurationProvider,
        private val deviceListUpdateService: DeviceListUpdateService
) {
    private val applicationContext get() = application.applicationContext

    @JvmOverloads
    fun setState(device: FhemDevice, targetState: String, connectionId: Optional<String>, invokeUpdate: Boolean = true) {
        val toSet = device.formatTargetState(targetState)

        commandExecutionService.executeSafely(Command("set ${device.name} $toSet", connectionId), invokePostCommandActions(device, invokeUpdate, "state", toSet, connectionId))

        device.xmlListDevice.setState("STATE", targetState)
        device.xmlListDevice.setInternal("STATE", targetState)
    }

    fun setSubState(device: FhemDevice, subStateName: String, value: String, connectionId: Optional<String>, invokeDeviceUpdate: Boolean) {
        val commandReplacements = deviceConfigurationProvider.configurationFor(device)
                .stateConfigFor(subStateName)
                ?.beforeCommandReplacement ?: emptySet()

        val valueToSet = commandReplacements.fold(value, { v: String, replacement ->
            v.replace(("([ ,])" + replacement.search).toRegex(), "$1${replacement.replaceBy}")
                    .replace(("^" + replacement.search).toRegex(), replacement.replaceBy)
        })

        val command = Command("set " + device.name + " " + subStateName + " " + valueToSet, connectionId)
        commandExecutionService.executeSafely(command, invokePostCommandActions(device, invokeDeviceUpdate, subStateName, valueToSet, connectionId))
    }

    private fun invokePostCommandActions(device: FhemDevice, invokeUpdate: Boolean,
                                         stateName: String, toSet: String,
                                         connectionId: Optional<String>): CommandExecutionService.ResultListener {
        return object : CommandExecutionService.SuccessfulResultListener() {
            override fun onResult(result: String) {

                if (invokeUpdate) {
                    update(device.xmlListDevice, connectionId.orNull())
                }

                Tasker.sendTaskerNotifyIntent(applicationContext, device.name, stateName, toSet)
                Tasker.requestQuery(applicationContext)
                device.xmlListDevice.setState(stateName, toSet)
            }
        }
    }

    fun setSubStates(device: FhemDevice, statesToSet: List<StateToSet>, connectionId: Optional<String>) {
        if ("FHT".equals(device.xmlListDevice.type, ignoreCase = true) && statesToSet.size > 1) {
            setSubStatesForFHT(device, statesToSet, connectionId)
        } else {
            for (toSet in statesToSet) {
                setSubState(device, toSet.key, toSet.value, connectionId, false)
            }
        }
        update(device.xmlListDevice, connectionId.orNull())
    }

    fun setAttribute(device: XmlListDevice, attributeName: String, attributeValue: String) {

        commandExecutionService.executeSafely(Command("attr ${device.name} $attributeName $attributeValue"),
                object : CommandExecutionService.SuccessfulResultListener() {
                    override fun onResult(result: String) {
                        device.setAttribute(attributeName, attributeValue)
                        update(device)
                    }
                })
    }

    private fun setSubStatesForFHT(device: FhemDevice, statesToSet: List<StateToSet>, connectionId: Optional<String>) {
        val partitions = partition(statesToSet, 8)
        partitions.map { fhtConcat(it) }
                .forEach { setState(device, it, connectionId, false) }
    }

    private fun fhtConcat(toConcat: List<StateToSet>?): String =
            (toConcat ?: emptyList())
                    .joinToString(separator = " ") { it.key + " " + it.value }

    fun update(device: XmlListDevice, connectionId: String? = null) {
        val configuration = deviceConfigurationProvider.configurationFor(device)
        val delay = configuration.delayForUpdateAfterCommand
        if (delay > 0) {
            val updateIntent = Intent(DO_REMOTE_UPDATE)
                    .putExtra(DEVICE_NAME, device.name)
                    .putExtra(CONNECTION_ID, connectionId)
                    .setClass(applicationContext, RoomListUpdateIntentService::class.java)
            val alarmManager = applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.set(AlarmManager.RTC, (delay * 1000).toLong(), PendingIntent.getService(applicationContext, 0, updateIntent, PendingIntent.FLAG_UPDATE_CURRENT))
        } else {
            deviceListUpdateService.updateSingleDevice(device.name, connectionId)
        }
    }
}
