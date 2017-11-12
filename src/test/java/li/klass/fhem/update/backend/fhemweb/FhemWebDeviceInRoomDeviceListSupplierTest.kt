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

import android.content.Context
import com.google.common.base.Optional
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import li.klass.fhem.connection.backend.ConnectionService
import li.klass.fhem.constants.XmllistKey
import li.klass.fhem.domain.FHEMWEBDevice
import li.klass.fhem.domain.core.RoomDeviceList
import li.klass.fhem.settings.SettingsKeys.FHEMWEB_DEVICE_NAME
import li.klass.fhem.update.backend.RoomListService
import li.klass.fhem.update.backend.xmllist.XmlListDevice
import li.klass.fhem.util.ApplicationProperties
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class FhemWebDeviceInRoomDeviceListSupplierTest {
    @Test
    fun should_find_no_fhemweb_device_if_none_is_present() {
        // given
        val applicationProperties = mock<ApplicationProperties>()
        val context = mock<Context>()
        val emptyRoomDeviceList = RoomDeviceList("bla")
        val connectionService = mock<ConnectionService> {
            on { getSelectedId(context) } doReturn "123"
        }
        val roomListService = mock<RoomListService> {
            on { getAllRoomsDeviceList(Optional.of("123"), context) } doReturn emptyRoomDeviceList
        }

        val supplier = FhemWebDeviceInRoomDeviceListSupplier(applicationProperties, connectionService, roomListService, context)

        // when
        val result = supplier.get()

        // then
        assertThat(result).isNull()
    }

    @Test
    fun should_find_one_fhemweb_device_if_one_is_present() {
        // given
        val applicationProperties = mock<ApplicationProperties>()
        val context = mock<Context>()
        val device = fhemwebDeviceFor("device")
        val deviceList = RoomDeviceList("bla")
                .addDevice(device, context)
        val connectionService = mock<ConnectionService> {
            on { getSelectedId(context) } doReturn "123"
        }
        val roomListService = mock<RoomListService> {
            on { getAllRoomsDeviceList(Optional.of("123"), context) } doReturn deviceList
        }

        val supplier = FhemWebDeviceInRoomDeviceListSupplier(applicationProperties, connectionService, roomListService, context)

        // when
        val result = supplier.get()

        // then
        assertThat(result).isEqualTo(device)
    }

    @Test
    fun should_use_connection_port() {
        // given
        val applicationProperties = mock<ApplicationProperties>() // returns null as qualifier
        val context = mock<Context>()
        val port = 123
        val incorrectDevice = fhemwebDeviceFor("incorrectDevice")
        val correctDevice = fhemwebDeviceFor("correctDevice")
        correctDevice.xmlListDevice.setInternal(XmllistKey.Internal.FhemWeb.port, port.toString())
        val deviceList = RoomDeviceList("bla")
                .addDevice(incorrectDevice, context)
                .addDevice(correctDevice, context)

        val roomListService = mock<RoomListService> {
            on { getAllRoomsDeviceList(Optional.of("123"), context) } doReturn deviceList
        }
        val connectionService = mock<ConnectionService> {
            on { getPortOfSelectedConnection(context) } doReturn port
            on { getSelectedId(context) } doReturn "123"
        }

        val supplier = FhemWebDeviceInRoomDeviceListSupplier(applicationProperties, connectionService, roomListService, context)

        // when
        val result = supplier.get()

        // then
        assertThat(result).isEqualTo(correctDevice)
    }

    @Test
    fun should_use_some_device_if_connection_port_does_not_match() {
        // given
        val applicationProperties = mock<ApplicationProperties>() // returns null as qualifier
        val context = mock<Context>()
        val port = 123
        val device2 = fhemwebDeviceFor("device2")
        val device1 = fhemwebDeviceFor("device1")
        device1.xmlListDevice.setInternal(XmllistKey.Internal.FhemWeb.port, "124")

        val deviceList = RoomDeviceList("bla")
                .addDevice(device2, context)
                .addDevice(device1, context)

        val roomListService = mock<RoomListService> {
            on { getAllRoomsDeviceList(Optional.of("123"), context) } doReturn deviceList
        }
        val connectionService = mock<ConnectionService> {
            on { getPortOfSelectedConnection(context) } doReturn port
            on { getSelectedId(context) } doReturn "123"
        }

        val supplier = FhemWebDeviceInRoomDeviceListSupplier(applicationProperties, connectionService, roomListService, context)

        // when
        val result = supplier.get()

        // then
        assertThat(result).isIn(device1, device2)
    }

    @Test
    fun should_use_qualifier() {
        // given
        val qualifier = "myQualifier"
        val context = mock<Context>()
        val applicationProperties = mock<ApplicationProperties> {
            on { getStringSharedPreference(FHEMWEB_DEVICE_NAME, null, context) } doReturn qualifier
        }
        val device1 = fhemwebDeviceFor("device1")
        val device2 = fhemwebDeviceFor("device2" + qualifier)
        val deviceList = RoomDeviceList("bla")
                .addDevice(device2, context)
                .addDevice(device1, context)

        val roomListService = mock<RoomListService> {
            on { getAllRoomsDeviceList(Optional.of("123"), context) } doReturn deviceList
        }
        val connectionService = mock<ConnectionService> {
            on { getSelectedId(context) } doReturn "123"
        }

        val supplier = FhemWebDeviceInRoomDeviceListSupplier(applicationProperties, connectionService, roomListService, context)

        // when
        val result = supplier.get()

        // then
        assertThat(result).isEqualTo(device2)
    }

    private fun fhemwebDeviceFor(name: String): FHEMWEBDevice {
        val device = FHEMWEBDevice()
        device.xmlListDevice = XmlListDevice("FHEMWEB")
        device.xmlListDevice.setInternal(XmllistKey.Internal.name, name)
        device.xmlListDevice.setAttribute(XmllistKey.Attribute.group, "default")
        return device
    }
}