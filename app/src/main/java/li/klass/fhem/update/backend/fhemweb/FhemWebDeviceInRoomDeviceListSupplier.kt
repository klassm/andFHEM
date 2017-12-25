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

import com.google.common.base.Predicate
import com.google.common.base.Supplier
import com.google.common.collect.FluentIterable.from
import li.klass.fhem.connection.backend.ConnectionService
import li.klass.fhem.domain.core.FhemDevice
import li.klass.fhem.settings.SettingsKeys.FHEMWEB_DEVICE_NAME
import li.klass.fhem.update.backend.DeviceListService
import li.klass.fhem.util.ApplicationProperties
import org.apache.commons.lang3.StringUtils.stripToNull
import java.util.*
import javax.inject.Inject

class FhemWebDeviceInRoomDeviceListSupplier
@Inject constructor(
        private val applicationProperties: ApplicationProperties,
        private val connectionService: ConnectionService,
        private val deviceListService: DeviceListService
) : Supplier<FhemDevice?> {

    override fun get(): FhemDevice? {
        val deviceList = deviceListService.getAllRoomsDeviceList(connectionService.getSelectedId())
        val fhemWebDevices = deviceList.getDevicesOfType("FHEMWEB")
        return getIn(fhemWebDevices)
    }

    private fun getIn(devices: List<FhemDevice>): FhemDevice? {
        if (devices.isEmpty()) return null
        if (devices.size == 1) return devices[0]

        val qualifierFromPreferences: String? =
                stripToNull(applicationProperties.getStringSharedPreference(FHEMWEB_DEVICE_NAME, null))

        if (qualifierFromPreferences == null) {
            val port = connectionService.getPortOfSelectedConnection()
            val match = from(devices).filter(predicateFHEMWEBDeviceForPort(port)).first()
            if (match.isPresent) {
                return match.get() as FhemDevice
            }
        }

        val qualifier = (qualifierFromPreferences ?: DEFAULT_FHEMWEB_QUALIFIER).toUpperCase(Locale.getDefault())

        val match = from(devices).filter(predicateFHEMWEBDeviceForQualifier(qualifier)).first()
        return if (match.isPresent) {
            match.get() as FhemDevice
        } else devices[0]
    }

    private fun predicateFHEMWEBDeviceForQualifier(qualifier: String): Predicate<FhemDevice> {
        return Predicate { device ->
            (device?.name?.toUpperCase(Locale.getDefault()) ?: "").contains(qualifier)
        }
    }

    private fun predicateFHEMWEBDeviceForPort(port: Int): Predicate<FhemDevice> {
        return Predicate { device ->
            return@Predicate device?.xmlListDevice?.let {
                it.type == "FHEMWEB" && it.attributeValueFor("port").orNull() == port.toString()
            } == true
        }
    }

    companion object {
        private val DEFAULT_FHEMWEB_QUALIFIER = "andFHEM"
    }
}
