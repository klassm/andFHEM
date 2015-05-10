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
import static org.assertj.core.data.Offset.offset;

public class PIDDeviceTest extends DeviceXMLParsingBase {
    @Test
    public void testForCorrectlySetAttributes() {
        PIDDevice device = getDefaultDevice(PIDDevice.class);

        assertThat(device.getName()).isEqualTo(DEFAULT_TEST_DEVICE_NAME);
        assertThat(device.getRoomConcatenated()).isEqualTo(DEFAULT_TEST_ROOM_NAME);

        assertThat(device.getTemperature()).isEqualTo("16.8 (°C)");
        assertThat(device.getDelta()).isEqualTo("-0.800000000000001");
        assertThat(device.getState()).isEqualTo("16.8 (delta -0.800000000000001)");
        assertThat(device.getDesiredTemp()).isEqualTo(16, offset(0.001));

        assertThat(device.getSetList().getEntries()).isNotEmpty();
    }

    @Test
    public void should_read_PID20_devices() {
        PIDDevice device = getDeviceFor("eg.wohnen.pid", PIDDevice.class);
        assertThat(device).isNotNull();

        assertThat(device.getTemperature()).isEqualTo("21.37 (°C)");
        assertThat(device.getDesiredTempDesc()).isEqualTo("21.5 (°C)");
        assertThat(device.getDesiredTemp()).isEqualTo(21.5, offset(0.001));
        assertThat(device.getActuator()).isEqualTo("27 (%)");
    }

    @Override
    protected String getFileName() {
        return "pid.xml";
    }
}
