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

public class HMSDeviceTest extends DeviceXMLParsingBase {
    @Override
    public void before() throws Exception {
        super.before();
    }

    @Test
    public void should_read_temperature_humidity_device_correctly() {
        HMSDevice device = getDefaultDevice(HMSDevice.class);

        assertThat(device.getName()).isEqualTo(DEFAULT_TEST_DEVICE_NAME);
        assertThat(device.getRoomConcatenated()).isEqualTo(DEFAULT_TEST_ROOM_NAME);

        assertThat(device.getHumidity()).isEqualTo("40.0 (%)");
        assertThat(device.getTemperature()).isEqualTo("12.6 (°C)");
        assertThat(device.getModel()).isEqualTo("HMS100T");
        assertThat(device.getBattery()).isEqualTo("ok");
        assertThat(device.getSwitchDetect()).isEqualTo("on");
        assertThat(device.getState()).isEqualTo("T: 12.6  Bat: ok");

        assertThat(device.getSetList().getEntries()).isEmpty();

        assertThat(device.getLogDevices()).isNotNull();
        assertThat(device.getDeviceCharts()).hasSize(2);
    }

    @Test
    public void should_read_waterDetect_device_correctly() {
        HMSDevice device = getDeviceFor("water", HMSDevice.class);

        assertThat(device.getTemperature()).isNull();
        assertThat(device.getHumidity()).isNull();

        assertThat(device.getWaterDetect()).isEqualTo("no");
        assertThat(device.getDeviceCharts()).isEmpty();
    }

    @Override
    protected String getFileName() {
        return "hms.xml";
    }
}
