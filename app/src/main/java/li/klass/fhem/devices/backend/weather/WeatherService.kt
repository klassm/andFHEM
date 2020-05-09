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

package li.klass.fhem.devices.backend.weather

import li.klass.fhem.domain.core.FhemDevice
import li.klass.fhem.update.backend.xmllist.DeviceNode
import li.klass.fhem.util.ValueDescriptionUtil.appendTemperature
import org.joda.time.LocalDate
import javax.inject.Inject

class WeatherService @Inject constructor() {

    fun iconFor(device: FhemDevice): String? =
            when (device.xmlListDevice.type) {
                "WEATHER" -> device.xmlListDevice.stateValueFor("icon")
                        ?.let { iconForName(it) }
                else -> null
            }



    fun forecastsFor(device: FhemDevice): List<WeatherForecastInformation> {
        val measureDate = findMeasureDate(device.xmlListDevice.states)
        return listOfNotNull(currentWeatherAsForecast(device, measureDate)) +
                forecastForFutureDays(device, measureDate)
    }

    private fun forecastForFutureDays(device: FhemDevice, today: LocalDate): List<WeatherForecastInformation> {
        return device.xmlListDevice.states.values
                .asSequence()
                .map { toForecastLineEntry(it) }
                .filterNotNull()
                .groupBy { it.number }
                .mapNotNull { toForecastDayInformation(device, it.value, today, it.key - 1) }
                .sortedBy { it.date }
                .drop(1) // this is today
                .toList()
    }

    private fun currentWeatherAsForecast(device: FhemDevice, today: LocalDate): WeatherForecastInformation? {
        val type = device.xmlListDevice.type
        return device.xmlListDevice.states.let {
            when (type) {
                "WEATHER" -> {
                    WeatherForecastInformation(
                            date = today,
                            weekday = it.getValue("day_of_week").value,
                            condition = it.getValue("condition").value,
                            temperature = it.getValue("temperature").value,
                            icon = iconForName(it.getValue("icon").value),
                            windChill = it["wind_chill"]?.value,
                            humidity = it["humidity"]?.value,
                            visibility = it["visibility"]?.value
                    )
                }
                "PROPLANTA" -> {
                    WeatherForecastInformation(
                            date = today,
                            condition = null,
                            temperature = it.getValue("temperature").value,
                            humidity = it["humidity"]?.value,
                            wind = it["wind"]?.value
                    )
                }
                else -> null
            }
        }
    }

    private fun findMeasureDate(it: Map<String, DeviceNode>): LocalDate =
            (it["current_date_time"] ?: it["temperature"])?.measured?.toLocalDate()!!

    private fun toForecastDayInformation(device: FhemDevice, values: List<ForecastLineEntry>, today: LocalDate, index: Int): WeatherForecastInformation? {
        val type = device.xmlListDevice.type
        val day = today.plusDays(index)
        return when (type) {
            "WEATHER" -> {
                val weekday = values.first { it.key == "day_of_week" }.value
                val tempLow = appendTemperature(values.first { it.key == "low_c" }.value)
                val tempHigh = appendTemperature(values.first { it.key == "high_c" }.value)
                val condition = values.first { it.key == "condition" }.value
                val icon = values.first { it.key == "icon" }.value

                WeatherForecastInformation(
                        date = day,
                        weekday = weekday,
                        condition = condition,
                        temperature = "$tempLow - $tempHigh",
                        icon = iconForName(icon)
                )
            }
            "PROPLANTA" -> {
                val tempLow = appendTemperature(values.first { it.key == "tempMin" }.value)
                val tempHigh = appendTemperature(values.first { it.key == "tempMax" }.value)
                val icon = values.firstOrNull { it.key == "weatherIcon" }?.value
                val moonRise = values.first { it.key == "moonRise" }.value
                val moonSet = values.first { it.key == "moonSet" }.value

                WeatherForecastInformation(
                        date = day,
                        weekday = null,
                        condition = null,
                        temperature = "$tempLow - $tempHigh",
                        moonRise = moonRise,
                        moonSet = moonSet,
                        icon = icon
                )
            }
            else -> null
        }
    }

    private fun toForecastLineEntry(node: DeviceNode): ForecastLineEntry? =
            forecastRegexp.matchEntire(node.key)
                    ?.let {
                        val number = Integer.parseInt(it.groupValues[1])
                        val key = it.groupValues[2]
                        val value = node.value

                        ForecastLineEntry(number, key, value)
                    }

    private fun iconForName(name: String) = String.format(urlTemplate, name)

    private data class ForecastLineEntry(val number: Int, val key: String, val value: String)

    data class WeatherForecastInformation(val date: LocalDate,
                                          val weekday: String? = null,
                                          val condition: String? = null,
                                          val temperature: String,
                                          val icon: String? = null,
                                          val windChill: String? = null,
                                          val humidity: String? = null,
                                          val visibility: String? = null,
                                          val moonRise: String? = null,
                                          val moonSet: String? = null,
                                          val wind: String? = null
    )

    companion object {
        val forecastRegexp = Regex("fc([0-9]+)_(.*)")
        const val urlTemplate = "https://github.com/klassm/andFHEM/raw/gh-pages/images/weatherIcons/%s.png"
    }
}