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

package li.klass.fhem.appwidget.view.widget.medium;

import android.content.Context;
import android.widget.RemoteViews;

import java.util.List;

import li.klass.fhem.R;
import li.klass.fhem.appwidget.WidgetConfiguration;
import li.klass.fhem.appwidget.view.widget.base.DeviceAppWidgetView;
import li.klass.fhem.domain.WeatherDevice;
import li.klass.fhem.domain.core.Device;

public class MediumWeatherForecastWidget extends DeviceAppWidgetView {
    @Override
    public int getWidgetName() {
        return R.string.widget_weather_forecast;
    }

    @Override
    protected int getContentView() {
        return R.layout.appwidget_weather;
    }

    @Override
    protected void fillWidgetView(Context context, RemoteViews view, Device<?> device, WidgetConfiguration widgetConfiguration) {
        WeatherDevice weatherDevice = (WeatherDevice) device;
        List<WeatherDevice.WeatherDeviceForecast> forecasts = weatherDevice.getForecasts();
        WeatherDevice.WeatherDeviceForecast forecast = forecasts.get(0);

        view.setTextViewText(R.id.day_description, forecast.getDayOfWeek() + ", " + forecast.getDate());
        view.setTextViewText(R.id.day_condition, forecast.getCondition());
        view.setTextViewText(R.id.day_temperature, forecast.getLowTemperature() + " - " + forecast.getHighTemperature());

        loadImageAndSetIn(view, R.id.day_image, forecast.getUrl(), false);

        openDeviceDetailPageWhenClicking(R.id.main, view, device, widgetConfiguration);
    }

    @Override
    public boolean supports(Device<?> device) {
        return device instanceof WeatherDevice;
    }
}
