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

package li.klass.fhem.appwidget.update

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import li.klass.fhem.appwidget.ui.widget.WidgetTypeProvider
import li.klass.fhem.appwidget.ui.widget.base.DeviceAppWidgetView
import li.klass.fhem.settings.SettingsKeys
import li.klass.fhem.update.backend.DeviceListUpdateService
import li.klass.fhem.util.ApplicationProperties
import org.slf4j.LoggerFactory
import javax.inject.Inject

class AppWidgetUpdateService @Inject constructor(
        private val appWidgetInstanceManager: AppWidgetInstanceManager,
        private val appWidgetSchedulingService: AppWidgetSchedulingService,
        private val applicationProperties: ApplicationProperties,
        private val deviceListUpdateService: DeviceListUpdateService,
        private val widgetTypeProvider: WidgetTypeProvider
) {

    fun updateAllWidgets() {
        LOG.info("updateAllWidgets")
        appWidgetInstanceManager.getExistingWidgetIds()
                .forEach { updateWidget(it) }
    }

    fun updateWidget(appWidgetId: Int) {
        LOG.info("updateWidget - appWidgetId=$appWidgetId")
        appWidgetInstanceManager.update(appWidgetId)
    }

    suspend fun doRemoteUpdate(appWidgetId: Int): Int {
        val configuration = appWidgetInstanceManager.getConfigurationFor(appWidgetId)
        val allowRemoteUpdates = applicationProperties.getBooleanSharedPreference(SettingsKeys.ALLOW_REMOTE_UPDATE, true)
        val connectionId = configuration?.connectionId
        if (configuration == null || !allowRemoteUpdates) {
            return appWidgetId
        }

        LOG.info("doRemoteUpdate - updating data for widget-id {}, connectionId={}", appWidgetId, connectionId)

        coroutineScope {
            withContext(Dispatchers.IO) {
                val widgetView = widgetTypeProvider.widgetFor(configuration.widgetType)
                if (widgetView is DeviceAppWidgetView) {
                    val deviceName = widgetView.deviceNameFrom(configuration)
                    handleDeviceUpdate(deviceName, connectionId)
                } else {
                    handleOtherUpdate(connectionId)
                }
            }
        }

        return appWidgetId
    }

    private fun handleOtherUpdate(connectionId: String?) {
        if (appWidgetSchedulingService.shouldUpdateDeviceList(connectionId)) {
            deviceListUpdateService.updateAllDevices(connectionId)
        } else {
            LOG.info("handleOtherUpdate - skipping update, as device list is recent enough")
        }
    }

    private fun handleDeviceUpdate(deviceName: String, connectionId: String?) {
        if (appWidgetSchedulingService.shouldUpdateDevice(connectionId, deviceName)) {
            deviceListUpdateService.updateSingleDevice(deviceName, connectionId)
        } else {
            LOG.info("handleDeviceUpdate(deviceName=$deviceName) - skipping update, as device list is recent enough")
        }
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(AppWidgetUpdateService::class.java)!!
    }
}