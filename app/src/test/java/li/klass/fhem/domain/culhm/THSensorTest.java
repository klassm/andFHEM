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

package li.klass.fhem.domain.culhm;

import org.junit.Test;

import li.klass.fhem.domain.CULHMDevice;
import li.klass.fhem.domain.core.DeviceXMLParsingBase;

import static org.fest.assertions.api.Assertions.assertThat;

public class THSensorTest extends DeviceXMLParsingBase {
    @Test
    public void testForCorrectlySetAttributes() {
        CULHMDevice device = getDefaultDevice();

        assertThat(device.getName()).isEqualTo(DEFAULT_TEST_DEVICE_NAME);
        assertThat(device.getRoomConcatenated()).isEqualTo(DEFAULT_TEST_ROOM_NAME);

        assertThat(device.getState()).isEqualTo("T: 14.6 H: 67");
        assertThat(device.getSubType()).isEqualTo(CULHMDevice.SubType.TH);
        assertThat(device.supportsDim()).isEqualTo(false);

        assertThat(device.getMeasuredTemp()).isEqualTo("-2.4 (°C)");
        assertThat(device.getHumidity()).isEqualTo("67 (%)");

        assertThat(device.getLogDevices()).isNotNull();
        assertThat(device.getDeviceCharts().size()).isEqualTo(1);

        assertThat(device.isSupported()).isEqualTo(true);
    }

    @Test
    public void testOC3Sensor() {
        CULHMDevice device = getDeviceFor("oc3");

        assertThat(device).isNotNull();
        assertThat(device.getMeasuredTemp()).isEqualTo("5.1 (°C)");
        assertThat(device.getHumidity()).isEqualTo("92 (%)");
        assertThat(device.getBrightness()).isEqualTo("9");
        assertThat(device.getSunshine()).isEqualTo("224");
        assertThat(device.getIsRaining()).isEqualTo("no");
        assertThat(device.getRain()).isEqualTo("74.045 (l/m2)");
        assertThat(device.getWindDirection()).isEqualTo("300 (°)");
        assertThat(device.getWindSpeed()).isEqualTo("6.4 (m/s)");
    }

    @Override
    protected String getFileName() {
        return "THSensor.xml";
    }
}
