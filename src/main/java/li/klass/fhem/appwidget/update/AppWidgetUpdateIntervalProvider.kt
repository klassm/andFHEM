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
import android.content.Context
import li.klass.fhem.update.backend.DeviceListService
import li.klass.fhem.util.ApplicationProperties
import li.klass.fhem.util.NetworkState
import org.slf4j.LoggerFactory
import javax.inject.Inject

class AppWidgetUpdateIntervalProvider @Inject constructor(
        private val application: Application,
        private val applicationProperties: ApplicationProperties
) {
    fun getConnectionDependentUpdateInterval(): Long {
        val updateInterval: Long

        if (!NetworkState.isConnected(applicationContext)) {
            LOG.debug("getConnectionDependentUpdateInterval - no network connection")
            updateInterval = DeviceListService.Companion.NEVER_UPDATE_PERIOD
        } else if (NetworkState.isConnectedMobile(applicationContext)) {
            LOG.debug("getConnectionDependentUpdateInterval - mobile connection")
            updateInterval = getWidgetUpdateIntervalFor(WIDGET_UPDATE_INTERVAL_PREFERENCES_KEY_MOBILE).toLong()
        } else {
            LOG.debug("getConnectionDependentUpdateInterval - wlan connection")
            updateInterval = getWidgetUpdateIntervalFor(WIDGET_UPDATE_INTERVAL_PREFERENCES_KEY_WLAN).toLong()
        }
        LOG.info("getConnectionDependentUpdateInterval - update interval is %d", updateInterval)

        return updateInterval
    }

    private fun getWidgetUpdateIntervalFor(key: String): Int {
        val value = applicationProperties.getStringSharedPreference(key, "3600")
        val intValue = Integer.parseInt(value)
        return intValue * 1000
    }

    private val applicationContext: Context get() = application.applicationContext


    companion object {
        private val WIDGET_UPDATE_INTERVAL_PREFERENCES_KEY_WLAN = "WIDGET_UPDATE_INTERVAL_WLAN"
        private val WIDGET_UPDATE_INTERVAL_PREFERENCES_KEY_MOBILE = "WIDGET_UPDATE_INTERVAL_MOBILE"
        val LOG = LoggerFactory.getLogger(AppWidgetUpdateIntervalProvider::class.java)
    }
}