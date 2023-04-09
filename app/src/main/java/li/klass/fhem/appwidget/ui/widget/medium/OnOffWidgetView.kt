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
import android.view.View
import android.widget.RemoteViews
import androidx.core.content.ContextCompat
import li.klass.fhem.R
import li.klass.fhem.adapter.devices.hook.DeviceHookProvider
import li.klass.fhem.appwidget.action.AppWidgetActionBroadcastReceiver
import li.klass.fhem.appwidget.ui.widget.WidgetSize
import li.klass.fhem.appwidget.ui.widget.WidgetType
import li.klass.fhem.appwidget.ui.widget.base.DeviceAppWidgetView
import li.klass.fhem.appwidget.update.WidgetConfiguration
import li.klass.fhem.behavior.toggle.OnOffBehavior
import li.klass.fhem.constants.Actions
import li.klass.fhem.constants.BundleExtraKeys
import li.klass.fhem.domain.core.FhemDevice
import javax.inject.Inject

class OnOffWidgetView @Inject constructor(
    val onOffBehavior: OnOffBehavior,
    val deviceHookProvider: DeviceHookProvider
) : DeviceAppWidgetView() {

    override fun getWidgetName(): Int = R.string.widget_onOff

    override fun getContentView(): Int = R.layout.appwidget_on_off

    override fun fillWidgetView(context: Context, view: RemoteViews, device: FhemDevice?, widgetConfiguration: WidgetConfiguration) {
        if (device == null) {
            view.setViewVisibility(R.id.widgetOnButton, View.GONE)
            view.setViewVisibility(R.id.widgetOffButton, View.GONE)
            return
        }
        val isOn = onOffBehavior.isOn(device)

        val onStateName = onOffBehavior.getOnStateName(device)!!
        val offStateName = onOffBehavior.getOffStateName(device)!!

        view.setTextViewText(R.id.widgetOnButton, device.getEventMapStateFor(onStateName))
        view.setTextViewText(R.id.widgetOffButton, device.getEventMapStateFor(offStateName))

        val backgroundColor = if (isOn) R.color.android_green else android.R.color.white
        view.setInt(R.id.widgetOnButton, "setBackgroundColor", ContextCompat.getColor(context, backgroundColor))

        val onPendingIntent = targetStatePendingIntent(context, device, onStateName, widgetConfiguration, widgetConfiguration.widgetId)
        view.setOnClickPendingIntent(R.id.widgetOnButton, onPendingIntent)

        val offPendingIntent = targetStatePendingIntent(context, device, offStateName, widgetConfiguration, -widgetConfiguration.widgetId)
        view.setOnClickPendingIntent(R.id.widgetOffButton, offPendingIntent)

        openDeviceDetailPageWhenClicking(R.id.deviceName, view, device, widgetConfiguration, context)
    }

    private fun targetStatePendingIntent(context: Context, device: FhemDevice, targetState: String, widgetConfiguration: WidgetConfiguration, requestCode: Int) =
            PendingIntent.getBroadcast(
                context, requestCode,
                Intent(context, AppWidgetActionBroadcastReceiver::class.java)
                    .setAction(Actions.DEVICE_WIDGET_TARGET_STATE)
                    .putExtra(BundleExtraKeys.DEVICE_NAME, device.name)
                    .putExtra(BundleExtraKeys.DEVICE_TARGET_STATE, targetState)
                    .putExtra(BundleExtraKeys.CONNECTION_ID, widgetConfiguration.connectionId),
                PendingIntent.FLAG_IMMUTABLE
            )

    override fun supports(device: FhemDevice): Boolean =
            onOffBehavior.supports(device)

    override val widgetSize = WidgetSize.MEDIUM

    override val widgetType = WidgetType.ON_OFF
}
