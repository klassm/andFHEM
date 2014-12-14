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

import java.util.List;

import li.klass.fhem.domain.core.DeviceXMLParsingBase;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.Is.is;

public class WeatherDeviceTest extends DeviceXMLParsingBase {
    @Test
    public void testForCorrectlySetAttributes() {
        WeatherDevice device = getDefaultDevice();

        assertThat(device.getName(), is(DEFAULT_TEST_DEVICE_NAME));
        assertThat(device.getRoomConcatenated(), is(DEFAULT_TEST_ROOM_NAME));

        assertThat(device.getIcon(), is("cloudy.png"));
        assertThat(device.getCondition(), is("Bewölkt"));
        assertThat(device.getHumidity(), is("43 (%)"));
        assertThat(device.getTemperature(), is("19 (°C)"));
        assertThat(device.getWind(), is("NW mit 11 km/h"));
        assertThat(device.getState(), is("T: 19  H: 43  W: 11"));

        List<WeatherDevice.WeatherDeviceForecast> forecasts = device.getForecasts();
        assertThat(forecasts.size(), is(3));

        WeatherDevice.WeatherDeviceForecast firstForecast = forecasts.get(0);
        assertThat(firstForecast.getIcon(), is("chance_of_rain.png"));
        assertThat(firstForecast.getDayOfWeek(), is("Mi."));
        assertThat(firstForecast.getCondition(), is("Vereinzelt Regen"));
        assertThat(firstForecast.getDate(), is("2012-05-02"));
        assertThat(firstForecast.getHighTemperature(), is("20 (°C)"));
        assertThat(firstForecast.getLowTemperature(), is("9 (°C)"));

        WeatherDevice.WeatherDeviceForecast secondForecast = forecasts.get(1);
        assertThat(secondForecast.getIcon(), is("mostly_sunny.png"));
        assertThat(secondForecast.getDayOfWeek(), is("Do."));
        assertThat(secondForecast.getCondition(), is("Meist sonnig"));
        assertThat(secondForecast.getDate(), is("2012-05-03"));
        assertThat(secondForecast.getHighTemperature(), is("19 (°C)"));
        assertThat(secondForecast.getLowTemperature(), is("8 (°C)"));

        WeatherDevice.WeatherDeviceForecast thirdForecast = forecasts.get(2);
        assertThat(thirdForecast.getIcon(), is("sunny.png"));
        assertThat(thirdForecast.getDayOfWeek(), is("Fr."));
        assertThat(thirdForecast.getCondition(), is("Klar"));
        assertThat(thirdForecast.getDate(), is("2012-05-04"));
        assertThat(thirdForecast.getHighTemperature(), is("19 (°C)"));
        assertThat(thirdForecast.getLowTemperature(), is("10 (°C)"));

        assertThat(device.getSetList().getEntries().size(), is(0));

        assertThat(device.getLogDevices(), is(notNullValue()));
        assertThat(device.getDeviceCharts().size(), is(1));

        device.readICON("");
    }

    @Override
    protected String getFileName() {
        return "weather.xml";
    }
}
