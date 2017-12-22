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

import li.klass.fhem.devices.backend.WeatherService
import li.klass.fhem.devices.backend.WeatherService.WeatherForecastInformation
import li.klass.fhem.domain.core.DeviceXMLParsingBase
import org.assertj.core.api.Assertions.assertThat
import org.joda.time.LocalDate
import org.junit.Test

class WeatherDeviceTest : DeviceXMLParsingBase() {
    @Test
    fun testForCorrectlySetAttributes() {
        val device = getDefaultDevice(GenericDevice::class.java)

        assertThat(device.name).isEqualTo(DeviceXMLParsingBase.DEFAULT_TEST_DEVICE_NAME)
        assertThat(device.roomConcatenated).isEqualTo(DeviceXMLParsingBase.DEFAULT_TEST_ROOM_NAME)

        assertThat(stateValueFor(device, "icon")).isEqualTo("cloudy")
        assertThat(stateValueFor(device, "condition")).isEqualTo("Bewölkt")
        assertThat(stateValueFor(device, "humidity")).isEqualTo("43.0 (%)")
        assertThat(stateValueFor(device, "temperature")).isEqualTo("19.0 (°C)")
        assertThat(stateValueFor(device, "wind_condition")).isEqualTo("NW mit 11 km/h")
        assertThat(device.state).isEqualTo("T: 19  H: 43  W: 11")

        val forecasts = WeatherService().forecastsFor(device)
        assertThat(forecasts).containsExactly(
                WeatherForecastInformation(
                        date = LocalDate.parse("2012-05-01"),
                        weekday = "Di.",
                        condition = "Bewölkt",
                        temperature = "19.0 (°C)",
                        icon = String.format(WeatherService.urlTemplate, "cloudy"),
                        humidity = "43.0 (%)"
                ),
                WeatherForecastInformation(
                        date = LocalDate.parse("2012-05-02"),
                        weekday = "Mi.",
                        condition = "Vereinzelt Regen",
                        temperature = "9 (°C) - 20 (°C)",
                        icon = String.format(WeatherService.urlTemplate, "chance_of_rain")
                ),
                WeatherForecastInformation(
                        date = LocalDate.parse("2012-05-03"),
                        weekday = "Do.",
                        condition = "Meist sonnig",
                        temperature = "8 (°C) - 19 (°C)",
                        icon = String.format(WeatherService.urlTemplate, "mostly_sunny")
                ),
                WeatherForecastInformation(
                        date = LocalDate.parse("2012-05-04"),
                        weekday = "Fr.",
                        condition = "Klar",
                        temperature = "10 (°C) - 19 (°C)",
                        icon = String.format(WeatherService.urlTemplate, "sunny")
                )
        )

        assertThat(device.xmlListDevice.setList.entries).isEmpty()
    }

    override fun getFileName(): String = "weather.xml"
}
