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
import com.google.common.base.Optional
import li.klass.fhem.domain.AtDevice
import li.klass.fhem.domain.core.FhemDevice
import li.klass.fhem.update.backend.DeviceListService
import li.klass.fhem.update.backend.DeviceListUpdateService
import li.klass.fhem.update.backend.command.execution.Command
import li.klass.fhem.update.backend.command.execution.CommandExecutionService
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AtService @Inject constructor(
        private val commandExecutionService: CommandExecutionService,
        private val deviceListService: DeviceListService,
        private val deviceListUpdateService: DeviceListUpdateService,
        private val genericDeviceService: GenericDeviceService
) {

    fun createNew(timerDefinition: TimerDefinition,
                  context: Context) {
        val device = AtDevice()

        setValues(device, timerDefinition)

        val definition = device.toFHEMDefinition()
        val command = "define ${timerDefinition.timerName} at $definition"
        commandExecutionService.executeSync(Command(command), context)
        handleDisabled(timerDefinition.timerName, timerDefinition.isActive, context)
        deviceListUpdateService.updateAllDevices()
    }

    private fun setValues(device: AtDevice, timerDefinition: TimerDefinition) {
        device.setHour(timerDefinition.hour)
        device.setMinute(timerDefinition.minute)
        device.setSecond(timerDefinition.second)
        device.repetition = AtDevice.AtRepetition.valueOf(timerDefinition.repetition)
        device.timerType = AtDevice.TimerType.valueOf(timerDefinition.type.trim())
        device.targetDevice = timerDefinition.targetDeviceName.trim()
        device.targetState = timerDefinition.targetState.trim()
        device.targetStateAddtionalInformation = timerDefinition.targetStateAppendix.trim()
    }

    fun modify(timerDefinition: TimerDefinition, context: Context) {
        val device = deviceListService.getDeviceForName<FhemDevice>(timerDefinition.timerName)

        if (device == null || device !is AtDevice) {
            LOG.info("cannot find device for {}", timerDefinition.timerName)
            return
        }

        setValues(device, timerDefinition)
        val definition = device.toFHEMDefinition()
        val command = "modify ${timerDefinition.timerName} $definition"

        commandExecutionService.executeSync(Command(command), context)
        handleDisabled(timerDefinition.timerName, timerDefinition.isActive, context)
        genericDeviceService.update(device, context, Optional.absent())
    }

    private fun handleDisabled(timerName: String, isActive: Boolean, context: Context): String? =
            commandExecutionService.executeSync(Command(String.format("attr %s %s %s", timerName, "disable", if (isActive) "0" else "1")), context)

    companion object {
        private val LOG = LoggerFactory.getLogger(AtService::class.java)
    }
}
