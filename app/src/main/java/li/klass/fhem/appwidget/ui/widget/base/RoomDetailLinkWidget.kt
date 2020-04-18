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

package li.klass.fhem.appwidget.ui.widget.base

import android.content.Context
import android.content.Intent
import android.os.SystemClock
import android.widget.RemoteViews
import androidx.navigation.NavDeepLinkBuilder
import li.klass.fhem.R
import li.klass.fhem.activities.AndFHEMMainActivity
import li.klass.fhem.appwidget.ui.widget.WidgetConfigurationCreatedCallback
import li.klass.fhem.appwidget.update.WidgetConfiguration
import li.klass.fhem.constants.BundleExtraKeys
import li.klass.fhem.room.detail.ui.RoomDetailFragmentArgs
import li.klass.fhem.ui.FragmentType

abstract class RoomDetailLinkWidget : RoomAppWidgetView() {
    override fun createWidgetConfiguration(context: Context, appWidgetId: Int, callback: WidgetConfigurationCreatedCallback, vararg payload: String) {
        callback.widgetConfigurationCreated(WidgetConfiguration(appWidgetId, widgetType, null, payload.toList()))
    }

    override fun getWidgetName(): Int = R.string.widget_room_detail

    override fun fillWidgetView(context: Context, view: RemoteViews, widgetConfiguration: WidgetConfiguration) {
        val roomName = widgetConfiguration.payload[0]

        view.setTextViewText(R.id.roomName, roomName)

        val openIntent = Intent(context, AndFHEMMainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(BundleExtraKeys.FRAGMENT, FragmentType.ROOM_DETAIL)
            putExtra(BundleExtraKeys.ROOM_NAME, roomName)
            putExtra("unique", "foobar://" + SystemClock.elapsedRealtime())
        }

        view.setOnClickPendingIntent(R.id.layout, NavDeepLinkBuilder(context)
                .setComponentName(AndFHEMMainActivity::class.java)
                .setGraph(R.navigation.nav_graph)
                .setDestination(R.id.roomDetailFragment)
                .setArguments(RoomDetailFragmentArgs(roomName).toBundle())
                .createPendingIntent()
        )
    }
}
