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
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.google.common.collect.ImmutableList
import li.klass.fhem.R
import li.klass.fhem.adapter.devices.genericui.AvailableTargetStatesDialogUtil
import li.klass.fhem.adapter.devices.genericui.availableTargetStates.OnTargetStateSelectedCallback
import li.klass.fhem.appwidget.action.AppWidgetBroadcastReceiver
import li.klass.fhem.appwidget.ui.widget.WidgetConfigurationCreatedCallback
import li.klass.fhem.appwidget.ui.widget.WidgetSize
import li.klass.fhem.appwidget.ui.widget.WidgetType
import li.klass.fhem.appwidget.ui.widget.activity.TargetStateAdditionalInformationActivity
import li.klass.fhem.appwidget.ui.widget.base.DeviceAppWidgetView
import li.klass.fhem.appwidget.update.WidgetConfiguration
import li.klass.fhem.constants.Actions
import li.klass.fhem.constants.BundleExtraKeys
import li.klass.fhem.domain.core.DeviceStateRequiringAdditionalInformation.requiresAdditionalInformation
import li.klass.fhem.domain.core.FhemDevice
import javax.inject.Inject

class TargetStateWidgetView @Inject constructor() : DeviceAppWidgetView() {
    override fun getWidgetName(): Int = R.string.widget_targetstate

    override fun getContentView(): Int = R.layout.appwidget_targetstate

    override fun fillWidgetView(context: Context, view: RemoteViews, device: FhemDevice, widgetConfiguration: WidgetConfiguration) {
        val payload = widgetConfiguration.payload[1]
        val state = device.getEventMapStateFor(payload)

        view.setTextViewText(R.id.button, state)

        val pendingIntent: PendingIntent
        if (requiresAdditionalInformation(state)) {
            val actionIntent = Intent(context, TargetStateAdditionalInformationActivity::class.java)
                    .putExtra(BundleExtraKeys.DEVICE_TARGET_STATE, payload)
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

            pendingIntent = PendingIntent.getActivity(context, widgetConfiguration.widgetId,
                    actionIntent, FLAG_UPDATE_CURRENT)
        } else {
            val actionIntent = Intent(context, AppWidgetBroadcastReceiver::class.java)
                    .setAction(Actions.DEVICE_WIDGET_TARGET_STATE)
                    .putExtra(BundleExtraKeys.DEVICE_NAME, device.name)
                    .putExtra(BundleExtraKeys.DEVICE_TARGET_STATE, payload)
                    .putExtra(BundleExtraKeys.CONNECTION_ID, widgetConfiguration.connectionId)

            pendingIntent = PendingIntent.getBroadcast(context, widgetConfiguration.widgetId, actionIntent,
                    FLAG_UPDATE_CURRENT)
        }

        view.setOnClickPendingIntent(R.id.button, pendingIntent)

        openDeviceDetailPageWhenClicking(R.id.main, view, device, widgetConfiguration, context)
    }


    override fun createDeviceWidgetConfiguration(context: Context,
                                                 appWidgetId: Int, device: FhemDevice,
                                                 callback: WidgetConfigurationCreatedCallback) {

        AvailableTargetStatesDialogUtil.showSwitchOptionsMenu(context, device, widgetCreatingCallback(widgetType, appWidgetId, callback))
    }

    private fun widgetCreatingCallback(widgetType: WidgetType, appWidgetId: Int, callback: WidgetConfigurationCreatedCallback): OnTargetStateSelectedCallback<*> {
        return object : OnTargetStateSelectedCallback<FhemDevice> {
            override fun onStateSelected(device: FhemDevice, targetState: String) {
                callback.widgetConfigurationCreated(WidgetConfiguration(appWidgetId,
                        widgetType, getCurrentConnectionId(), ImmutableList.of(device.name, targetState)))
            }

            override fun onSubStateSelected(device: FhemDevice, state: String, subState: String) {
                callback.widgetConfigurationCreated(WidgetConfiguration(appWidgetId,
                        widgetType, getCurrentConnectionId(), ImmutableList.of(device.name, state + " " + subState)))
            }

            override fun onNothingSelected(device: FhemDevice) {}
        }
    }

    override fun supports(device: FhemDevice): Boolean =
            !device.setList.entries.isEmpty()

    override val widgetType = WidgetType.TARGET_STATE
    override val widgetSize = WidgetSize.MEDIUM
}
