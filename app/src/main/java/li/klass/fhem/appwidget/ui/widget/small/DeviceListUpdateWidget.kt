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

package li.klass.fhem.appwidget.ui.widget.small

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import android.widget.RemoteViews
import li.klass.fhem.R
import li.klass.fhem.appwidget.action.AppWidgetActionBroadcastReceiver
import li.klass.fhem.appwidget.ui.widget.WidgetConfigurationCreatedCallback
import li.klass.fhem.appwidget.ui.widget.WidgetSize
import li.klass.fhem.appwidget.ui.widget.WidgetType
import li.klass.fhem.appwidget.ui.widget.base.OtherAppWidgetView
import li.klass.fhem.appwidget.update.WidgetConfiguration
import li.klass.fhem.constants.Actions.WIDGET_REQUEST_UPDATE
import li.klass.fhem.constants.BundleExtraKeys
import javax.inject.Inject

class DeviceListUpdateWidget @Inject constructor() : OtherAppWidgetView() {
    override fun createWidgetConfiguration(context: Context, appWidgetId: Int, connectionId: String, callback: WidgetConfigurationCreatedCallback, vararg payload: String) {
        callback.widgetConfigurationCreated(WidgetConfiguration(appWidgetId, widgetType, connectionId, payload.toList()))
    }

    override fun getWidgetName(): Int = R.string.widget_device_list_update

    override fun getContentView(): Int = R.layout.appwidget_icon_small

    override fun fillWidgetView(context: Context, view: RemoteViews, widgetConfiguration: WidgetConfiguration) {
        view.setImageViewResource(R.id.icon, R.drawable.launcher_refresh)

        val updateIntent = Intent(context, AppWidgetActionBroadcastReceiver::class.java)
                .setAction(WIDGET_REQUEST_UPDATE)
                .putExtra(BundleExtraKeys.CONNECTION_ID, widgetConfiguration.connectionId)
                .putExtra("unique", "foobar://" + SystemClock.elapsedRealtime())

        view.setOnClickPendingIntent(
            R.id.layout, PendingIntent.getBroadcast(
                context,
                widgetConfiguration.widgetId, updateIntent,
                PendingIntent.FLAG_IMMUTABLE
            )
        )
    }

    override val widgetSize = WidgetSize.SMALL

    override val widgetType = WidgetType.UPDATE_WIDGET
}