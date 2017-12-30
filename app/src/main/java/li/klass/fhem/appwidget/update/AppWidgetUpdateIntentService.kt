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

import android.app.IntentService
import android.content.Intent
import li.klass.fhem.AndFHEMApplication
import li.klass.fhem.constants.Actions.REDRAW_ALL_WIDGETS
import li.klass.fhem.constants.Actions.REDRAW_WIDGET
import li.klass.fhem.constants.BundleExtraKeys.ALLOW_REMOTE_UPDATES
import li.klass.fhem.constants.BundleExtraKeys.APP_WIDGET_ID
import li.klass.fhem.update.backend.DeviceListUpdateService
import org.slf4j.LoggerFactory
import javax.inject.Inject


class AppWidgetUpdateIntentService : IntentService(AppWidgetUpdateIntentService::class.java.name) {

    @Inject
    lateinit var appWidgetUpdateService: AppWidgetUpdateService
    @Inject
    lateinit var deviceListUpdateService: DeviceListUpdateService

    override fun onCreate() {
        super.onCreate()
        (application as AndFHEMApplication).daggerComponent.inject(this)
    }

    override fun onHandleIntent(intent: Intent?) {
        val action = intent!!.action
        val allowRemoteUpdates = intent.getBooleanExtra(ALLOW_REMOTE_UPDATES, false)
        when {
            REDRAW_WIDGET == action -> handleRedrawWidget(intent, allowRemoteUpdates)
            REDRAW_ALL_WIDGETS == action -> {
                LOG.info("onHandleIntent() - updating all widgets (received REDRAW_ALL_WIDGETS)")
                appWidgetUpdateService.updateAllWidgets()
            }
        }
    }

    private fun handleRedrawWidget(intent: Intent, allowRemoteUpdates: Boolean) {
        if (!intent.hasExtra(APP_WIDGET_ID)) {
            return
        }

        val widgetId = intent.getIntExtra(APP_WIDGET_ID, -1)
        LOG.debug("handleRedrawWidget() - updating widget-id {}, remote update is {}", widgetId, allowRemoteUpdates)

        appWidgetUpdateService.doRemoteUpdate(widgetId, {
            appWidgetUpdateService.updateWidget(widgetId)
        })
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(AppWidgetUpdateIntentService::class.java)!!
    }
}
