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

import io.mockk.every
import io.mockk.mockk
import li.klass.fhem.connection.backend.ConnectionService
import li.klass.fhem.constants.XmllistKey
import li.klass.fhem.domain.core.FhemDevice
import li.klass.fhem.domain.core.RoomDeviceList
import li.klass.fhem.settings.SettingsKeys.FHEMWEB_DEVICE_NAME
import li.klass.fhem.update.backend.DeviceListService
import li.klass.fhem.update.backend.xmllist.XmlListDevice
import li.klass.fhem.util.ApplicationProperties
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class FhemWebDeviceInRoomDeviceListSupplierTest {
    @Test
    fun should_find_no_fhemweb_device_if_none_is_present() {
        // given
        val applicationProperties: ApplicationProperties = mockk()
        val emptyRoomDeviceList = RoomDeviceList("bla")
        val connectionService: ConnectionService = mockk()
        every { connectionService.getSelectedId() } returns "123"
        val roomListService: DeviceListService = mockk()
        every { roomListService.getAllRoomsDeviceList("123") } returns emptyRoomDeviceList

        val supplier = FhemWebDeviceInRoomDeviceListSupplier(applicationProperties, connectionService, roomListService)

        // when
        val result = supplier.get()

        // then
        assertThat(result).isNull()
    }

    @Test
    fun should_find_one_fhemweb_device_if_one_is_present() {
        // given
        val applicationProperties: ApplicationProperties = mockk()
        val device = fhemwebDeviceFor("device")
        val deviceList = RoomDeviceList("bla")
                .addDevice(device)
        val roomListService: DeviceListService = mockk()
        every { roomListService.getAllRoomsDeviceList("123") } returns deviceList

        val connectionService: ConnectionService = mockk()
        every { connectionService.getSelectedId() } returns "123"

        val supplier = FhemWebDeviceInRoomDeviceListSupplier(applicationProperties, connectionService, roomListService)

        // when
        val result = supplier.get()

        // then
        assertThat(result).isEqualTo(device)
    }

    @Test
    fun should_use_connection_port() {
        // given
        val applicationProperties: ApplicationProperties = mockk() // returns null as qualifier
        every { applicationProperties.getStringSharedPreference(FHEMWEB_DEVICE_NAME) } returns null
        val port = 123
        val incorrectDevice = fhemwebDeviceFor("incorrectDevice")
        val correctDevice = fhemwebDeviceFor("correctDevice")
        correctDevice.xmlListDevice.setInternal(XmllistKey.Internal.FhemWeb.port, port.toString())
        val deviceList = RoomDeviceList("bla")
                .addDevice(incorrectDevice)
                .addDevice(correctDevice)

        val roomListService: DeviceListService = mockk()
        every { roomListService.getAllRoomsDeviceList("123") } returns deviceList
        val connectionService: ConnectionService = mockk()
        every { connectionService.getPortOfSelectedConnection() } returns port
        every { connectionService.getSelectedId() } returns "123"

        val supplier = FhemWebDeviceInRoomDeviceListSupplier(applicationProperties, connectionService, roomListService)

        // when
        val result = supplier.get()

        // then
        assertThat(result).isEqualTo(correctDevice)
    }

    @Test
    fun should_use_some_device_if_connection_port_does_not_match() {
        // given
        val applicationProperties: ApplicationProperties = mockk() // returns null as qualifier
        every { applicationProperties.getStringSharedPreference(FHEMWEB_DEVICE_NAME) } returns null
        val port = 123
        val device2 = fhemwebDeviceFor("device2")
        val device1 = fhemwebDeviceFor("device1")
        device1.xmlListDevice.setInternal(XmllistKey.Internal.FhemWeb.port, "124")

        val deviceList = RoomDeviceList("bla")
                .addDevice(device2)
                .addDevice(device1)

        val roomListService: DeviceListService = mockk()
        every { roomListService.getAllRoomsDeviceList("123") } returns deviceList

        val connectionService: ConnectionService = mockk()
        every { connectionService.getPortOfSelectedConnection() } returns port
        every { connectionService.getSelectedId() } returns "123"

        val supplier = FhemWebDeviceInRoomDeviceListSupplier(applicationProperties, connectionService, roomListService)

        // when
        val result = supplier.get()

        // then
        assertThat(result).isIn(device1, device2)
    }

    @Test
    fun should_use_qualifier() {
        // given
        val qualifier = "myQualifier"
        val applicationProperties: ApplicationProperties = mockk()
        every { applicationProperties.getStringSharedPreference(FHEMWEB_DEVICE_NAME, null) } returns qualifier
        val device1 = fhemwebDeviceFor("device1")
        val device2 = fhemwebDeviceFor("device2" + qualifier)
        val deviceList = RoomDeviceList("bla")
                .addDevice(device2)
                .addDevice(device1)

        val roomListService: DeviceListService = mockk()
        every { roomListService.getAllRoomsDeviceList("123") } returns deviceList

        val connectionService: ConnectionService = mockk()
        every { connectionService.getSelectedId() } returns "123"

        val supplier = FhemWebDeviceInRoomDeviceListSupplier(applicationProperties, connectionService, roomListService)

        // when
        val result = supplier.get()

        // then
        assertThat(result).isEqualTo(device2)
    }

    private fun fhemwebDeviceFor(name: String): FhemDevice {
        val xmllistDevice = XmlListDevice("FHEMWEB").apply {
            setInternal(XmllistKey.Internal.name, name)
            setAttribute(XmllistKey.Attribute.group, "default")
        }
        return FhemDevice(xmllistDevice)
    }
}