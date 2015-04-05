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

import static org.assertj.core.api.Assertions.assertThat;

public class WeatherDeviceTest extends DeviceXMLParsingBase {
    @Test
    public void testForCorrectlySetAttributes() {
        WeatherDevice device = getDefaultDevice(WeatherDevice.class);

        assertThat(device.getName()).isEqualTo(DEFAULT_TEST_DEVICE_NAME);
        assertThat(device.getRoomConcatenated()).isEqualTo(DEFAULT_TEST_ROOM_NAME);

        assertThat(device.getIcon()).isEqualTo("cloudy.png");
        assertThat(device.getCondition()).isEqualTo("Bewölkt");
        assertThat(device.getHumidity()).isEqualTo("43.0 (%)");
        assertThat(device.getTemperature()).isEqualTo("19.0 (°C)");
        assertThat(device.getWind()).isEqualTo("NW mit 11 km/h");
        assertThat(device.getState()).isEqualTo("T: 19  H: 43  W: 11");

        List<WeatherDevice.WeatherDeviceForecast> forecasts = device.getForecasts();
        assertThat(forecasts.size()).isEqualTo(3);

        WeatherDevice.WeatherDeviceForecast firstForecast = forecasts.get(0);
        assertThat(firstForecast.getIcon()).isEqualTo("chance_of_rain.png");
        assertThat(firstForecast.getDayOfWeek()).isEqualTo("Mi.");
        assertThat(firstForecast.getCondition()).isEqualTo("Vereinzelt Regen");
        assertThat(firstForecast.getDate()).isEqualTo("2012-05-02");
        assertThat(firstForecast.getHighTemperature()).isEqualTo("20 (°C)");
        assertThat(firstForecast.getLowTemperature()).isEqualTo("9 (°C)");

        WeatherDevice.WeatherDeviceForecast secondForecast = forecasts.get(1);
        assertThat(secondForecast.getIcon()).isEqualTo("mostly_sunny.png");
        assertThat(secondForecast.getDayOfWeek()).isEqualTo("Do.");
        assertThat(secondForecast.getCondition()).isEqualTo("Meist sonnig");
        assertThat(secondForecast.getDate()).isEqualTo("2012-05-03");
        assertThat(secondForecast.getHighTemperature()).isEqualTo("19 (°C)");
        assertThat(secondForecast.getLowTemperature()).isEqualTo("8 (°C)");

        WeatherDevice.WeatherDeviceForecast thirdForecast = forecasts.get(2);
        assertThat(thirdForecast.getIcon()).isEqualTo("sunny.png");
        assertThat(thirdForecast.getDayOfWeek()).isEqualTo("Fr.");
        assertThat(thirdForecast.getCondition()).isEqualTo("Klar");
        assertThat(thirdForecast.getDate()).isEqualTo("2012-05-04");
        assertThat(thirdForecast.getHighTemperature()).isEqualTo("19 (°C)");
        assertThat(thirdForecast.getLowTemperature()).isEqualTo("10 (°C)");

        assertThat(device.getSetList().getEntries().size()).isEqualTo(0);

        assertThat(device.getLogDevices()).isNotNull();
        assertThat(device.getDeviceCharts().size()).isEqualTo(1);
    }

    @Override
    protected String getFileName() {
        return "weather.xml";
    }
}
