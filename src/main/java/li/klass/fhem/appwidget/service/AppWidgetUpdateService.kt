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

package li.klass.fhem.appwidget.service

import android.app.IntentService
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.widget.Toast
import com.google.common.base.Optional
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import li.klass.fhem.AndFHEMApplication
import li.klass.fhem.R
import li.klass.fhem.appwidget.AppWidgetDataHolder
import li.klass.fhem.appwidget.view.widget.base.DeviceAppWidgetView
import li.klass.fhem.constants.Actions
import li.klass.fhem.constants.Actions.*
import li.klass.fhem.constants.BundleExtraKeys.*
import li.klass.fhem.settings.SettingsKeys.ALLOW_REMOTE_UPDATE
import li.klass.fhem.update.backend.RoomListService
import li.klass.fhem.update.backend.RoomListUpdateService
import li.klass.fhem.util.ApplicationProperties
import li.klass.fhem.util.DateFormatUtil
import org.jetbrains.anko.coroutines.experimental.bg
import org.slf4j.LoggerFactory
import javax.inject.Inject


class AppWidgetUpdateService : IntentService(AppWidgetUpdateService::class.java.name) {

    @Inject
    lateinit var appWidgetDataHolder: AppWidgetDataHolder

    @Inject
    lateinit var applicationProperties: ApplicationProperties

    @Inject
    lateinit var roomListUpdateService: RoomListUpdateService

    override fun onCreate() {
        super.onCreate()
        (application as AndFHEMApplication).daggerComponent.inject(this)
    }

    override fun onHandleIntent(intent: Intent?) {
        val action = intent!!.action
        val allowRemoteUpdates = intent.getBooleanExtra(ALLOW_REMOTE_UPDATES, false)

        if (REDRAW_WIDGET == action) {
            handleRedrawWidget(intent, allowRemoteUpdates)
        } else if (REDRAW_ALL_WIDGETS == action) {
            LOG.info("onHandleIntent() - updating all widgets (received REDRAW_ALL_WIDGETS)")
            appWidgetDataHolder.updateAllWidgets(this, allowRemoteUpdates)
        } else if (WIDGET_REQUEST_UPDATE == action) {
            Handler(mainLooper).post { Toast.makeText(this@AppWidgetUpdateService, R.string.widget_remote_update_started, Toast.LENGTH_LONG).show() }
            sendBroadcast(Intent(Actions.DO_UPDATE)
                    .putExtra(DO_REFRESH, true))
        }
    }

    private fun handleRedrawWidget(intent: Intent, allowRemoteUpdates: Boolean) {
        if (!intent.hasExtra(APP_WIDGET_ID)) {
            return
        }

        val widgetId = intent.getIntExtra(APP_WIDGET_ID, -1)
        LOG.debug("handleRedrawWidget() - updating widget-id {}, remote update is {}", widgetId, allowRemoteUpdates)

        updateWidget(this, widgetId, allowRemoteUpdates)
    }

    private fun updateWidget(intentService: IntentService,
                             appWidgetId: Int, allowRemoteUpdate: Boolean) {
        val widgetConfigurationOptional = appWidgetDataHolder.getWidgetConfiguration(appWidgetId, this)

        if (!widgetConfigurationOptional.isPresent) {
            appWidgetDataHolder.deleteWidget(intentService, appWidgetId)
            LOG.info("updateWidget - widget with widget-id {} has been deleted", appWidgetId)
            return
        }

        val configuration = widgetConfigurationOptional.get()

        val updateInterval = appWidgetDataHolder.getConnectionDependentUpdateInterval(intentService)

        val doRemoteWidgetUpdates = applicationProperties.getBooleanSharedPreference(ALLOW_REMOTE_UPDATE, true)
        val viewCreateUpdateInterval = if (doRemoteWidgetUpdates && allowRemoteUpdate) updateInterval else RoomListService.Companion.NEVER_UPDATE_PERIOD

        appWidgetDataHolder.scheduleUpdateIntent(intentService, configuration, false, viewCreateUpdateInterval)

        val connectionId = configuration.connectionId
        LOG.info("updateWidget - request widget update for widget-id {}, interval is {}, update interval is {}ms, connectionId={}", appWidgetId, viewCreateUpdateInterval, updateInterval, connectionId.orNull())

        val serviceAsContext: Context = intentService
        async(UI) {
            bg {
                if (doRemoteWidgetUpdates) {
                    when {
                        configuration.widgetType.widgetView is DeviceAppWidgetView -> {
                            val deviceName = configuration.widgetType.widgetView.deviceNameFrom(configuration)
                            roomListUpdateService.updateSingleDevice(deviceName, connectionId, serviceAsContext, updateWidgets = false)
                        }
                        shouldUpdate(updateInterval, connectionId) -> roomListUpdateService.updateAllDevices(connectionId, serviceAsContext, updateWidgets = false)
                    }
                }
            }.await()
            updateWidgetAfterDeviceListReload(appWidgetId)
        }
    }

    private fun shouldUpdate(updatePeriod: Long, connectionId: Optional<String>): Boolean {
        if (updatePeriod == RoomListService.ALWAYS_UPDATE_PERIOD) {
            LOG.debug("shouldUpdate() : recommend update, as updatePeriod is set to ALWAYS_UPDATE")
            return true
        }
        if (updatePeriod == RoomListService.NEVER_UPDATE_PERIOD) {
            LOG.debug("shouldUpdate() : recommend no update, as updatePeriod is set to NEVER_UPDATE")
            return false
        }

        val lastUpdate = roomListUpdateService.getLastUpdate(connectionId, this)
        val shouldUpdate = lastUpdate + updatePeriod < System.currentTimeMillis()

        LOG.debug("shouldUpdate() : recommend {} update (lastUpdate: {}, updatePeriod: {} min)", if (!shouldUpdate) "no " else "to", DateFormatUtil.toReadable(lastUpdate), updatePeriod / 1000 / 60)

        return shouldUpdate
    }

    private fun updateWidgetAfterDeviceListReload(appWidgetId: Int) {

        val optional = appWidgetDataHolder.getWidgetConfiguration(appWidgetId, this)
        if (!optional.isPresent) {
            LOG.error("cannot find configuration for widget id {}", appWidgetId)
            return
        }

        val configuration = optional.get()

        val appWidgetManager = AppWidgetManager.getInstance(this)

        val widgetView = appWidgetDataHolder.getAppWidgetView(configuration)
        val content = widgetView.createView(this, configuration)

        try {
            appWidgetManager.updateAppWidget(appWidgetId, content)
        } catch (e: Exception) {
            LOG.error("updateWidgetAfterDeviceListReload() - something strange happened during appwidget update", e)
        }
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(AppWidgetUpdateService::class.java)!!
    }
}
