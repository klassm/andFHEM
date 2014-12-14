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

public class TwilightDeviceTest extends DeviceXMLParsingBase {
    @Test
    public void testForCorrectlySetAttributes() {
        TwilightDevice device = getDefaultDevice();

        assertThat(device.getName()).isEqualTo(DEFAULT_TEST_DEVICE_NAME);
        assertThat(device.getRoomConcatenated()).isEqualTo(DEFAULT_TEST_ROOM_NAME);

        assertThat(device.getLight()).isEqualTo("Daylight");
        assertThat(device.getNextEvent()).isEqualTo("ss_weather");
        assertThat(device.getNextEventTime()).isEqualTo("21:05:07");
        assertThat(device.getSunrise()).isEqualTo("05:39:59");
        assertThat(device.getSunriseAstronomical()).isEqualTo("02:48:05");
        assertThat(device.getSunriseCivil()).isEqualTo("04:54:41");
        assertThat(device.getSunriseIndoor()).isEqualTo("06:07:50");
        assertThat(device.getSunriseNautical()).isEqualTo("04:01:49");
        assertThat(device.getSunriseWeather()).isEqualTo("05:39:59");
        assertThat(device.getSunset()).isEqualTo("21:05:07");
        assertThat(device.getSunsetAstronomical()).isEqualTo("23:57:00");
        assertThat(device.getSunsetCivil()).isEqualTo("21:50:24");
        assertThat(device.getSunsetIndoor()).isEqualTo("20:37:15");
        assertThat(device.getSunsetNautical()).isEqualTo("22:43:16");
        assertThat(device.getSunsetWeather()).isEqualTo("21:05:07");
        assertThat(device.getState()).isEqualTo("6");

        assertThat(device.getSetList().getEntries().size()).isEqualTo(0);

        assertThat(device.getLogDevices()).isEmpty();
        assertThat(device.getDeviceCharts().size()).isEqualTo(0);
    }

    @Override
    protected String getFileName() {
        return "twilight.xml";
    }
}
