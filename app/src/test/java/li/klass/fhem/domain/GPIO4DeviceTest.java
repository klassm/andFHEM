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

import static org.assertj.core.api.Assertions.assertThat;

public class GPIO4DeviceTest extends DeviceXMLParsingBase {
    @Test
    public void testForCorrectlySetAttributes() {
        GenericDevice device = getDeviceFor("temp", GenericDevice.class);

        assertThat(stateValueFor(device, "temperature")).isEqualTo("22.937 (°C)");
        assertThat(device.getState()).isEqualTo("22.937 °C (Mittelwert: 22.7 °C)");
    }

    @Test
    public void testDS18B20Device() {
        GenericDevice device = getDeviceFor("DS18B20", GenericDevice.class);
        assertThat(device).isNotNull();

        assertThat(device.isSupported()).isEqualTo(true);
        assertThat(stateValueFor(device, "temperature")).isEqualTo("20.437 (°C)");
    }

    @Test
    public void testOtherModelsCanBeRead() {
        assertThat(getDeviceFor("Sensor_5", GenericDevice.class)).isNotNull();
        assertThat(getDeviceFor("Sensor_4", GenericDevice.class)).isNotNull();
        assertThat(getDeviceFor("Sensor_3", GenericDevice.class)).isNotNull();
        assertThat(getDeviceFor("Sensor_2", GenericDevice.class)).isNotNull();
        assertThat(getDeviceFor("Sensor_1", GenericDevice.class)).isNotNull();
    }

    @Override
    protected String getFileName() {
        return "gpio4.xml";
    }
}
