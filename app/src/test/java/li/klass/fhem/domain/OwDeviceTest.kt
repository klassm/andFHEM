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

package li.klass.fhem.domain

import org.junit.Test

import li.klass.fhem.domain.core.DeviceXMLParsingBase

import org.assertj.core.api.Assertions.assertThat

class OwDeviceTest : DeviceXMLParsingBase() {
    @Test
    fun should_read_temperatures_correctly() {
        val device = getDeviceFor("Aussentemperatur", GenericDevice::class.java)

        assertThat(device.name).isEqualTo("Aussentemperatur")
        assertThat(device.roomConcatenated).isEqualTo(DeviceXMLParsingBase.DEFAULT_TEST_ROOM_NAME)
        assertThat(device.isSupported).isTrue()

        val device1 = getDeviceFor("Vorlauf", GenericDevice::class.java)
        assertThat(device1.isSupported).isTrue()
    }

    @Test
    fun should_read_counter_values_correctly() {
        val device = getDeviceFor("DS2413A", GenericDevice::class.java)
        assertThat(stateValueFor(device, "PIO.A")).isEqualTo("2")
        assertThat(stateValueFor(device, "PIO.B")).isEqualTo("3")

        assertThat(device.isSupported).isTrue()
    }

    @Test
    fun should_handle_switch_devices() {
        val device = getDeviceFor("Relais1", GenericDevice::class.java)
        assertThat(device.state).isEqualTo("ein")
        assertThat(device.internalState).isEqualTo("PIO 1")
    }

    @Test
    fun should_handle_single_counter_devices() {
        val device = getDeviceFor("Relais2", GenericDevice::class.java)
        assertThat(stateValueFor(device, "PIO")).isEqualTo("0")
    }

    @Test
    fun should_handle_devices_with_more_than_two_PIOs() {
        val device = getDeviceFor("OWSw", GenericDevice::class.java)
        assertThat(stateValueFor(device, "PIO.0")).isEqualTo("0")
        assertThat(stateValueFor(device, "PIO.1")).isEqualTo("1")
        assertThat(stateValueFor(device, "PIO.2")).isEqualTo("0")
        assertThat(stateValueFor(device, "PIO.3")).isEqualTo("1")
    }

    @Test
    fun should_infer_temperature_subtype_from_temperature_in_state() {
        val device = getDeviceFor("Wohnzimmer", GenericDevice::class.java)
        assertThat(device).isNotNull()
    }

    override fun getFileName(): String = "owdevice.xml"
}
