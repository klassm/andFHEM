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

package li.klass.fhem.appwidget.ui.widget.base

import android.app.PendingIntent
import android.content.Context
import android.widget.RemoteViews
import androidx.navigation.NavDeepLinkBuilder
import li.klass.fhem.R
import li.klass.fhem.activities.AndFHEMMainActivity
import li.klass.fhem.adapter.devices.core.detail.DeviceDetailRedirectFragmentArgs
import li.klass.fhem.appwidget.ui.widget.WidgetConfigurationCreatedCallback
import li.klass.fhem.appwidget.update.WidgetConfiguration
import li.klass.fhem.domain.core.FhemDevice
import li.klass.fhem.update.backend.DeviceListService
import li.klass.fhem.update.backend.device.configuration.DeviceConfigurationProvider
import li.klass.fhem.util.toHtml
import org.slf4j.LoggerFactory
import javax.inject.Inject

abstract class DeviceAppWidgetView : AppWidgetView() {

    @Inject
    lateinit var deviceListService: DeviceListService

    @Inject
    lateinit var deviceConfigurationProvider: DeviceConfigurationProvider

    open fun supports(device: FhemDevice): Boolean =
            supportsFromJsonConfiguration(device)

    private fun supportsFromJsonConfiguration(device: FhemDevice): Boolean {
        val configuration = deviceConfigurationProvider.configurationFor(device)
        val supportedWidgets = configuration.supportedWidgets
        return supportedWidgets
                .any { javaClass.simpleName.equals(it, ignoreCase = true) }
    }

    override fun createView(context: Context, widgetConfiguration: WidgetConfiguration): RemoteViews {
        val views = super.createView(context, widgetConfiguration)
        logger.info("createView - creating appwidget view for $widgetConfiguration")

        if (shouldSetDeviceName()) {
            val deviceName = deviceNameFrom(widgetConfiguration)

            val device = getDeviceFor(deviceName, widgetConfiguration.connectionId)
            if (device == null) {
                logger.info("createView - device is null, ignoring update", device)
            } else {
                views.setTextViewText(R.id.deviceName, device.widgetName.toHtml())
            }
        }

        return views
    }

    fun deviceNameFrom(widgetConfiguration: WidgetConfiguration): String =
            widgetConfiguration.payload[0]

    open fun shouldSetDeviceName(): Boolean = true

    private fun getDeviceFor(deviceName: String, connectionId: String?): FhemDevice? =
            deviceListService.getDeviceForName(deviceName, connectionId)

    protected fun openDeviceDetailPageWhenClicking(viewId: Int, view: RemoteViews, device: FhemDevice, widgetConfiguration: WidgetConfiguration, context: Context) {
        val pendingIntent = createOpenDeviceDetailPageIntent(device, widgetConfiguration, context)

        view.setOnClickPendingIntent(viewId, pendingIntent)
    }

    fun createOpenDeviceDetailPageIntent(device: FhemDevice, widgetConfiguration: WidgetConfiguration, context: Context): PendingIntent =
            NavDeepLinkBuilder(context)
                    .setComponentName(AndFHEMMainActivity::class.java)
                    .setGraph(R.navigation.nav_graph)
                    .setDestination(R.id.deviceDetailRedirectFragment)
                    .setArguments(DeviceDetailRedirectFragmentArgs(device.name, widgetConfiguration.connectionId).toBundle())
                    .createPendingIntent()

    override fun createWidgetConfiguration(context: Context, appWidgetId: Int, connectionId: String,
                                           callback: WidgetConfigurationCreatedCallback, vararg payload: String) {
        val device = deviceListService.getDeviceForName(payload[0])
        if (device != null) {
            createDeviceWidgetConfiguration(context, appWidgetId, connectionId, device, callback)
        } else {
            logger.info("cannot find device for " + payload[0])
        }
    }

    protected fun valueForMarker(device: FhemDevice, annotationCls: Class<out Annotation>): String? {
        val configuration = deviceConfigurationProvider.configurationFor(device)
        val states = configuration.states
        return states
                .filter { it.markers.contains(annotationCls.simpleName) }
                .mapNotNull { device.xmlListDevice.stateValueFor(it.key) }
                .firstOrNull()
    }

    protected open fun createDeviceWidgetConfiguration(context: Context, appWidgetId: Int, connectionId: String, device: FhemDevice,
                                                       callback: WidgetConfigurationCreatedCallback) {
        callback.widgetConfigurationCreated(WidgetConfiguration(appWidgetId, widgetType, connectionId, listOf(device.name)))
    }

    override fun fillWidgetView(context: Context, view: RemoteViews,
                                widgetConfiguration: WidgetConfiguration) {
        val device = getDeviceFor(deviceNameFrom(widgetConfiguration), widgetConfiguration.connectionId)
        if (device != null) {
            view.setTextViewText(R.id.deviceName, device.widgetName.toHtml())
            fillWidgetView(context, view, device, widgetConfiguration)
        } else {
            logger.info("cannot find device for " + deviceNameFrom(widgetConfiguration))
        }
    }

    protected abstract fun fillWidgetView(context: Context, view: RemoteViews, device: FhemDevice,
                                          widgetConfiguration: WidgetConfiguration)

    companion object {
        val logger = LoggerFactory.getLogger(DeviceAppWidgetView::class.java)!!
    }
}
