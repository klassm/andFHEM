/*
 * AndFHEM - Open Source Android application to control a FHEM home automation
 * server.
 *
 * Copyright (c) 2012, Matthias Klass or third-party contributors as
 * indicated by the @author tags or express copyright attribution
 * statements applied by the authors.  All third-party contributions are
 * distributed under license by Red Hat Inc.
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU GENERAL PUBLICLICENSE, as published by the Free Software Foundation.
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
 */

package li.klass.fhem.adapter.devices;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import li.klass.fhem.R;
import li.klass.fhem.adapter.ListDataAdapter;
import li.klass.fhem.adapter.devices.core.DeviceDetailAvailableAdapter;
import li.klass.fhem.domain.Device;
import li.klass.fhem.domain.WeatherDevice;
import li.klass.fhem.service.graph.description.ChartSeriesDescription;
import li.klass.fhem.util.ListViewUtil;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class WeatherAdapter extends DeviceDetailAvailableAdapter<WeatherDevice> {

    @Override
    protected int getOverviewLayout(WeatherDevice device) {
        return R.layout.room_detail_weather;
    }

    @Override
    protected void fillDeviceOverviewView(final View view, WeatherDevice device) {
        setTextView(view, R.id.deviceName, device.getAliasOrName());
        setTextViewOrHideTableRow(view, R.id.tableRowTemperature, R.id.temperature, device.getTemperature());
        setTextViewOrHideTableRow(view, R.id.tableRowWind, R.id.wind, device.getWind());
        setTextViewOrHideTableRow(view, R.id.tableRowHumidity, R.id.humidity, device.getHumidity());
        setTextViewOrHideTableRow(view, R.id.tableRowCondition, R.id.condition, device.getCondition());

        setWeatherIconIn((ImageView) view.findViewById(R.id.weatherImage), device.getIcon());
    }

    @Override
    public int getDetailViewLayout() {
        return R.layout.device_detail_weather;
    }

    @Override
    protected void fillDeviceDetailView(Context context, View view, WeatherDevice device) {
        setTextView(view, R.id.deviceName, device.getAliasOrName());
        setTextViewOrHideTableRow(view, R.id.tableRowTemperature, R.id.temperature, device.getTemperature());
        setTextViewOrHideTableRow(view, R.id.tableRowWind, R.id.wind, device.getWind());
        setTextViewOrHideTableRow(view, R.id.tableRowHumidity, R.id.humidity, device.getHumidity());
        setTextViewOrHideTableRow(view, R.id.tableRowCondition, R.id.condition, device.getCondition());

        setWeatherIconIn((ImageView) view.findViewById(R.id.currentWeatherImage), device.getIcon());

        createPlotButton(context, view, R.id.temperatureGraph, device.getTemperature(),
                device, R.string.yAxisTemperature, ChartSeriesDescription.getRegressionValuesInstance(WeatherDevice.COLUMN_SPEC_TEMPERATURE));

        createPlotButton(context, view, R.id.humidityGraph, device.getHumidity(),
                device, R.string.yAxisHumidity, WeatherDevice.COLUMN_SPEC_HUMIDITY);

        final ListView weatherForecastList = (ListView) view.findViewById(R.id.weatherForecast);
        ListDataAdapter<WeatherDevice.WeatherDeviceForecast> forecastAdapter = new ListDataAdapter<WeatherDevice.WeatherDeviceForecast>(
                context, R.layout.weather_forecast_item, device.getForecasts()
        ) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                WeatherDevice.WeatherDeviceForecast item = data.get(position);
                View view = inflater.inflate(resource, null);

                String date = item.getDayOfWeek() + ", " + item.getDate();
                setTextViewOrHideTableRow(view, R.id.tableRowDate, R.id.date, date);

                String temperature = item.getLowTemperature() + " - " + item.getHighTemperature();
                setTextViewOrHideTableRow(view, R.id.tableRowTemperature, R.id.temperature,temperature);

                setTextViewOrHideTableRow(view, R.id.tableRowCondition, R.id.condition, item.getCondition());

                setWeatherIconIn((ImageView) view.findViewById(R.id.forecastWeatherImage), item.getIcon());

                return view;
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
        ListViewUtil.setListViewHeightBasedOnChildren(weatherForecastList);
    }

    @Override
    public Class<? extends Device> getSupportedDeviceClass() {
        return WeatherDevice.class;
    }

    private void setWeatherIconIn(final ImageView imageView, String weatherIcon) {
        final String imageURL = WeatherDevice.IMAGE_URL_PREFIX + weatherIcon;
        new AsyncTask<Void, Void, Bitmap>() {

            @Override
            protected Bitmap doInBackground(Void... voids) {
                try {
                    return BitmapFactory.decodeStream((InputStream) new URL(imageURL).getContent());
                } catch (IOException e) {
                    Log.e(WeatherAdapter.class.getName(), "could not load image from " + imageURL, e);
                    return null;
                }
            }

            @Override
            protected void onPostExecute(Bitmap bitmap) {
                imageView.setImageBitmap(bitmap);
            }
        }.execute(null, null);
    }
}
