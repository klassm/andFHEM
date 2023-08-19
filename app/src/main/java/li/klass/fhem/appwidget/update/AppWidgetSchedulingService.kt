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
import li.klass.fhem.constants.Actions
import li.klass.fhem.constants.BundleExtraKeys.ALLOW_REMOTE_UPDATES
import li.klass.fhem.constants.BundleExtraKeys.APP_WIDGET_ID
import li.klass.fhem.update.backend.DeviceListService
import li.klass.fhem.update.backend.DeviceListUpdateService
import li.klass.fhem.util.DateFormatUtil
import org.joda.time.DateTime
import org.slf4j.LoggerFactory
import javax.inject.Inject

class AppWidgetSchedulingService @Inject constructor(
        private val application: Application,
        private val updateIntervalProvider: AppWidgetUpdateIntervalProvider,
        private val deviceListUpdateService: DeviceListUpdateService,
        private val deviceListService: DeviceListService
) {

    @Deprecated(message = "To be removed when cancelling of widget updates is released")
    fun cancelUpdating(appWidgetId: Int) {
        val updatePendingIntent = updatePendingIndentForWidgetId(appWidgetId)
        if (updatePendingIntent != null) {
            alarmManager.cancel(updatePendingIntent)
        }
    }

    fun shouldUpdateDeviceList(connectionId: String?): Boolean {
        val lastUpdate = deviceListUpdateService.getLastUpdate(connectionId)
        return if (lastUpdate == null) true else shouldUpdate(lastUpdate)
    }

    fun shouldUpdateDevice(connectionId: String?, deviceName: String): Boolean =
            deviceListService.getDeviceForName(deviceName, connectionId)
                    ?.xmlListDevice?.creationTime?.let { shouldUpdate(it) } ?: false

    private fun shouldUpdate(lastUpdate: DateTime): Boolean {
        val updatePeriod = updateIntervalProvider.getConnectionDependentUpdateInterval()
        if (updatePeriod == DeviceListService.ALWAYS_UPDATE_PERIOD) {
            LOG.info(
                    "shouldUpdateDeviceList() : recommend update, as updatePeriod is set to ALWAYS_UPDATE")
            return true
        }
        if (updatePeriod == DeviceListService.NEVER_UPDATE_PERIOD) {
            LOG.info(
                    "shouldUpdateDeviceList() : recommend no update, as updatePeriod is set to NEVER_UPDATE")
            return false
        }

        val shouldUpdate = (lastUpdate + updatePeriod).isBeforeNow

        LOG.info(
                "shouldUpdateDeviceList() : recommend {} update (lastUpdate: {}, updatePeriod: {} min)",
                if (!shouldUpdate) "no " else "to", DateFormatUtil.toReadable(lastUpdate),
                updatePeriod / 1000 / 60)

        return shouldUpdate
    }

    @Deprecated(message = "To be removed when cancelling of widget updates is released")
    private fun updatePendingIndentForWidgetId(widgetId: Int): PendingIntent? {
        val updateIntent = getRedrawWidgetIntent(widgetId, true)

        return PendingIntent.getService(
            applicationContext, widgetId * -1,
            updateIntent, PendingIntent.FLAG_IMMUTABLE
        )
    }

    @Deprecated(message = "To be removed when cancelling of widget updates is released")
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