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
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews
import li.klass.fhem.R
import li.klass.fhem.adapter.devices.genericui.AvailableTargetStatesDialogUtil
import li.klass.fhem.adapter.devices.genericui.availableTargetStates.OnTargetStateSelectedCallback
import li.klass.fhem.appwidget.action.AppWidgetActionBroadcastReceiver
import li.klass.fhem.appwidget.ui.widget.WidgetConfigurationCreatedCallback
import li.klass.fhem.appwidget.ui.widget.WidgetSize
import li.klass.fhem.appwidget.ui.widget.WidgetType
import li.klass.fhem.appwidget.ui.widget.base.DeviceAppWidgetView
import li.klass.fhem.appwidget.update.WidgetConfiguration
import li.klass.fhem.constants.Actions
import li.klass.fhem.constants.BundleExtraKeys
import li.klass.fhem.domain.core.FhemDevice
import javax.inject.Inject

class TargetStateWidgetView @Inject constructor() : DeviceAppWidgetView() {
    override fun getWidgetName(): Int = R.string.widget_targetstate

    override fun getContentView(): Int = R.layout.appwidget_targetstate

    override fun fillWidgetView(context: Context, view: RemoteViews, device: FhemDevice?, widgetConfiguration: WidgetConfiguration) {
        if (device == null) {
            view.setViewVisibility(R.id.button, View.GONE)
            return
        }

        val payload = widgetConfiguration.payload[1]
        val state = device.getEventMapStateFor(payload)

        view.setTextViewText(R.id.button, state)

        val actionIntent = Intent(context, AppWidgetActionBroadcastReceiver::class.java)
                .setAction(Actions.DEVICE_WIDGET_TARGET_STATE)
                .putExtra(BundleExtraKeys.DEVICE_NAME, device.name)
                .putExtra(BundleExtraKeys.DEVICE_TARGET_STATE, payload)
                .putExtra(BundleExtraKeys.CONNECTION_ID, widgetConfiguration.connectionId)

        view.apply {
            setOnClickPendingIntent(
                    R.id.button,
                    PendingIntent.getBroadcast(
                        context, widgetConfiguration.widgetId, actionIntent,
                        FLAG_IMMUTABLE
                    )
            )
        }

        openDeviceDetailPageWhenClicking(R.id.main, view, device, widgetConfiguration, context)
    }


    override fun createDeviceWidgetConfiguration(context: Context,
                                                 appWidgetId: Int, connectionId: String, device: FhemDevice,
                                                 callback: WidgetConfigurationCreatedCallback) {

        AvailableTargetStatesDialogUtil.showSwitchOptionsMenu(context, device, widgetCreatingCallback(widgetType, appWidgetId, connectionId, callback))
    }

    private fun widgetCreatingCallback(widgetType: WidgetType, appWidgetId: Int, connectionId: String, callback: WidgetConfigurationCreatedCallback): OnTargetStateSelectedCallback {
        return object : OnTargetStateSelectedCallback {
            override suspend fun onStateSelected(device: FhemDevice, targetState: String) {
                callback.widgetConfigurationCreated(WidgetConfiguration(appWidgetId,
                        widgetType, connectionId, listOf(device.name, targetState)))
            }

            override suspend fun onSubStateSelected(device: FhemDevice, state: String, subState: String) {
                callback.widgetConfigurationCreated(WidgetConfiguration(appWidgetId,
                        widgetType, connectionId, listOf(device.name, "$state $subState")))
            }

            override suspend fun onNothingSelected(device: FhemDevice) {}
        }
    }

    override fun supports(device: FhemDevice): Boolean =
            device.setList.entries.isNotEmpty()

    override val widgetType = WidgetType.TARGET_STATE
    override val widgetSize = WidgetSize.MEDIUM
}
