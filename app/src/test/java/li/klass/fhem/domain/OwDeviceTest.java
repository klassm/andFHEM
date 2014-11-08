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

package li.klass.fhem.domain;

import org.junit.Test;

import li.klass.fhem.domain.core.DeviceXMLParsingBase;

import static org.fest.assertions.api.Assertions.assertThat;

public class OwDeviceTest extends DeviceXMLParsingBase {
    @Test
    public void should_read_temperatures_correctly() {
        OwDevice device = getDeviceFor("Aussentemperatur");

        assertThat(device.getName()).isEqualTo("Aussentemperatur");
        assertThat(device.getRoomConcatenated()).isEqualTo(DEFAULT_TEST_ROOM_NAME);
        assertThat(device.getSubType()).isEqualTo(OwDevice.SubType.TEMPERATURE);
        assertThat(device.getState()).isEqualTo("0.0 (°C)");
        assertThat(device.isSupported()).isTrue();
        assertThat(device.supportsToggle()).isFalse();

        OwDevice device1 = getDeviceFor("Vorlauf");
        assertThat(device1.getSubType()).isEqualTo(OwDevice.SubType.TEMPERATURE);
        assertThat(device1.getState()).isEqualTo("19.1 (°C)");
        assertThat(device1.isSupported()).isTrue();
        assertThat(device1.supportsToggle()).isFalse();
    }

    @Test
    public void should_read_counter_values_correctly() {
        OwDevice device = getDeviceFor("DS2413A");
        assertThat(device.getInputA()).isEqualTo("2");
        assertThat(device.getInputB()).isEqualTo("3");

        assertThat(device.supportsToggle()).isFalse();
        assertThat(device.isSupported()).isTrue();
    }

    @Test
    public void should_handle_switch_devices() {
        OwDevice device = getDeviceFor("Relais1");
        assertThat(device.supportsToggle()).isTrue();
        assertThat(device.getState()).isEqualTo("ein");
        assertThat(device.getInternalState()).isEqualTo("PIO 1");
        assertThat(device.isOnByState()).isTrue();
    }

    @Test
    public void should_handle_single_counter_devices() {
        OwDevice device = getDeviceFor("Relais2");
        assertThat(device.getInputA()).isEqualTo("0");
    }

    @Test
    public void should_handle_devices_with_more_than_two_PIOs() {
        OwDevice device = getDeviceFor("OWSw");
        assertThat(device.getInputA()).isEqualTo("0");
        assertThat(device.getInputB()).isEqualTo("1");
        assertThat(device.getInputC()).isEqualTo("0");
        assertThat(device.getInputD()).isEqualTo("1");
    }

    @Test
    public void should_infer_temperature_subtype_from_temperature_in_state() {
        OwDevice device = getDeviceFor("Wohnzimmer");
        assertThat(device.getSubType()).isEqualTo(OwDevice.SubType.TEMPERATURE);
    }

    @Override
    protected String getFileName() {
        return "owdevice.xml";
    }
}
