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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;

public class TwilightDeviceTest extends DeviceXMLParsingBase {
    @Test
    public void testForCorrectlySetAttributes() {
        TwilightDevice device = getDefaultDevice();

        assertThat(device.getName(), is(DEFAULT_TEST_DEVICE_NAME));
        assertThat(device.getRoomConcatenated(), is(DEFAULT_TEST_ROOM_NAME));

        assertThat(device.getLight(), is("Daylight"));
        assertThat(device.getNextEvent(), is("ss_weather"));
        assertThat(device.getNextEventTime(), is("21:05:07"));
        assertThat(device.getSunrise(), is("05:39:59"));
        assertThat(device.getSunriseAstronomical(), is("02:48:05"));
        assertThat(device.getSunriseCivil(), is("04:54:41"));
        assertThat(device.getSunriseIndoor(), is("06:07:50"));
        assertThat(device.getSunriseNautical(), is("04:01:49"));
        assertThat(device.getSunriseWeather(), is("05:39:59"));
        assertThat(device.getSunset(), is("21:05:07"));
        assertThat(device.getSunsetAstronomical(), is("23:57:00"));
        assertThat(device.getSunsetCivil(), is("21:50:24"));
        assertThat(device.getSunsetIndoor(), is("20:37:15"));
        assertThat(device.getSunsetNautical(), is("22:43:16"));
        assertThat(device.getSunsetWeather(), is("21:05:07"));
        assertThat(device.getState(), is("6"));

        assertThat(device.getSetList().getEntries().size(), is(0));

        assertThat(device.getLogDevice(), is(nullValue()));
        assertThat(device.getDeviceCharts().size(), is(0));
    }

    @Override
    protected String getFileName() {
        return "twilight.xml";
    }
}
