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
import android.content.Intent
import android.os.SystemClock
import android.widget.RemoteViews
import com.google.common.collect.ImmutableList
import li.klass.fhem.R
import li.klass.fhem.activities.AndFHEMMainActivity
import li.klass.fhem.appwidget.annotation.SupportsWidget
import li.klass.fhem.appwidget.ui.widget.WidgetConfigurationCreatedCallback
import li.klass.fhem.appwidget.ui.widget.WidgetType
import li.klass.fhem.appwidget.update.WidgetConfiguration
import li.klass.fhem.connection.backend.ConnectionService
import li.klass.fhem.constants.BundleExtraKeys.*
import li.klass.fhem.domain.core.FhemDevice
import li.klass.fhem.ui.FragmentType
import li.klass.fhem.update.backend.DeviceListService
import li.klass.fhem.update.backend.device.configuration.DeviceConfigurationProvider
import li.klass.fhem.util.ReflectionUtil.getValueAndDescriptionForAnnotation
import org.slf4j.LoggerFactory
import javax.inject.Inject

abstract class DeviceAppWidgetView : AppWidgetView() {

    @Inject
    lateinit var deviceListService: DeviceListService

    @Inject
    lateinit var deviceConfigurationProvider: DeviceConfigurationProvider

    @Inject
    lateinit var connectionService: ConnectionService

    open fun supports(device: FhemDevice, context: Context): Boolean {
        val supportsFromJson = supportsFromJsonConfiguration(device)
        val supportsFromAnnotation = supportsFromAnnotation(device)

        return supportsFromJson || supportsFromAnnotation
    }

    private fun supportsFromAnnotation(device: FhemDevice): Boolean {
        if (!device.javaClass.isAnnotationPresent(SupportsWidget::class.java)) return false

        if (!device.supportsWidget(this.javaClass)) {
            return false
        }

        val annotation = device.javaClass.getAnnotation(SupportsWidget::class.java)
        val supportedWidgetViews = annotation.value.toList()
        return supportedWidgetViews.any { it == javaClass }
    }

    private fun supportsFromJsonConfiguration(device: FhemDevice): Boolean {
        val deviceConfiguration = device.deviceConfiguration
        val supportedWidgets = deviceConfiguration.supportedWidgets
        supportedWidgets
                .filter { javaClass.simpleName.equals(it, ignoreCase = true) }
                .forEach { return true }
        return false
    }

    override fun createView(context: Context, widgetConfiguration: WidgetConfiguration): RemoteViews {
        val views = super.createView(context, widgetConfiguration)
        logger.info("creating appwidget view for " + widgetConfiguration)

        if (shouldSetDeviceName()) {
            val deviceName = deviceNameFrom(widgetConfiguration)

            val device = getDeviceFor(deviceName, widgetConfiguration.connectionId)
            views.setTextViewText(R.id.deviceName, device?.widgetName ?: "????")
        }

        return views
    }

    fun deviceNameFrom(widgetConfiguration: WidgetConfiguration): String =
            widgetConfiguration.payload[0]

    open fun shouldSetDeviceName(): Boolean = true

    private fun getDeviceFor(deviceName: String, connectionId: String?): FhemDevice? =
            deviceListService.getDeviceForName(deviceName, connectionId)

    protected fun openDeviceDetailPageWhenClicking(viewId: Int, view: RemoteViews, device: FhemDevice, widgetConfiguration: WidgetConfiguration, context: Context) {
        val pendingIntent = createOpenDeviceDetailPagePendingIntent(device, widgetConfiguration, context)

        view.setOnClickPendingIntent(viewId, pendingIntent)
    }

    protected fun createOpenDeviceDetailPagePendingIntent(device: FhemDevice, widgetConfiguration: WidgetConfiguration, context: Context): PendingIntent {
        val openIntent = createOpenDeviceDetailPageIntent(device, widgetConfiguration, context)
        return PendingIntent.getActivity(context, widgetConfiguration.widgetId, openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT)
    }

    private fun createOpenDeviceDetailPageIntent(device: FhemDevice, widgetConfiguration: WidgetConfiguration, context: Context): Intent {
        return Intent(context, AndFHEMMainActivity::class.java)
                .setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                .putExtra(FRAGMENT, FragmentType.DEVICE_DETAIL)
                .putExtra(DEVICE_NAME, device.name)
                .putExtra(CONNECTION_ID, widgetConfiguration.connectionId)
                .putExtra("unique", "foobar://" + SystemClock.elapsedRealtime())
    }

    override fun createWidgetConfiguration(context: Context, widgetType: WidgetType, appWidgetId: Int,
                                           callback: WidgetConfigurationCreatedCallback, vararg payload: String) {
        val device = deviceListService.getDeviceForName<FhemDevice>(payload[0])
        if (device != null) {
            createDeviceWidgetConfiguration(context, widgetType, appWidgetId, device, callback)
        } else {
            logger.info("cannot find device for " + payload[0])
        }
    }

    protected fun valueForAnnotation(device: FhemDevice, annotationCls: Class<out Annotation>, context: Context): String? {
        val configuration = deviceConfigurationProvider.configurationFor(device)
        val states = configuration.states
        states
                .filter { it.markers.contains(annotationCls.simpleName) }
                .forEach { return device.xmlListDevice.stateValueFor(it.key).orNull() }
        return getValueAndDescriptionForAnnotation(device, annotationCls, context)
    }

    protected open fun createDeviceWidgetConfiguration(context: Context, widgetType: WidgetType, appWidgetId: Int,
                                                       device: FhemDevice, callback: WidgetConfigurationCreatedCallback) {
        val connectionId = connectionService.getSelectedId()
        callback.widgetConfigurationCreated(WidgetConfiguration(appWidgetId, widgetType, connectionId, ImmutableList.of(device.name!!)))
    }

    protected fun getCurrentConnectionId(): String =
            connectionService.getSelectedId()

    override fun fillWidgetView(context: Context, view: RemoteViews,
                                widgetConfiguration: WidgetConfiguration) {
        val device = getDeviceFor(deviceNameFrom(widgetConfiguration), widgetConfiguration.connectionId)
        if (device != null) {
            view.setTextViewText(R.id.deviceName, device.widgetName)
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
