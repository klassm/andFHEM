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
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import li.klass.fhem.domain.core.FhemDevice

abstract class DeviceListAppWidgetView<T> : DeviceAppWidgetView() {

    private inner class ListRemoteViewsFactory internal constructor(private val context: Context, private val items: List<T>, private val widgetId: Int) : RemoteViewsService.RemoteViewsFactory {

        override fun onCreate() {}

        override fun onDataSetChanged() {}

        override fun onDestroy() {}

        override fun getCount(): Int = items.size

        override fun getViewAt(i: Int): RemoteViews = getRemoteViewAt(context, items[i], widgetId)

        override fun getLoadingView(): RemoteViews? = null

        override fun getViewTypeCount(): Int = 1

        override fun getItemId(i: Int): Long = i.toLong()

        override fun hasStableIds(): Boolean = true
    }

    fun getRemoteViewsFactory(context: Context,
                              device: FhemDevice,
                              widgetId: Int): RemoteViewsService.RemoteViewsFactory =
            ListRemoteViewsFactory(context, extractItemsFrom(device), widgetId)

    abstract fun extractItemsFrom(device: FhemDevice): List<T>

    internal abstract fun getRemoteViewAt(context: Context, item: T, widgetId: Int): RemoteViews
}
