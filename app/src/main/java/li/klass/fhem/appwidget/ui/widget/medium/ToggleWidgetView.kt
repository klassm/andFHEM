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
import li.klass.fhem.adapter.devices.hook.ButtonHook
import li.klass.fhem.adapter.devices.hook.DeviceHookProvider
import li.klass.fhem.adapter.devices.toggle.OnOffBehavior
import li.klass.fhem.appwidget.ui.widget.base.DeviceAppWidgetView
import li.klass.fhem.appwidget.update.WidgetConfiguration
import li.klass.fhem.constants.Actions
import li.klass.fhem.constants.BundleExtraKeys.*
import li.klass.fhem.dagger.ApplicationComponent
import li.klass.fhem.domain.core.FhemDevice
import li.klass.fhem.domain.core.ToggleableDevice
import li.klass.fhem.service.intent.DeviceIntentService
import javax.inject.Inject

open class ToggleWidgetView : DeviceAppWidgetView() {
    @Inject
    lateinit var deviceHookProvider: DeviceHookProvider
    @Inject
    lateinit var onOffBehavior: OnOffBehavior

    override fun getWidgetName(): Int = R.string.widget_toggle

    override fun getContentView(): Int = R.layout.appwidget_toggle

    override fun fillWidgetView(context: Context, view: RemoteViews, device: FhemDevice, widgetConfiguration: WidgetConfiguration) {
        val isOn = onOffBehavior.isOnConsideringHooks(device)
        val actionIntent = actionIntentFor(device, widgetConfiguration, context)

        if (isOn) {
            view.setViewVisibility(R.id.toggleOff, View.GONE)
            view.setViewVisibility(R.id.toggleOn, View.VISIBLE)
            view.setTextViewText(R.id.toggleOn, device.getEventMapStateFor(deviceHookProvider.getOnStateName(device) ?: onOffBehavior.getOnStateName(device)))
        } else {
            view.setViewVisibility(R.id.toggleOff, View.VISIBLE)
            view.setViewVisibility(R.id.toggleOn, View.GONE)
            view.setTextViewText(R.id.toggleOff, device.getEventMapStateFor(deviceHookProvider.getOffStateName(device) ?: onOffBehavior.getOffStateName(device)))
        }

        val pendingIntent = PendingIntent.getService(context, widgetConfiguration.widgetId, actionIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        view.setOnClickPendingIntent(R.id.toggleOff, pendingIntent)
        view.setOnClickPendingIntent(R.id.toggleOn, pendingIntent)

        openDeviceDetailPageWhenClicking(R.id.main, view, device, widgetConfiguration, context)
    }

    private fun actionIntentFor(device: FhemDevice, widgetConfiguration: WidgetConfiguration, context: Context): Intent {
        val hook = deviceHookProvider.buttonHookFor(device)

        val actionIntent = when (hook) {
            ButtonHook.ON_DEVICE -> actionIntentForOnOffDevice(device, widgetConfiguration)
                    .putExtra(DEVICE_TARGET_STATE, deviceHookProvider.getOnStateName(device) ?: onOffBehavior.getOnStateName(device))
            ButtonHook.OFF_DEVICE -> actionIntentForOnOffDevice(device, widgetConfiguration)
                    .putExtra(DEVICE_TARGET_STATE, deviceHookProvider.getOffStateName(device) ?: onOffBehavior.getOffStateName(device))
            else -> Intent(Actions.DEVICE_WIDGET_TOGGLE)
                    .putExtra(APP_WIDGET_ID, widgetConfiguration.widgetId)
                    .putExtra(DEVICE_NAME, device.name)
                    .putExtra(CONNECTION_ID, widgetConfiguration.connectionId)
        }
        return actionIntent
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .setClass(context, DeviceIntentService::class.java)
    }

    private fun actionIntentForOnOffDevice(device: FhemDevice, widgetConfiguration: WidgetConfiguration): Intent =
            Intent(Actions.DEVICE_SET_STATE)
                    .putExtra(DEVICE_NAME, device.name)
                    .putExtra(CONNECTION_ID, widgetConfiguration.connectionId)

    override fun supports(device: FhemDevice, context: Context): Boolean =
            onOffBehavior.supports(device)

    override fun inject(applicationComponent: ApplicationComponent) {
        applicationComponent.inject(this)
    }
}
