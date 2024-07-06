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

import android.app.Application
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.SharedPreferences
import li.klass.fhem.appwidget.ui.widget.WidgetTypeProvider
import li.klass.fhem.util.preferences.SharedPreferencesService
import org.slf4j.LoggerFactory
import javax.inject.Inject

class AppWidgetInstanceManager @Inject constructor(
        private val sharedPreferencesService: SharedPreferencesService,
        private val appWidgetSchedulingService: AppWidgetSchedulingService,
        private val application: Application,
        private val widgetProvider: WidgetTypeProvider
) {
    fun delete(widgetId: Int) {
        val preferences = getSavedPreferences()
        if (preferences.contains(widgetId.toString())) {
            LOG.info("delete(widgetId=$widgetId)")
            preferences.edit().remove(widgetId.toString()).apply()
        }
    }

    fun update(widgetId: Int) {
        val configuration = getConfigurationFor(widgetId)

        // Discard any existing update intents
        appWidgetSchedulingService.cancelUpdating(widgetId)

        if (configuration == null) {
            LOG.error("cannot find configuration for widget id {}", widgetId)
            return
        }

        try {
            val widgetView = widgetProvider.widgetFor(configuration.widgetType)
            val content = widgetView.createView(applicationContext, configuration)
            appWidgetManager.updateAppWidget(widgetId, content)
        } catch (e: Exception) {
            LOG.error("updateWidgetAfterDeviceListReload() - something strange happened during appwidget update", e)
        }
    }

    fun getExistingWidgetIds(): Set<Int> {
        val all = getAllAppWidgetIds()
        val withInfo = all.map { it to appWidgetManager.getAppWidgetInfo(it) }

        withInfo.filter { it.second == null }
                .forEach { delete(it.first) }

        return withInfo
                .filter { it.second != null }
                .map { it.first }.toSet()
    }

    private fun getAllAppWidgetIds(): Set<Int> = getSavedPreferences()
            .all.keys
            .map(Integer::valueOf)
            .toSet()

    fun getConfigurationFor(widgetId: Int): WidgetConfiguration? {
        val sharedPreferences = getSavedPreferences()
        val value = sharedPreferences.getString(widgetId.toString(), null)
        if (value == null) {
            appWidgetSchedulingService.cancelUpdating(widgetId)
        }
        return value?.let { WidgetConfiguration.fromSaveString(it) }
    }

    fun save(widgetConfiguration: WidgetConfiguration) {
        val value = widgetConfiguration.toSaveString()
        getSavedPreferences().edit()
                .putString(widgetConfiguration.widgetId.toString(), value)
                .apply()
    }

    private fun getSavedPreferences(): SharedPreferences =
            sharedPreferencesService.getPreferences(SAVE_PREFERENCE_NAME)

    private val applicationContext: Context get() = application.applicationContext

    private val appWidgetManager: AppWidgetManager get() = AppWidgetManager.getInstance(applicationContext)

    companion object {
        const val SAVE_PREFERENCE_NAME = "li.klass.fhem.appwidget.AppWidgetDataHolder"
        private val LOG = LoggerFactory.getLogger(AppWidgetInstanceManager::class.java)!!
    }
}