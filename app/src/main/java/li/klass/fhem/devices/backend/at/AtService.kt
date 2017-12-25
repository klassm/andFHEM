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

package li.klass.fhem.devices.backend.at

import li.klass.fhem.devices.backend.GenericDeviceService
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
        private val genericDeviceService: GenericDeviceService,
        private val atDefinitionParser: AtDefinitionParser
) {

    fun createNew(device: TimerDevice) {
        val definition = atDefinitionParser.toFHEMDefinition(device.definition)
        val command = "define ${device.name} at $definition"
        commandExecutionService.executeSync(Command(command))
        handleDisabled(device.name, device.isActive)
        deviceListUpdateService.updateAllDevices()
    }

    fun modify(device: TimerDevice) {
        val genericDevice = getFhemDeviceFor(device.name) ?: return

        val definition = atDefinitionParser.toFHEMDefinition(device.definition)
        val command = "modify ${device.name} $definition"

        commandExecutionService.executeSync(Command(command))
        handleDisabled(device.name, device.isActive)
        genericDeviceService.update(genericDevice.xmlListDevice)
    }

    fun getTimerDeviceFor(deviceName: String): TimerDevice? {
        val device = deviceListService.getDeviceForName(deviceName) ?: return null
        return toTimerDevice(device)
    }

    fun getTimerDevices(): List<TimerDevice> {
        return deviceListService.getAllRoomsDeviceList(null)
                .getDevicesOfType("at")
                .map { toTimerDevice(it) }
                .filter { it != null }
                .map { it!! }
                .toList()
    }

    private fun toTimerDevice(device: FhemDevice): TimerDevice? {
        val definition = device.xmlListDevice.getInternal("DEF").orNull() ?: return null
        val parsedDefinition = atDefinitionParser.parse(definition) ?: return null

        return TimerDevice(
                name = device.name,
                isActive = device.xmlListDevice.getAttribute("disable").or("0") == "0",
                definition = parsedDefinition,
                next = device.xmlListDevice.getInternal("TIMESPEC").or("??:??:??")
        )
    }

    private fun getFhemDeviceFor(deviceName: String): FhemDevice? {
        val genericDevice = deviceListService.getDeviceForName(deviceName)

        if (genericDevice == null || genericDevice.xmlListDevice.type != "at") {
            LOG.info("cannot find device for {}", deviceName)
            return null
        }
        return genericDevice
    }

    private fun handleDisabled(timerName: String, isActive: Boolean): String? =
            commandExecutionService.executeSync(Command(String.format("attr %s %s %s", timerName, "disable", if (isActive) "0" else "1")))

    companion object {
        private val LOG = LoggerFactory.getLogger(AtService::class.java)
    }
}
