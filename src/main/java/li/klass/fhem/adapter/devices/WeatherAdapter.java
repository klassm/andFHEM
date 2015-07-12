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

package li.klass.fhem.adapter.devices;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;

import javax.inject.Inject;

import li.klass.fhem.R;
import li.klass.fhem.adapter.ListDataAdapter;
import li.klass.fhem.adapter.devices.core.ExplicitOverviewDetailDeviceAdapter;
import li.klass.fhem.adapter.devices.overview.strategy.OverviewStrategy;
import li.klass.fhem.adapter.devices.overview.strategy.WeatherDeviceOverviewStrategy;
import li.klass.fhem.dagger.ApplicationComponent;
import li.klass.fhem.domain.WeatherDevice;
import li.klass.fhem.domain.core.FhemDevice;
import li.klass.fhem.util.ImageUtil;
import li.klass.fhem.util.ListViewUtil;

public class WeatherAdapter extends ExplicitOverviewDetailDeviceAdapter {

    @Inject
    WeatherDeviceOverviewStrategy weatherDeviceOverviewStrategy;

    @Override
    public Class<? extends FhemDevice> getSupportedDeviceClass() {
        return WeatherDevice.class;
    }

    @Override
    protected void inject(ApplicationComponent daggerComponent) {
        daggerComponent.inject(this);
    }

    private void setWeatherIconIn(final ImageView imageView, String weatherIcon) {
        final String imageURL = WeatherDevice.IMAGE_URL_PREFIX + weatherIcon + ".png";
        ImageUtil.setExternalImageIn(imageView, imageURL);
    }

    @Override
    protected void fillOtherStuffDetailLayout(Context context, LinearLayout layout, FhemDevice device, LayoutInflater inflater) {
        LinearLayout currentWeatherHolder = (LinearLayout) inflater.inflate(R.layout.device_detail_other_layout, layout, false);
        setTextView(currentWeatherHolder, R.id.caption, R.string.currentWeather);
        RelativeLayout currentWeatherContent = createCurrentWeatherContent(device, inflater, layout);
        currentWeatherHolder.addView(currentWeatherContent);
        layout.addView(currentWeatherHolder);

        LinearLayout forecastHolder = (LinearLayout) inflater.inflate(R.layout.device_detail_other_layout, layout, false);
        setTextView(forecastHolder, R.id.caption, R.string.forecast);
        layout.addView(forecastHolder);

        ListView weatherForecastList = createWeatherForecastList(context, device);
        forecastHolder.addView(weatherForecastList);
        ListViewUtil.setHeightBasedOnChildren(weatherForecastList);
    }

    private RelativeLayout createCurrentWeatherContent(FhemDevice device, LayoutInflater inflater, LinearLayout layout) {
        RelativeLayout currentWeather = (RelativeLayout) inflater.inflate(R.layout.weather_current, layout, false);

        WeatherDevice weatherDevice = (WeatherDevice) device;
        setTextViewOrHideTableRow(currentWeather, R.id.tableRowTemperature, R.id.temperature, weatherDevice.getTemperature());
        setTextViewOrHideTableRow(currentWeather, R.id.tableRowWind, R.id.wind, weatherDevice.getWind());
        setTextViewOrHideTableRow(currentWeather, R.id.tableRowHumidity, R.id.humidity, weatherDevice.getHumidity());
        setTextViewOrHideTableRow(currentWeather, R.id.tableRowCondition, R.id.condition, weatherDevice.getCondition());
        setTextViewOrHideTableRow(currentWeather, R.id.tableRowWindChill, R.id.windChill, weatherDevice.getWindChill());
        setTextViewOrHideTableRow(currentWeather, R.id.tableRowVisibilityCondition, R.id.visibilityCondition, weatherDevice.getVisibilityConditions());

        setWeatherIconIn((ImageView) currentWeather.findViewById(R.id.currentWeatherImage), weatherDevice.getIcon());

        return currentWeather;
    }

    private ListView createWeatherForecastList(final Context context, final FhemDevice device) {
        final ListView weatherForecastList = new ListView(context);
        ListDataAdapter<WeatherDevice.WeatherDeviceForecast> forecastAdapter = new ListDataAdapter<WeatherDevice.WeatherDeviceForecast>(
                context, R.layout.weather_forecast_item, ((WeatherDevice) device).getForecasts()
        ) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                WeatherDevice.WeatherDeviceForecast item = data.get(position);
                if (convertView == null) {
                    convertView = inflater.inflate(resource, null);
                }

                String date = item.getDayOfWeek() + ", " + item.getDate();
                setTextViewOrHideTableRow(convertView, R.id.tableRowDate, R.id.date, date);

                String temperature = item.getLowTemperature() + " - " + item.getHighTemperature();
                setTextViewOrHideTableRow(convertView, R.id.tableRowTemperature, R.id.temperature, temperature);

                setTextViewOrHideTableRow(convertView, R.id.tableRowCondition, R.id.condition, item.getCondition());

                setWeatherIconIn((ImageView) convertView.findViewById(R.id.forecastWeatherImage), item.getIcon());

                return convertView;
            }

            @Override
            public boolean areAllItemsEnabled() {
                return false;
            }

            @Override
            public boolean isEnabled(int position) {
                return false;
            }
        };
        weatherForecastList.setAdapter(forecastAdapter);
        return weatherForecastList;
    }

    @Override
    public Class getOverviewViewHolderClass() {
        return null;
    }

    @Override
    public OverviewStrategy getOverviewStrategy() {
        return weatherDeviceOverviewStrategy;
    }
}
