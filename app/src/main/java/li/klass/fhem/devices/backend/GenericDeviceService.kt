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
import li.klass.fhem.appwidget.update.AppWidgetUpdateService
import li.klass.fhem.constants.Actions.DO_REMOTE_UPDATE
import li.klass.fhem.constants.BundleExtraKeys.CONNECTION_ID
import li.klass.fhem.constants.BundleExtraKeys.DEVICE_NAME
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
        private val deviceListUpdateService: DeviceListUpdateService,
        private val widgetUpdateService: AppWidgetUpdateService
) {
    private val applicationContext get() = application.applicationContext

    @JvmOverloads
    fun setState(device: XmlListDevice, targetState: String, connectionId: String?, invokeUpdate: Boolean = true) {
        commandExecutionService.executeSafely(Command("set ${device.name} $targetState", connectionId), invokePostCommandActions(device, invokeUpdate, "state", targetState, connectionId))

        device.setState("STATE", targetState)
        device.setInternal("STATE", targetState)
    }

    fun setSubState(device: XmlListDevice, subStateName: String, value: String, connectionId: String?, invokeDeviceUpdate: Boolean = true) {
        if (subStateName.equals("state", ignoreCase = true)) {
            setState(device, value, connectionId, invokeDeviceUpdate)
            return
        }

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

    private fun invokePostCommandActions(device: XmlListDevice, invokeUpdate: Boolean,
                                         stateName: String, toSet: String,
                                         connectionId: String?): CommandExecutionService.ResultListener {
        return object : CommandExecutionService.SuccessfulResultListener() {
            override fun onResult(result: String) {

                if (invokeUpdate) {
                    update(device, connectionId)
                }

                device.setState(stateName, toSet)
                widgetUpdateService.updateWidgetsFor(device.name)
                Tasker.sendTaskerNotifyIntent(applicationContext, device.name, stateName, toSet)
                Tasker.requestQuery(applicationContext)
            }
        }
    }

    fun setSubStates(device: XmlListDevice, statesToSet: List<StateToSet>, connectionId: String?) {
        if ("FHT".equals(device.type, ignoreCase = true) && statesToSet.size > 1) {
            setSubStatesForFHT(device, statesToSet, connectionId)
        } else {
            for (toSet in statesToSet) {
                setSubState(device, toSet.key, toSet.value, connectionId, false)
            }
        }
        update(device, connectionId)
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

    private fun setSubStatesForFHT(device: XmlListDevice, statesToSet: List<StateToSet>, connectionId: String?) {
        statesToSet.chunked(8).map { fhtConcat(it) }
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
            alarmManager.set(
                AlarmManager.RTC,
                (delay * 1000).toLong(),
                PendingIntent.getService(
                    applicationContext,
                    0,
                    updateIntent,
                    PendingIntent.FLAG_IMMUTABLE
                )
            )
        } else {
            deviceListUpdateService.updateSingleDevice(device.name, connectionId)
        }
    }
}
