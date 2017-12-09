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
import android.os.Bundle
import android.os.Handler
import android.os.ResultReceiver
import android.widget.RemoteViews
import li.klass.fhem.R
import li.klass.fhem.appwidget.ui.widget.base.DeviceAppWidgetView
import li.klass.fhem.appwidget.update.AppWidgetUpdateIntentService
import li.klass.fhem.appwidget.update.WidgetConfiguration
import li.klass.fhem.constants.Actions
import li.klass.fhem.constants.BundleExtraKeys
import li.klass.fhem.constants.ResultCodes
import li.klass.fhem.dagger.ApplicationComponent
import li.klass.fhem.domain.core.DimmableDevice
import li.klass.fhem.domain.core.FhemDevice
import li.klass.fhem.service.intent.DeviceIntentService

class DimWidgetView : DeviceAppWidgetView() {
    override fun getWidgetName(): Int = R.string.widget_dim

    override fun getContentView(): Int = R.layout.appwidget_dim

    override fun fillWidgetView(context: Context, view: RemoteViews, device: FhemDevice, widgetConfiguration: WidgetConfiguration) {
        val dimmableDevice = device as DimmableDevice<*>

        val resultReceiver = object : ResultReceiver(Handler()) {
            override fun onReceiveResult(resultCode: Int, resultData: Bundle) {
                if (resultCode == ResultCodes.SUCCESS) {
                    val intent = Intent(Actions.REDRAW_WIDGET)
                    intent.setClass(context, AppWidgetUpdateIntentService::class.java)
                    intent.putExtra(BundleExtraKeys.APP_WIDGET_ID, widgetConfiguration.widgetId)
                    context.startService(intent)
                }
            }
        }
        update(context, dimmableDevice, view, widgetConfiguration.widgetId, widgetConfiguration.connectionId, resultReceiver)

        openDeviceDetailPageWhenClicking(R.id.main, view, device, widgetConfiguration, context)
    }

    private fun update(context: Context, device: DimmableDevice<*>, view: RemoteViews, widgetId: Int, connectionId: String?, resultReceiver: ResultReceiver) {
        view.setTextViewText(R.id.state, device.getDimStateNameForDimStateValue(device.dimPosition))

        val dimDownIntent = sendTargetDimState(context, device, "dimdown", connectionId, resultReceiver)
        view.setOnClickPendingIntent(R.id.dimDown, PendingIntent.getService(context, (widgetId.toString() + "dimDown").hashCode(), dimDownIntent,
                PendingIntent.FLAG_UPDATE_CURRENT))

        val dimUpIntent = sendTargetDimState(context, device, "dimup", connectionId, resultReceiver)
        view.setOnClickPendingIntent(R.id.dimUp, PendingIntent.getService(context, (widgetId.toString() + "dimUp").hashCode(), dimUpIntent,
                PendingIntent.FLAG_UPDATE_CURRENT))
    }

    override fun supports(device: FhemDevice, context: Context): Boolean =
            device is DimmableDevice<*> && device.supportsDim()

    private fun sendTargetDimState(context: Context, device: DimmableDevice<*>, targetState: String, connectionId: String?, resultReceiver: ResultReceiver): Intent {

        return Intent(Actions.DEVICE_SET_STATE)
                .setClass(context, DeviceIntentService::class.java)
                .putExtra(BundleExtraKeys.DEVICE_TARGET_STATE, targetState)
                .putExtra(BundleExtraKeys.DEVICE_NAME, device.name)
                .putExtra(BundleExtraKeys.RESULT_RECEIVER, resultReceiver)
                .putExtra(BundleExtraKeys.CONNECTION_ID, connectionId)
    }

    override fun inject(applicationComponent: ApplicationComponent) {
        applicationComponent.inject(this)
    }
}
