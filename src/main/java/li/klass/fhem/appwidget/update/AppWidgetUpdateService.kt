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

import android.content.Context
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import li.klass.fhem.appwidget.ui.widget.base.DeviceAppWidgetView
import li.klass.fhem.settings.SettingsKeys
import li.klass.fhem.update.backend.RoomListUpdateService
import li.klass.fhem.util.ApplicationProperties
import org.jetbrains.anko.coroutines.experimental.bg
import org.slf4j.LoggerFactory
import javax.inject.Inject

class AppWidgetUpdateService @Inject constructor(
        private val appWidgetInstanceManager: AppWidgetInstanceManager,
        private val appWidgetSchedulingService: AppWidgetSchedulingService,
        private val appWidgetDeletionService: AppWidgetDeletionService,
        private val applicationProperties: ApplicationProperties,
        private val roomListUpdateService: RoomListUpdateService
) {

    fun updateAllWidgets(context: Context) {
        appWidgetInstanceManager.getAllAppWidgetIds()
                .forEach { updateWidget(context, it) }
    }

    fun updateWidget(context: Context, appWidgetId: Int) {
        val configuration = appWidgetInstanceManager.getConfigurationFor(appWidgetId)
        if (configuration == null) {
            appWidgetDeletionService.deleteWidget(appWidgetId)
            LOG.info("updateWidget - widget with widget-id {} has been deleted", appWidgetId)
            return
        }

        val allowRemoteUpdates = applicationProperties.getBooleanSharedPreference(SettingsKeys.ALLOW_REMOTE_UPDATE, true)

        appWidgetSchedulingService.scheduleUpdate(configuration)

        val connectionId = configuration.connectionId
        LOG.info("updateWidget - request widget update for widget-id {}, connectionId={}", appWidgetId, connectionId.orNull())

        async(UI) {
            bg {
                if (allowRemoteUpdates) {
                    when {
                        configuration.widgetType.widgetView is DeviceAppWidgetView -> {
                            val deviceName = configuration.widgetType.widgetView.deviceNameFrom(configuration)
                            roomListUpdateService.updateSingleDevice(deviceName, connectionId, context, updateWidgets = false)
                        }
                        appWidgetSchedulingService.shouldUpdateDeviceList(connectionId) -> roomListUpdateService.updateAllDevices(connectionId, context, updateWidgets = false)
                    }
                }
            }.await()
            appWidgetInstanceManager.update(appWidgetId)
        }
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(AppWidgetUpdateIntentService::class.java)!!
    }
}