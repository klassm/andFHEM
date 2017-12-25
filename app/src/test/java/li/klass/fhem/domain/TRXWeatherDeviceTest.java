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
import li.klass.fhem.domain.core.FhemDevice;

import static org.assertj.core.api.Assertions.assertThat;

public class TRXWeatherDeviceTest extends DeviceXMLParsingBase {
    @Test
    public void testForCorrectlySetAttributes() {
        FhemDevice device = getDefaultDevice();

        assertThat(device.getName()).isEqualTo(DEFAULT_TEST_DEVICE_NAME);
        assertThat(device.getRoomConcatenated()).isEqualTo(DEFAULT_TEST_ROOM_NAME);

        assertThat(stateValueFor(device, "temperature")).isEqualTo("21.1 (°C)");
        assertThat(stateValueFor(device, "battery")).isEqualTo("ok");
        assertThat(device.getState()).isEqualTo("T: 21.1 BAT: ok");

        assertThat(device.getSetList().getEntries().size()).isEqualTo(0);
    }

    @Test
    public void testForCorrectlySetAttributesInHumidityDevice() {
        FhemDevice device = getDeviceFor("device1");

        assertThat(stateValueFor(device, "humidity")).isEqualTo("59.0 (%)");
        assertThat(stateValueFor(device, "dewpoint")).isEqualTo("11.1 (°C)");
    }

    @Test
    public void testRainAttributes() {
        FhemDevice device = getDeviceFor("rain");
        assertThat(stateValueFor(device, "rain_total")).isEqualTo("343 (l/m²)");
    }

    @Test
    public void testWindAttributes() {
        FhemDevice device = getDeviceFor("wind");
        assertThat(stateValueFor(device, "wind_avspeed")).isEqualTo("3.0 (km/h)");
        assertThat(stateValueFor(device, "wind_dir")).isEqualTo("180 S");
        assertThat(stateValueFor(device, "wind_speed")).isEqualTo("0.0 (km/h)");
        assertThat(stateValueFor(device, "windchill")).isEqualTo("10.4 (°C)");
    }

    @Override
    protected String getFileName() {
        return "trx_weather.xml";
    }
}
