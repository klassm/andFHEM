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

package li.klass.fhem.appwidget.view.widget.big;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.widget.RemoteViews;

import li.klass.fhem.AndFHEMApplication;
import li.klass.fhem.R;
import li.klass.fhem.appwidget.WidgetConfiguration;
import li.klass.fhem.appwidget.service.AppWidgetListViewUpdateRemoteViewsService;
import li.klass.fhem.appwidget.view.widget.base.DeviceListAppWidgetView;
import li.klass.fhem.constants.BundleExtraKeys;
import li.klass.fhem.domain.WeatherDevice;
import li.klass.fhem.domain.core.Device;

public class BigWeatherForecastWidget extends DeviceListAppWidgetView {
    @Override
    public int getWidgetName() {
        return R.string.widget_weather_forecast;
    }

    @Override
    protected int getContentView() {
        return R.layout.appwidget_forecast_big;
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @Override
    protected void fillWidgetView(Context context, RemoteViews view, Device<?> device,
                                  WidgetConfiguration widgetConfiguration) {

        Intent listIntent = new Intent(context, AppWidgetListViewUpdateRemoteViewsService.class);
        listIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetConfiguration.widgetId);
        listIntent.putExtra(BundleExtraKeys.APP_WIDGET_ID, widgetConfiguration.widgetId);
        listIntent.putExtra(BundleExtraKeys.APP_WIDGET_TYPE_NAME, widgetConfiguration.widgetType.name());
        listIntent.putExtra(BundleExtraKeys.DEVICE_NAME, device.getName());
        listIntent.setData(Uri.parse(listIntent.toUri(Intent.URI_INTENT_SCHEME)));

        view.setRemoteAdapter(R.id.forecastList, listIntent);

        PendingIntent pendingIntent = createOpenDeviceDetailPagePendingIntent(device, widgetConfiguration.widgetId);
        view.setOnClickPendingIntent(R.id.main, pendingIntent);
        view.setPendingIntentTemplate(R.id.forecastList, pendingIntent);

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        assert appWidgetManager != null;
        appWidgetManager.notifyAppWidgetViewDataChanged(widgetConfiguration.widgetId, R.id.forecastList);
    }

    @Override
    public boolean supports(Device<?> device) {
        if (AndFHEMApplication.getAndroidSDKLevel() < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            return false;
        }
        return device instanceof WeatherDevice;
    }

    @Override
    protected int getListItemCount(Device<?> device) {
        WeatherDevice weatherDevice = (WeatherDevice) device;
        return weatherDevice.getForecasts().size();
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    protected RemoteViews getRemoteViewAt(Context context, Device<?> device, int position, int widgetId) {
        WeatherDevice weatherDevice = (WeatherDevice) device;

        RemoteViews view = new RemoteViews(context.getPackageName(),
                R.layout.appwidget_forecast_big_item);

        WeatherDevice.WeatherDeviceForecast forecast = weatherDevice.getForecasts().get(position);

        view.setTextViewText(R.id.day_description, forecast.getDayOfWeek() + ", " + forecast.getDate());
        view.setTextViewText(R.id.day_condition, forecast.getCondition());
        view.setTextViewText(R.id.day_temperature, forecast.getLowTemperature() + " - " + forecast.getHighTemperature());

        loadImageAndSetIn(view, R.id.day_image, forecast.getUrl(), false);

        view.setOnClickFillInIntent(R.id.forecastItem, new Intent());
        return view;
    }
}
