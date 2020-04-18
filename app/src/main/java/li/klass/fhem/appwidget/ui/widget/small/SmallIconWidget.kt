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

import android.content.Context
import android.widget.RemoteViews
import androidx.navigation.NavDeepLinkBuilder
import li.klass.fhem.R
import li.klass.fhem.activities.AndFHEMMainActivity
import li.klass.fhem.appwidget.ui.widget.WidgetConfigurationCreatedCallback
import li.klass.fhem.appwidget.ui.widget.base.OtherAppWidgetView
import li.klass.fhem.appwidget.update.WidgetConfiguration

abstract class SmallIconWidget : OtherAppWidgetView() {

    protected abstract val destination: Int
    protected abstract val iconResource: Int

    override fun createWidgetConfiguration(context: Context, appWidgetId: Int, callback: WidgetConfigurationCreatedCallback, vararg payload: String) {
        callback.widgetConfigurationCreated(WidgetConfiguration(appWidgetId, widgetType, null, payload.toList()))
    }

    override fun getContentView(): Int = R.layout.appwidget_icon_small

    override fun fillWidgetView(context: Context, view: RemoteViews, widgetConfiguration: WidgetConfiguration) {
        view.setImageViewResource(R.id.icon, iconResource)

        view.setOnClickPendingIntent(R.id.layout, NavDeepLinkBuilder(context)
                .setComponentName(AndFHEMMainActivity::class.java)
                .setGraph(R.navigation.nav_graph)
                .setDestination(destination)
                .createPendingIntent())
    }
}
