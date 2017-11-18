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
import com.google.common.base.Optional
import li.klass.fhem.constants.Actions
import li.klass.fhem.constants.BundleExtraKeys
import li.klass.fhem.domain.AtDevice
import li.klass.fhem.domain.core.FhemDevice
import li.klass.fhem.update.backend.DeviceListService
import li.klass.fhem.update.backend.command.execution.Command
import li.klass.fhem.update.backend.command.execution.CommandExecutionService
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AtService @Inject constructor(
        private val commandExecutionService: CommandExecutionService,
        private val deviceListService: DeviceListService,
        private val genericDeviceService: GenericDeviceService
) {

    fun createNew(timerName: String, hour: Int, minute: Int, second: Int, repetition: String, type: String,
                  targetDeviceName: String, targetState: String, targetStateAppendix: String, isActive: Boolean,
                  context: Context) {
        val device = AtDevice()

        setValues(hour, minute, second, repetition, type, targetDeviceName, targetState, targetStateAppendix, device)

        val definition = device.toFHEMDefinition()
        val command = "define $timerName at $definition"
        commandExecutionService.executeSafely(Command(command), context, object : CommandExecutionService.SuccessfulResultListener() {
            override fun onResult(result: String) {
                handleDisabled(timerName, isActive, context)

                context.sendBroadcast(Intent(Actions.DO_UPDATE)
                        .putExtra(BundleExtraKeys.DO_REFRESH, true))
            }
        })
    }

    private fun setValues(hour: Int, minute: Int, second: Int, repetition: String, type: String, targetDeviceName: String, targetState: String, targetStateAppendix: String, device: AtDevice) {
        device.setHour(hour)
        device.setMinute(minute)
        device.setSecond(second)
        device.repetition = AtDevice.AtRepetition.valueOf(repetition)
        device.timerType = AtDevice.TimerType.valueOf(type)
        device.targetDevice = targetDeviceName
        device.targetState = targetState
        device.targetStateAddtionalInformation = targetStateAppendix
    }

    fun modify(timerName: String, hour: Int, minute: Int, second: Int, repetition: String, type: String,
               targetDeviceName: String, targetState: String, targetStateAppendix: String, isActive: Boolean, context: Context) {
        val device = deviceListService.getDeviceForName<FhemDevice>(timerName)

        if (device == null || device !is AtDevice) {
            LOG.info("cannot find device for {}", timerName)
            return
        }

        setValues(hour, minute, second, repetition, type, targetDeviceName, targetState, targetStateAppendix, device)
        val definition = device.toFHEMDefinition()
        val command = "modify $timerName $definition"

        commandExecutionService.executeSafely(Command(command), context, object : CommandExecutionService.SuccessfulResultListener() {
            override fun onResult(result: String) {
                handleDisabled(timerName, isActive, context)
                genericDeviceService.update(device, context, Optional.absent())
            }
        })

    }

    private fun handleDisabled(timerName: String, isActive: Boolean, context: Context): String? =
            commandExecutionService.executeSync(Command(String.format("attr %s %s %s", timerName, "disable", if (isActive) "0" else "1")), context)

    companion object {

        private val LOG = LoggerFactory.getLogger(AtService::class.java)
    }
}
