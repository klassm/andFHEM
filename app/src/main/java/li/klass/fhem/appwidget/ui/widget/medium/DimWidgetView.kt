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
import li.klass.fhem.R
import li.klass.fhem.appwidget.action.AppWidgetActionBroadcastReceiver
import li.klass.fhem.appwidget.ui.widget.WidgetSize
import li.klass.fhem.appwidget.ui.widget.WidgetType
import li.klass.fhem.appwidget.ui.widget.base.DeviceAppWidgetView
import li.klass.fhem.appwidget.update.WidgetConfiguration
import li.klass.fhem.behavior.dim.DimmableBehavior
import li.klass.fhem.constants.Actions
import li.klass.fhem.constants.BundleExtraKeys
import li.klass.fhem.domain.core.FhemDevice
import javax.inject.Inject

class DimWidgetView @Inject constructor() : DeviceAppWidgetView() {
    override fun getWidgetName(): Int = R.string.widget_dim

    override fun getContentView(): Int = R.layout.appwidget_dim

    override fun fillWidgetView(context: Context, view: RemoteViews, device: FhemDevice?, widgetConfiguration: WidgetConfiguration) {
        update(context, device, view, widgetConfiguration.widgetId, widgetConfiguration.connectionId)

        if (device != null) {
            openDeviceDetailPageWhenClicking(R.id.main, view, device, widgetConfiguration, context)
        }
    }

    private fun update(context: Context, device: FhemDevice?, view: RemoteViews, widgetId: Int, connectionId: String?) {
        if (device == null) {
            view.setViewVisibility(R.id.dimUp, View.GONE)
            view.setViewVisibility(R.id.dimDown, View.GONE)
            return
        }
        val behavior = DimmableBehavior.behaviorFor(device, connectionId)!!
        view.setTextViewText(R.id.state, behavior.getDimStateForPosition(behavior.currentDimPosition))

        val dimDownState = behavior.getDimStateForPosition(behavior.currentDimPosition - 1)
        val dimDownIntent = sendTargetDimState(context, device, dimDownState, connectionId)
        view.setOnClickPendingIntent(
            R.id.dimDown, PendingIntent.getService(
                context, (widgetId.toString() + "dimDown").hashCode(), dimDownIntent,
                PendingIntent.FLAG_IMMUTABLE
            )
        )

        val dimUpState = behavior.getDimStateForPosition(behavior.currentDimPosition + 1)
        val dimUpIntent = sendTargetDimState(context, device, dimUpState, connectionId)
        view.setOnClickPendingIntent(
            R.id.dimUp, PendingIntent.getBroadcast(
                context, (widgetId.toString() + "dimUp").hashCode(), dimUpIntent,
                PendingIntent.FLAG_IMMUTABLE
            )
        )
    }

    override fun supports(device: FhemDevice) = DimmableBehavior.supports(device)

    private fun sendTargetDimState(context: Context, device: FhemDevice, targetState: String, connectionId: String?) =
            Intent(context, AppWidgetActionBroadcastReceiver::class.java)
                    .setAction(Actions.DEVICE_WIDGET_TARGET_STATE)
                    .putExtra(BundleExtraKeys.DEVICE_TARGET_STATE, targetState)
                    .putExtra(BundleExtraKeys.DEVICE_NAME, device.name)
                    .putExtra(BundleExtraKeys.CONNECTION_ID, connectionId)

    override val widgetType = WidgetType.DIM
    override val widgetSize = WidgetSize.MEDIUM
}
