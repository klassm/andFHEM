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

package li.klass.fhem.domain

import li.klass.fhem.devices.backend.weather.WeatherService
import li.klass.fhem.devices.backend.weather.WeatherService.WeatherForecastInformation
import li.klass.fhem.domain.core.DeviceXMLParsingBase
import org.assertj.core.api.Assertions.assertThat
import org.joda.time.LocalDate
import org.junit.Test

class PROPLANTADeviceTest : DeviceXMLParsingBase() {
    @Test
    fun testForCorrectlySetAttributes() {
        val device = defaultDevice!!

        assertThat(device.name).isEqualTo(DEFAULT_TEST_DEVICE_NAME)
        assertThat(device.roomConcatenated).isEqualTo(DEFAULT_TEST_ROOM_NAME)

        assertThat(stateValueFor(device, "humidity")).isEqualTo("37.0 (%)")
        assertThat(stateValueFor(device, "temperature")).isEqualTo("14.3 (°C)")
        assertThat(stateValueFor(device, "wind")).isEqualTo("7.2 (km/h)")
        assertThat(device.state).isEqualTo("Tmin: 3 Tmax: 15")

        val forecasts = WeatherService().forecastsFor(device)
        assertThat(forecasts).containsExactly(
                WeatherForecastInformation(
                        date = LocalDate.parse("2020-05-05"),
                        temperature = "14.3 (°C)",
                        humidity = "37.0 (%)",
                        wind = "7.2 (km/h)"
                ),
                WeatherForecastInformation(
                        date = LocalDate.parse("2020-05-06"),
                        temperature = "3 (°C) - 18 (°C)",
                        moonRise = "21:17",
                        moonSet = "06:16",
                        chanceOfRain = "0 (%) - 5 (%)"
                ),
                WeatherForecastInformation(
                        date = LocalDate.parse("2020-05-07"),
                        temperature = "8 (°C) - 20 (°C)",
                        moonRise = "22:42",
                        moonSet = "06:43",
                        chanceOfRain = "0 (%) - 5 (%)"
                ),
                WeatherForecastInformation(
                        date = LocalDate.parse("2020-05-08"),
                        temperature = "8 (°C) - 23 (°C)",
                        moonRise = "--:--",
                        moonSet = "07:17",
                        chanceOfRain = "0 (%) - 5 (%)"
                ),
                WeatherForecastInformation(
                        date = LocalDate.parse("2020-05-09"),
                        temperature = "5 (°C) - 15 (°C)",
                        moonRise = "00:01",
                        moonSet = "07:59",
                        chanceOfRain = "10 (%) - 20 (%)"
                ),
                WeatherForecastInformation(
                        date = LocalDate.parse("2020-05-10"),
                        temperature = "1 (°C) - 13 (°C)",
                        moonRise = "01:09",
                        moonSet = "08:52",
                        chanceOfRain = "0 (%) - 5 (%)"
                ),
                WeatherForecastInformation(
                        date = LocalDate.parse("2020-05-11"),
                        temperature = "1 (°C) - 13 (°C)",
                        moonRise = "02:03",
                        moonSet = "09:54",
                        icon = "https://www.proplanta.de/wetterdaten/images/symbole/t2.gif"
                ),
                WeatherForecastInformation(
                        date = LocalDate.parse("2020-05-12"),
                        temperature = "2 (°C) - 17 (°C)",
                        moonRise = "02:44",
                        moonSet = "11:01",
                        icon = "https://www.proplanta.de/wetterdaten/images/symbole/t2.gif"
                ),
                WeatherForecastInformation(
                        date = LocalDate.parse("2020-05-13"),
                        temperature = "7 (°C) - 20 (°C)",
                        moonRise = "03:16",
                        moonSet = "12:10",
                        icon = "https://www.proplanta.de/wetterdaten/images/symbole/t4.gif"
                ),
                WeatherForecastInformation(
                        date = LocalDate.parse("2020-05-14"),
                        temperature = "7 (°C) - 22 (°C)",
                        moonRise = "03:41",
                        moonSet = "13:20",
                        icon = "https://www.proplanta.de/wetterdaten/images/symbole/t3.gif"
                ),
                WeatherForecastInformation(
                        date = LocalDate.parse("2020-05-15"),
                        temperature = "10 (°C) - 22 (°C)",
                        moonRise = "04:00",
                        moonSet = "14:28",
                        icon = "https://www.proplanta.de/wetterdaten/images/symbole/t7.gif"
                ),
                WeatherForecastInformation(
                        date = LocalDate.parse("2020-05-16"),
                        temperature = "12 (°C) - 23 (°C)",
                        moonRise = "04:17",
                        moonSet = "15:35",
                        icon = "https://www.proplanta.de/wetterdaten/images/symbole/t6.gif"
                ),
                WeatherForecastInformation(
                        date = LocalDate.parse("2020-05-17"),
                        temperature = "14 (°C) - 22 (°C)",
                        moonRise = "04:33",
                        moonSet = "16:41",
                        icon = "https://www.proplanta.de/wetterdaten/images/symbole/t6.gif"
                )
        )
    }

    override fun getFileName(): String = "PROPLANTA.xml"
}
