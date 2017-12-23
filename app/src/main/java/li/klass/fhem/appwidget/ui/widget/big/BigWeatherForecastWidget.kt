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

package li.klass.fhem.appwidget.ui.widget.big

import android.annotation.TargetApi
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.RemoteViews
import com.bumptech.glide.Glide
import li.klass.fhem.AndFHEMApplication
import li.klass.fhem.R
import li.klass.fhem.appwidget.ui.widget.base.DeviceListAppWidgetView
import li.klass.fhem.appwidget.update.AppWidgetListViewUpdateRemoteViewsService
import li.klass.fhem.appwidget.update.WidgetConfiguration
import li.klass.fhem.constants.BundleExtraKeys
import li.klass.fhem.dagger.ApplicationComponent
import li.klass.fhem.devices.backend.WeatherService
import li.klass.fhem.domain.core.FhemDevice
import li.klass.fhem.util.DateFormatUtil
import javax.inject.Inject

class BigWeatherForecastWidget : DeviceListAppWidgetView<WeatherService.WeatherForecastInformation>() {
    @Inject
    lateinit var weatherService: WeatherService

    override fun getWidgetName(): Int = R.string.widget_weather_forecast

    override fun getContentView(): Int = R.layout.appwidget_forecast_big

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    override fun fillWidgetView(context: Context, view: RemoteViews, device: FhemDevice,
                                widgetConfiguration: WidgetConfiguration) {

        val listIntent = Intent(context, AppWidgetListViewUpdateRemoteViewsService::class.java)
                .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetConfiguration.widgetId)
                .putExtra(BundleExtraKeys.APP_WIDGET_ID, widgetConfiguration.widgetId)
                .putExtra(BundleExtraKeys.APP_WIDGET_TYPE_NAME, widgetConfiguration.widgetType.name)
                .putExtra(BundleExtraKeys.DEVICE_NAME, device.name)
                .putExtra(BundleExtraKeys.CONNECTION_ID, widgetConfiguration.connectionId)
        listIntent.data = Uri.parse(listIntent.toUri(Intent.URI_INTENT_SCHEME))

        view.setRemoteAdapter(R.id.forecastList, listIntent)

        val pendingIntent = createOpenDeviceDetailPagePendingIntent(device, widgetConfiguration, context)
        view.setOnClickPendingIntent(R.id.main, pendingIntent)
        view.setPendingIntentTemplate(R.id.forecastList, pendingIntent)

        val appWidgetManager = AppWidgetManager.getInstance(context)!!
        appWidgetManager.notifyAppWidgetViewDataChanged(widgetConfiguration.widgetId, R.id.forecastList)
    }

    override fun supports(device: FhemDevice, context: Context): Boolean {
        return AndFHEMApplication.androidSDKLevel >= Build.VERSION_CODES.ICE_CREAM_SANDWICH
                && device.xmlListDevice.type == "Weather"
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    override fun getRemoteViewAt(context: Context, item: WeatherService.WeatherForecastInformation, widgetId: Int): RemoteViews {
        val view = RemoteViews(context.packageName,
                R.layout.appwidget_forecast_big_item)

        view.setTextViewText(R.id.day_description, item.weekday + ", " + DateFormatUtil.ANDFHEM_DATE_FORMAT.print(item.date))
        view.setTextViewText(R.id.day_condition, item.condition)
        view.setTextViewText(R.id.day_temperature, item.temperature)
        val bitmap = Glide.with(context)
                .asBitmap()
                .load(item.icon)
                .submit().get()
        view.setImageViewBitmap(R.id.day_image, bitmap)
        view.setOnClickFillInIntent(R.id.forecastItem, Intent())
        return view
    }

    override fun extractItemsFrom(device: FhemDevice): List<WeatherService.WeatherForecastInformation> =
            weatherService.forecastsFor(device)

    override fun inject(applicationComponent: ApplicationComponent) {
        applicationComponent.inject(this)
    }
}
