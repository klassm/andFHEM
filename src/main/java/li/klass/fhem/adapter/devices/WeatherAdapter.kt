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

package li.klass.fhem.adapter.devices

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.RelativeLayout
import li.klass.fhem.GlideApp
import li.klass.fhem.R
import li.klass.fhem.adapter.ListDataAdapter
import li.klass.fhem.adapter.devices.core.ExplicitOverviewDetailDeviceAdapter
import li.klass.fhem.adapter.devices.strategy.ViewStrategy
import li.klass.fhem.adapter.devices.strategy.WeatherDeviceViewStrategy
import li.klass.fhem.dagger.ApplicationComponent
import li.klass.fhem.domain.WeatherDevice
import li.klass.fhem.domain.core.FhemDevice
import li.klass.fhem.util.ListViewUtil
import javax.inject.Inject

class WeatherAdapter : ExplicitOverviewDetailDeviceAdapter() {

    @Inject
    lateinit var weatherDeviceOverviewStrategy: WeatherDeviceViewStrategy

    override fun getSupportedDeviceClass(): Class<out FhemDevice> = WeatherDevice::class.java

    override fun inject(daggerComponent: ApplicationComponent) {
        daggerComponent.inject(this)
    }

    private fun setWeatherIconIn(imageView: ImageView, weatherIcon: String?) {
        val imageURL = WeatherDevice.IMAGE_URL_PREFIX + weatherIcon + ".png"

        GlideApp.with(imageView.context)
                .load(imageURL)
                .error(R.drawable.empty)
                .into(imageView)
    }

    override fun fillOtherStuffDetailLayout(context: Context, layout: LinearLayout, device: FhemDevice, inflater: LayoutInflater) {
        val currentWeatherHolder = inflater.inflate(R.layout.device_detail_other_layout, layout, false) as LinearLayout
        setTextView(currentWeatherHolder, R.id.caption, R.string.currentWeather)
        val currentWeatherContent = createCurrentWeatherContent(device, inflater, layout)
        currentWeatherHolder.addView(currentWeatherContent)
        layout.addView(currentWeatherHolder)

        val forecastHolder = inflater.inflate(R.layout.device_detail_other_layout, layout, false) as LinearLayout
        setTextView(forecastHolder, R.id.caption, R.string.forecast)
        layout.addView(forecastHolder)

        val weatherForecastList = createWeatherForecastList(context, device)
        forecastHolder.addView(weatherForecastList)
        ListViewUtil.setHeightBasedOnChildren(weatherForecastList)
    }

    private fun createCurrentWeatherContent(device: FhemDevice, inflater: LayoutInflater, layout: LinearLayout): RelativeLayout {
        val currentWeather = inflater.inflate(R.layout.weather_current, layout, false) as RelativeLayout

        val weatherDevice = device as WeatherDevice
        setTextViewOrHideTableRow(currentWeather, R.id.tableRowTemperature, R.id.temperature, weatherDevice.temperature)
        setTextViewOrHideTableRow(currentWeather, R.id.tableRowWind, R.id.wind, weatherDevice.wind)
        setTextViewOrHideTableRow(currentWeather, R.id.tableRowHumidity, R.id.humidity, weatherDevice.humidity)
        setTextViewOrHideTableRow(currentWeather, R.id.tableRowCondition, R.id.condition, weatherDevice.condition)
        setTextViewOrHideTableRow(currentWeather, R.id.tableRowWindChill, R.id.windChill, weatherDevice.windChill)
        setTextViewOrHideTableRow(currentWeather, R.id.tableRowVisibilityCondition, R.id.visibilityCondition, weatherDevice.visibilityConditions)

        setWeatherIconIn(currentWeather.findViewById<View>(R.id.currentWeatherImage) as ImageView, weatherDevice.icon)

        return currentWeather
    }

    private fun createWeatherForecastList(context: Context, device: FhemDevice): ListView {
        val weatherForecastList = ListView(context)
        val forecastAdapter = object : ListDataAdapter<WeatherDevice.WeatherDeviceForecast>(
                context, R.layout.weather_forecast_item, (device as WeatherDevice).forecasts
        ) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val myView = convertView ?: inflater.inflate(resource, null)
                val item = data[position]

                val date = item.dayOfWeek + ", " + item.date
                setTextViewOrHideTableRow(myView, R.id.tableRowDate, R.id.date, date)

                val temperature = item.lowTemperature + " - " + item.highTemperature
                setTextViewOrHideTableRow(myView, R.id.tableRowTemperature, R.id.temperature, temperature)

                setTextViewOrHideTableRow(myView, R.id.tableRowCondition, R.id.condition, item.condition)

                setWeatherIconIn(myView!!.findViewById<View>(R.id.forecastWeatherImage) as ImageView, item.icon)

                return myView
            }

            override fun areAllItemsEnabled(): Boolean = false

            override fun isEnabled(position: Int): Boolean = false
        }
        weatherForecastList.adapter = forecastAdapter
        return weatherForecastList
    }

    override fun getOverviewViewHolderClass(): Class<*>? = null

    override fun fillOverviewStrategies(overviewStrategies: MutableList<ViewStrategy>) {
        super.fillOverviewStrategies(overviewStrategies)
        overviewStrategies.add(weatherDeviceOverviewStrategy)
    }
}
