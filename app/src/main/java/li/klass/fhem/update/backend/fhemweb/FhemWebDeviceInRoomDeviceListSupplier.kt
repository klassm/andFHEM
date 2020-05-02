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

package li.klass.fhem.update.backend.fhemweb

import li.klass.fhem.connection.backend.ConnectionService
import li.klass.fhem.domain.core.FhemDevice
import li.klass.fhem.settings.SettingsKeys.FHEMWEB_DEVICE_NAME
import li.klass.fhem.update.backend.DeviceListService
import li.klass.fhem.util.ApplicationProperties
import org.apache.commons.lang3.StringUtils.stripToNull
import org.slf4j.LoggerFactory
import java.util.*
import javax.inject.Inject

class FhemWebDeviceInRoomDeviceListSupplier
@Inject constructor(
        private val applicationProperties: ApplicationProperties,
        private val connectionService: ConnectionService,
        private val deviceListService: DeviceListService
) {

    fun get(): FhemDevice? {
        val deviceList = deviceListService.getAllRoomsDeviceList(connectionService.getSelectedId())
        val fhemWebDevices = deviceList.getDevicesOfType("FHEMWEB")
        return findFhemWebDevice(fhemWebDevices)
    }

    private fun findFhemWebDevice(devices: List<FhemDevice>): FhemDevice? {
        if (devices.isEmpty()) {
            logger.info("findFhemWebDevice - cannot find a device because devices are empty")
            return null
        }
        if (devices.size == 1) {
            logger.info("findFhemWebDevice - only got one device, so using '${devices[0].name}'")
            return devices[0]
        }

        val deviceForQualifier = findDeviceForQualifier(devices)
        if (deviceForQualifier != null) {
            logger.info("findFhemWebDevice - found device for qualifier, using '${deviceForQualifier.name}'")
            return deviceForQualifier
        }
        val deviceForPort = findDeviceForPort(devices)
        if (deviceForPort != null) {
            logger.info("findFhemWebDevice - found device for port, using '${deviceForPort.name}'")
            return deviceForPort
        }

        val someDevice = devices[0]
        logger.info("findFhemWebDevice - have no idea what device to choose, so choosing random device:  '${someDevice.name}'")
        return someDevice
    }

    private fun findDeviceForPort(devices: List<FhemDevice>): FhemDevice? {
        val port = connectionService.getPortOfSelectedConnection()
        return devices.firstOrNull {
            it.xmlListDevice.let {
                it.type == "FHEMWEB" && it.attributeValueFor("port") == port.toString()
            }
        }
    }

    private fun findDeviceForQualifier(devices: List<FhemDevice>): FhemDevice? {
        val qualifier = getQualifier()
        logger.debug("findDeviceForQualifier - qualifier is '$qualifier'")
        return devices.firstOrNull {
            it.name.toUpperCase(Locale.getDefault()).contains(qualifier)
        }
    }

    private fun getQualifier(): String =
            (stripToNull(applicationProperties.getStringSharedPreference(FHEMWEB_DEVICE_NAME, null))
                    ?: DEFAULT_FHEMWEB_QUALIFIER)
                    .toUpperCase(Locale.getDefault())

    companion object {
        private const val DEFAULT_FHEMWEB_QUALIFIER = "andFHEM"
        private val logger = LoggerFactory.getLogger(FhemWebDeviceInRoomDeviceListSupplier::class.java)
    }
}
