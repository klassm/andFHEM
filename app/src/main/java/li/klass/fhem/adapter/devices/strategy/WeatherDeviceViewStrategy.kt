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

package li.klass.fhem.adapter.devices.strategy

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import kotlinx.android.synthetic.main.device_overview_weather.view.*
import li.klass.fhem.GlideApp
import li.klass.fhem.R
import li.klass.fhem.adapter.devices.core.GenericDeviceOverviewViewHolder
import li.klass.fhem.adapter.devices.core.deviceItems.XmlDeviceViewItem
import li.klass.fhem.devices.backend.WeatherService
import li.klass.fhem.domain.core.FhemDevice
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WeatherDeviceViewStrategy @Inject constructor(
        private val defaultViewStrategy: DefaultViewStrategy,
        private val weatherService: WeatherService
) : ViewStrategy() {

    @SuppressLint("InflateParams")
    override fun createOverviewView(layoutInflater: LayoutInflater, convertView: View?, rawDevice: FhemDevice, deviceItems: List<XmlDeviceViewItem>, connectionId: String?): View {
        val view = layoutInflater.inflate(R.layout.device_overview_weather, null)
        defaultViewStrategy.fillDeviceOverviewView(view, rawDevice, GenericDeviceOverviewViewHolder(view), deviceItems, layoutInflater)

        val url = weatherService.iconFor(rawDevice)
        setWeatherIconIn(view.weatherImage, url)
        return view
    }

    override fun supports(fhemDevice: FhemDevice): Boolean =
            fhemDevice.xmlListDevice.type == "Weather"


    private fun setWeatherIconIn(imageView: ImageView, url: String?) {
        url ?: return
        GlideApp.with(imageView.context)
                .load(url)
                .error(R.drawable.empty)
                .into(imageView)
    }
}
