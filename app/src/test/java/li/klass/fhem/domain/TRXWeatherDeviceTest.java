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

public class TRXWeatherDeviceTest extends DeviceXMLParsingBase {
    @Test
    public void testForCorrectlySetAttributes() {
        TRXWeatherDevice device = getDefaultDevice();

        assertThat(device.getName()).isEqualTo(DEFAULT_TEST_DEVICE_NAME);
        assertThat(device.getRoomConcatenated()).isEqualTo(DEFAULT_TEST_ROOM_NAME);

        assertThat(device.getTemperature()).isEqualTo("21.1 (°C)");
        assertThat(device.getBattery()).isEqualTo("ok");
        assertThat(device.getState()).isEqualTo("T: 21.1 BAT: ok");

        assertThat(device.getSetList().getEntries().size()).isEqualTo(0);

        assertThat(device.getLogDevices()).isEmpty();
        assertThat(device.getDeviceCharts().size()).isEqualTo(0);
    }

    @Test
    public void testForCorrectlySetAttributesInHumidityDevice() {
        TRXWeatherDevice device = getDeviceFor("device1");

        assertThat(device.getHumidity()).isEqualTo("59 (%)");
        assertThat(device.getDewpoint()).isEqualTo("11.1 (°C)");

        assertThat(device.getLogDevices()).isNotEmpty();
        assertThat(device.getDeviceCharts().size()).isEqualTo(3);
    }

    @Test
    public void testRainAttributes() {
        TRXWeatherDevice device = getDeviceFor("rain");
        assertThat(device.getRain()).isEqualTo("343 (l/m2)");
    }

    @Test
    public void testWindAttributes() {
        TRXWeatherDevice device = getDeviceFor("wind");
        assertThat(device.getWindAverageSpeed()).isEqualTo("3 (km/h)");
        assertThat(device.getWindDirection()).isEqualTo("180 S");
        assertThat(device.getWindSpeed()).isEqualTo("0 (km/h)");
        assertThat(device.getWindchill()).isEqualTo("10.4 (°C)");
    }

    @Override
    protected String getFileName() {
        return "trx_weather.xml";
    }
}
