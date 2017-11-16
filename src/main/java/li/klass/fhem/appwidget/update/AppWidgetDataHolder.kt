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
import android.app.PendingIntent
import android.appwidget.AppWidgetHost
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import com.google.common.base.Optional
import li.klass.fhem.appwidget.ui.widget.base.AppWidgetView
import li.klass.fhem.constants.Actions
import li.klass.fhem.constants.BundleExtraKeys.ALLOW_REMOTE_UPDATES
import li.klass.fhem.constants.BundleExtraKeys.APP_WIDGET_ID
import li.klass.fhem.update.backend.RoomListService
import li.klass.fhem.util.ApplicationProperties
import li.klass.fhem.util.NetworkState
import li.klass.fhem.util.preferences.SharedPreferencesService
import org.slf4j.LoggerFactory
import java.lang.Integer.parseInt
import javax.inject.Inject

class AppWidgetDataHolder @Inject
constructor() {

    @Inject
    lateinit var applicationProperties: ApplicationProperties
    @Inject
    lateinit var sharedPreferencesService: SharedPreferencesService

    fun updateAllWidgets(context: Context, allowRemoteUpdate: Boolean) {
        val appWidgetIds = getAllAppWidgetIds(context)
        for (appWidgetId in appWidgetIds) {
            context.startService(getRedrawWidgetIntent(context, parseInt(appWidgetId), allowRemoteUpdate))
        }
    }

    fun getAllAppWidgetIds(context: Context): Set<String> {
        val sharedPreferences = getSavedPreferences(context)
        val allEntries = sharedPreferences.all!!

        return allEntries.keys
    }

    private fun getRedrawWidgetIntent(context: Context, appWidgetId: Int, allowRemoteUpdate: Boolean): Intent {
        return Intent(Actions.REDRAW_WIDGET)
                .setClass(context, AppWidgetUpdateIntentService::class.java)
                .putExtra(APP_WIDGET_ID, appWidgetId)
                .putExtra(ALLOW_REMOTE_UPDATES, allowRemoteUpdate)
    }

    private fun getSavedPreferences(context: Context): SharedPreferences =
            sharedPreferencesService.getPreferences(SAVE_PREFERENCE_NAME, context)


    fun getAppWidgetView(configuration: WidgetConfiguration): AppWidgetView =
            configuration.widgetType.widgetView

    fun deleteWidget(context: Context, appWidgetId: Int) {
        Log.d(AppWidgetDataHolder::class.java.name, String.format("deleting widget for id %d", appWidgetId))

        val preferences = getSavedPreferences(context)
        val key = appWidgetId.toString()
        if (preferences.contains(key)) {
            preferences.edit().remove(key).apply()

            val host = getAppWidgetHost(context)
            host.deleteAppWidgetId(appWidgetId)
        }

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        cancelUpdating(context, appWidgetId, alarmManager)
    }

    fun getAppWidgetHost(context: Context): AppWidgetHost = AppWidgetHost(context, 0)

    private fun cancelUpdating(context: Context, appWidgetId: Int, alarmManager: AlarmManager) {
        val updatePendingIntent = updatePendingIndentForWidgetId(context, appWidgetId)
        alarmManager.cancel(updatePendingIntent)
    }

    fun scheduleUpdateIntent(context: Context, widgetConfiguration: WidgetConfiguration,
                             updateImmediately: Boolean, widgetUpdateInterval: Long) {
        if (widgetUpdateInterval > 0) {
            LOG.debug(String.format("scheduling widget update %s => %s, updateImmediately=%b ", widgetConfiguration.toString(), (widgetUpdateInterval / 1000).toString() + "s", updateImmediately))

            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val pendingIntent = updatePendingIndentForWidgetId(context, widgetConfiguration.widgetId)
            val now = System.currentTimeMillis()
            val firstRun = if (updateImmediately) now else now + widgetUpdateInterval

            cancelUpdating(context, widgetConfiguration.widgetId, alarmManager)
            if (pendingIntent != null) {
                alarmManager.setRepeating(AlarmManager.RTC, firstRun, widgetUpdateInterval, pendingIntent)
            }
        }
    }

    private fun updatePendingIndentForWidgetId(context: Context, widgetId: Int): PendingIntent? {
        val updateIntent = getRedrawWidgetIntent(context, widgetId, true)

        return PendingIntent.getService(context, widgetId * -1,
                updateIntent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    fun getConnectionDependentUpdateInterval(context: Context): Long {
        val updateInterval: Long

        if (!NetworkState.isConnected(context)) {
            LOG.debug("getConnectionDependentUpdateInterval - no network connection")
            updateInterval = RoomListService.Companion.NEVER_UPDATE_PERIOD
        } else if (NetworkState.isConnectedMobile(context)) {
            LOG.debug("getConnectionDependentUpdateInterval - mobile connection")
            updateInterval = getWidgetUpdateIntervalFor(WIDGET_UPDATE_INTERVAL_PREFERENCES_KEY_MOBILE).toLong()
        } else {
            LOG.debug("getConnectionDependentUpdateInterval - wlan connection")
            updateInterval = getWidgetUpdateIntervalFor(WIDGET_UPDATE_INTERVAL_PREFERENCES_KEY_WLAN).toLong()
        }

        return updateInterval
    }

    fun getWidgetConfiguration(widgetId: Int, context: Context): Optional<WidgetConfiguration> {
        val sharedPreferences = getSavedPreferences(context)
        val value = sharedPreferences.getString(widgetId.toString(), null)

        if (value == null) {
            return Optional.absent<WidgetConfiguration>()
        } else {
            return Optional.fromNullable(WidgetConfiguration.fromSaveString(value))
        }
    }

    fun saveWidgetConfigurationToPreferences(widgetConfiguration: WidgetConfiguration, context: Context) {
        val edit = sharedPreferencesService.getSharedPreferencesEditor(SAVE_PREFERENCE_NAME, context)
        val value = widgetConfiguration.toSaveString()
        edit.putString(widgetConfiguration.widgetId.toString(), value)
        edit.apply()
    }

    private fun getWidgetUpdateIntervalFor(key: String): Int {
        val value = applicationProperties.getStringSharedPreference(key, "3600")
        val intValue = parseInt(value)
        return intValue * 1000
    }

    companion object {
        private val WIDGET_UPDATE_INTERVAL_PREFERENCES_KEY_WLAN = "WIDGET_UPDATE_INTERVAL_WLAN"
        private val WIDGET_UPDATE_INTERVAL_PREFERENCES_KEY_MOBILE = "WIDGET_UPDATE_INTERVAL_MOBILE"
        val SAVE_PREFERENCE_NAME = AppWidgetDataHolder::class.java.name
        val LOG = LoggerFactory.getLogger(AppWidgetDataHolder::class.java)
    }
}
