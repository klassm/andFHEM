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

package li.klass.fhem.adapter.devices.overview.strategy;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import li.klass.fhem.R;
import li.klass.fhem.adapter.devices.core.GenericDeviceOverviewViewHolder;
import li.klass.fhem.adapter.devices.core.deviceItems.DeviceViewItem;
import li.klass.fhem.domain.WeatherDevice;
import li.klass.fhem.domain.core.FhemDevice;
import li.klass.fhem.util.ImageUtil;

@Singleton
public class WeatherDeviceOverviewStrategy extends OverviewStrategy {
    @Inject
    public WeatherDeviceOverviewStrategy() {
    }

    @Override
    public View createOverviewView(LayoutInflater layoutInflater, View convertView, FhemDevice rawDevice, long lastUpdate, List<DeviceViewItem> deviceItems) {
        RelativeLayout layout = (RelativeLayout) layoutInflater.inflate(R.layout.device_overview_weather, null);
        fillDeviceOverviewView(layout, (WeatherDevice) rawDevice, lastUpdate, null);
        return layout;
    }


    protected void fillDeviceOverviewView(final View view, WeatherDevice device, long lastUpdate, GenericDeviceOverviewViewHolder viewHolder) {
        setTextView(view, R.id.deviceName, device.getAliasOrName());
        setTextViewOrHideTableRow(view, R.id.tableRowTemperature, R.id.temperature, device.getTemperature());
        setTextViewOrHideTableRow(view, R.id.tableRowWind, R.id.wind, device.getWind());
        setTextViewOrHideTableRow(view, R.id.tableRowHumidity, R.id.humidity, device.getHumidity());
        setTextViewOrHideTableRow(view, R.id.tableRowCondition, R.id.condition, device.getCondition());

        setWeatherIconIn((ImageView) view.findViewById(R.id.weatherImage), device.getIcon());
    }

    private void setWeatherIconIn(final ImageView imageView, String weatherIcon) {
        final String imageURL = WeatherDevice.IMAGE_URL_PREFIX + weatherIcon + ".png";
        ImageUtil.setExternalImageIn(imageView, imageURL);
    }

}
