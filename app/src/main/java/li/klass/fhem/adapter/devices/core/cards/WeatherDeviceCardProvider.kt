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

package li.klass.fhem.adapter.devices.core.cards

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.navigation.NavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.engine.DiskCacheStrategy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import li.klass.fhem.GlideApp
import li.klass.fhem.R
import li.klass.fhem.databinding.DeviceDetailCardWeatherBinding
import li.klass.fhem.databinding.WeatherForecastItemBinding
import li.klass.fhem.devices.backend.weather.WeatherService
import li.klass.fhem.devices.backend.weather.WeatherService.WeatherForecastInformation
import li.klass.fhem.devices.detail.ui.ExpandHandler
import li.klass.fhem.domain.core.FhemDevice
import li.klass.fhem.util.DateFormatUtil
import li.klass.fhem.util.view.setTextOrHide
import javax.inject.Inject

class WeatherDeviceCardProvider @Inject constructor(
        private val weatherService: WeatherService
) : GenericDetailCardProvider {
    override fun ordering(): Int = 1

    override suspend fun provideCard(device: FhemDevice, context: Context, connectionId: String, navController: NavController, expandHandler: ExpandHandler): CardView? {
        val type = device.xmlListDevice.type
        if (type != "Weather" && type != "PROPLANTA") {
            return null
        }
        val binding =
            DeviceDetailCardWeatherBinding.inflate(LayoutInflater.from(context), null, false)
        binding.forecast.layoutManager = object : LinearLayoutManager(context) {
            override fun canScrollVertically() = false
        }
        binding.forecast.adapter = Adapter()

        coroutineScope {
            val forecasts = withContext(Dispatchers.IO) { weatherService.forecastsFor(device) }
            updateListWith(binding.forecast, forecasts)
            binding.root.invalidate()
        }

        return binding.root
    }

    private fun updateListWith(content: RecyclerView,
                               forecasts: List<WeatherForecastInformation>) {
        content.adapter = Adapter(forecasts)
        content.invalidate()
    }

    private class Adapter(
            private val elements: List<WeatherForecastInformation> = emptyList()
    ) : RecyclerView.Adapter<Adapter.WeatherViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WeatherViewHolder =
                WeatherViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.weather_forecast_item, parent, false))

        @SuppressLint("SetTextI18n")
        override fun onBindViewHolder(holder: WeatherViewHolder, position: Int) {
            val itemBinding = WeatherForecastItemBinding.bind(holder.view)
            itemBinding.apply {
                val element = elements[position]
                holder.view.apply {
                    date.text = listOfNotNull(
                        element.weekday?.let { "$it." },
                        DateFormatUtil.ANDFHEM_DATE_FORMAT.print(element.date)
                    ).joinToString(separator = " ")
                    temperature.text = element.temperature

                    condition.setTextOrHide(element.condition, tableRowCondition)
                    windChill.setTextOrHide(element.windChill, tableRowWindChill)
                    humidity.setTextOrHide(element.humidity, tableRowHumidity)
                    wind.setTextOrHide(element.wind, tableRowWind)
                    moonRise.setTextOrHide(element.moonRise, tableRowMoonRise)
                    moonSet.setTextOrHide(element.moonRise, tableRowMoonSet)
                    chanceOfRain.setTextOrHide(element.chanceOfRain, tableRowChanceOfRain)
                    visibilityCondition.setTextOrHide(element.visibility, tableRowVisibilityCondition)

                    GlideApp.with(context)
                            .load(element.icon)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .error(R.drawable.empty)
                            .into(forecastWeatherImage)
                }
            }
        }

        override fun getItemCount(): Int = elements.size

        class WeatherViewHolder(val view: View) : RecyclerView.ViewHolder(view)
    }
}