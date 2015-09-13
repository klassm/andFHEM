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

public class HMSDeviceTest extends DeviceXMLParsingBase {
    @Override
    public void before() throws Exception {
        super.before();
    }

    @Test
    public void should_read_temperature_humidity_device_correctly() {
        GenericDevice device = getDefaultDevice(GenericDevice.class);

        assertThat(device.getName()).isEqualTo(DEFAULT_TEST_DEVICE_NAME);
        assertThat(device.getRoomConcatenated()).isEqualTo(DEFAULT_TEST_ROOM_NAME);

        assertThat(stateValueFor(device, "humidity")).isEqualTo("40.0 (%)");
        assertThat(stateValueFor(device, "temperature")).isEqualTo("12.6 (°C)");
        assertThat(stateValueFor(device, "type")).isEqualTo("HMS100T");
        assertThat(stateValueFor(device, "battery")).isEqualTo("ok");
        assertThat(stateValueFor(device, "switch_detect")).isEqualTo("on");
        assertThat(device.getState()).isEqualTo("T: 12.6  Bat: ok");

        assertThat(device.getSetList().getEntries()).isEmpty();
    }

    @Test
    public void should_read_waterDetect_device_correctly() {
        GenericDevice device = getDeviceFor("water", GenericDevice.class);

        assertThat(stateValueFor(device, "temperature")).isNull();
        assertThat(stateValueFor(device, "humidity")).isNull();

        assertThat(stateValueFor(device, "water_detect")).isEqualTo("off");
    }

    @Override
    protected String getFileName() {
        return "hms.xml";
    }
}
