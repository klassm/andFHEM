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

package li.klass.fhem.appwidget.ui.widget.medium

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.support.v4.content.ContextCompat
import android.widget.RemoteViews
import li.klass.fhem.R
import li.klass.fhem.adapter.devices.hook.DeviceHookProvider
import li.klass.fhem.adapter.devices.toggle.OnOffBehavior
import li.klass.fhem.appwidget.ui.widget.base.DeviceAppWidgetView
import li.klass.fhem.appwidget.update.WidgetConfiguration
import li.klass.fhem.constants.Actions
import li.klass.fhem.constants.BundleExtraKeys
import li.klass.fhem.dagger.ApplicationComponent
import li.klass.fhem.domain.core.FhemDevice
import li.klass.fhem.domain.core.ToggleableDevice
import li.klass.fhem.service.intent.DeviceIntentService
import javax.inject.Inject

class OnOffWidgetView : DeviceAppWidgetView() {
    @Inject
    lateinit var onOffBehavior: OnOffBehavior

    @Inject
    lateinit var deviceHookProvider: DeviceHookProvider

    override fun getWidgetName(): Int = R.string.widget_onOff

    override fun getContentView(): Int = R.layout.appwidget_on_off

    override fun fillWidgetView(context: Context, view: RemoteViews, device: FhemDevice, widgetConfiguration: WidgetConfiguration) {
        val isOn = onOffBehavior.isOn(device)

        val onStateName = deviceHookProvider.getOnStateName(device)
        val offStateName = deviceHookProvider.getOffStateName(device)

        view.setTextViewText(R.id.widgetOnButton, device.getEventMapStateFor(onStateName))
        view.setTextViewText(R.id.widgetOffButton, device.getEventMapStateFor(offStateName))

        val backgroundColor = if (isOn) R.color.android_green else android.R.color.white
        view.setInt(R.id.widgetOnButton, "setBackgroundColor", ContextCompat.getColor(context, backgroundColor))

        val onIntent = Intent(Actions.DEVICE_SET_STATE)
                .setClass(context, DeviceIntentService::class.java)
                .putExtra(BundleExtraKeys.DEVICE_NAME, device.name)
                .putExtra(BundleExtraKeys.DEVICE_TARGET_STATE, onStateName)
                .putExtra(BundleExtraKeys.CONNECTION_ID, widgetConfiguration.connectionId)
        val onPendingIntent = PendingIntent.getService(context, widgetConfiguration.widgetId,
                onIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        view.setOnClickPendingIntent(R.id.widgetOnButton, onPendingIntent)

        val offIntent = Intent(Actions.DEVICE_SET_STATE)
                .setClass(context, DeviceIntentService::class.java)
                .putExtra(BundleExtraKeys.DEVICE_NAME, device.name)
                .putExtra(BundleExtraKeys.DEVICE_TARGET_STATE, offStateName)
                .putExtra(BundleExtraKeys.CONNECTION_ID, widgetConfiguration.connectionId)
        val offPendingIntent = PendingIntent.getService(context, -1 * widgetConfiguration.widgetId,
                offIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        view.setOnClickPendingIntent(R.id.widgetOffButton, offPendingIntent)

        openDeviceDetailPageWhenClicking(R.id.deviceName, view, device, widgetConfiguration, context)
    }

    override fun supports(device: FhemDevice, context: Context): Boolean =
            device is ToggleableDevice

    override fun inject(applicationComponent: ApplicationComponent) {
        applicationComponent.inject(this)
    }
}
