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

import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.google.common.base.Optional
import li.klass.fhem.constants.Actions
import li.klass.fhem.constants.BundleExtraKeys.ALLOW_REMOTE_UPDATES
import li.klass.fhem.constants.BundleExtraKeys.APP_WIDGET_ID
import li.klass.fhem.update.backend.RoomListService
import li.klass.fhem.update.backend.RoomListUpdateService
import li.klass.fhem.util.DateFormatUtil
import org.slf4j.LoggerFactory
import javax.inject.Inject

class AppWidgetSchedulingService @Inject constructor(
        private val application: Application,
        private val updateIntervalProvider: AppWidgetUpdateIntervalProvider,
        private val roomListUpdateService: RoomListUpdateService
) {

    fun cancelUpdating(appWidgetId: Int) {
        val updatePendingIntent = updatePendingIndentForWidgetId(appWidgetId)
        alarmManager.cancel(updatePendingIntent)
    }

    fun scheduleUpdate(widgetConfiguration: WidgetConfiguration) {
        val interval = updateIntervalProvider.getConnectionDependentUpdateInterval()
        if (interval > 0) {
            LOG.debug(String.format("scheduling widget update %s => %s", widgetConfiguration.toString(), (interval / 1000).toString() + "s"))

            val pendingIntent = updatePendingIndentForWidgetId(widgetConfiguration.widgetId)
            val now = System.currentTimeMillis()
            val firstRun = now + interval

            cancelUpdating(widgetConfiguration.widgetId)
            if (pendingIntent != null) {
                alarmManager.setRepeating(AlarmManager.RTC, firstRun, interval, pendingIntent)
            }
        }
    }

    fun shouldUpdateDeviceList(connectionId: Optional<String>): Boolean {
        val updatePeriod = updateIntervalProvider.getConnectionDependentUpdateInterval()
        if (updatePeriod == RoomListService.ALWAYS_UPDATE_PERIOD) {
            LOG.debug("shouldUpdateDeviceList() : recommend update, as updatePeriod is set to ALWAYS_UPDATE")
            return true
        }
        if (updatePeriod == RoomListService.NEVER_UPDATE_PERIOD) {
            LOG.debug("shouldUpdateDeviceList() : recommend no update, as updatePeriod is set to NEVER_UPDATE")
            return false
        }

        val lastUpdate = roomListUpdateService.getLastUpdate(connectionId, applicationContext)
        val shouldUpdate = lastUpdate + updatePeriod < System.currentTimeMillis()

        LOG.debug("shouldUpdateDeviceList() : recommend {} update (lastUpdate: {}, updatePeriod: {} min)", if (!shouldUpdate) "no " else "to", DateFormatUtil.toReadable(lastUpdate), updatePeriod / 1000 / 60)

        return shouldUpdate
    }

    private fun updatePendingIndentForWidgetId(widgetId: Int): PendingIntent? {
        val updateIntent = getRedrawWidgetIntent(widgetId, true)

        return PendingIntent.getService(applicationContext, widgetId * -1,
                updateIntent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    private fun getRedrawWidgetIntent(appWidgetId: Int, allowRemoteUpdate: Boolean): Intent {
        return Intent(Actions.REDRAW_WIDGET)
                .setClass(applicationContext, AppWidgetUpdateIntentService::class.java)
                .putExtra(APP_WIDGET_ID, appWidgetId)
                .putExtra(ALLOW_REMOTE_UPDATES, allowRemoteUpdate)
    }

    private val applicationContext: Context get() = application.applicationContext
    private val alarmManager: AlarmManager get() = applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager


    companion object {
        val LOG = LoggerFactory.getLogger(AppWidgetSchedulingService::class.java)
    }
}