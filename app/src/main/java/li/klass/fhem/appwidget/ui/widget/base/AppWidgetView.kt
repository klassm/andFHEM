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
import android.view.View
import android.widget.RemoteViews
import li.klass.fhem.AndFHEMApplication
import li.klass.fhem.appwidget.ui.widget.WidgetConfigurationCreatedCallback
import li.klass.fhem.appwidget.ui.widget.WidgetType
import li.klass.fhem.appwidget.update.WidgetConfiguration
import li.klass.fhem.dagger.ApplicationComponent
import org.slf4j.LoggerFactory

abstract class AppWidgetView {

    protected abstract fun getContentView(): Int
    abstract fun getWidgetName(): Int

    init {
        val application = AndFHEMApplication.application
        if (application != null) {
            @Suppress("LeakingThis")
            inject(application.daggerComponent)
        }
    }

    protected abstract fun inject(applicationComponent: ApplicationComponent)

    abstract fun createWidgetConfiguration(context: Context, widgetType: WidgetType, appWidgetId: Int,
                                           callback: WidgetConfigurationCreatedCallback, vararg payload: String)

    open fun createView(context: Context, widgetConfiguration: WidgetConfiguration): RemoteViews {
        logger.debug("creating widget view for configuration {}", widgetConfiguration)

        val views = RemoteViews(context.packageName, getContentView())

        fillWidgetView(context, views, widgetConfiguration)

        return views
    }

    protected abstract fun fillWidgetView(context: Context, view: RemoteViews,
                                          widgetConfiguration: WidgetConfiguration)

    protected fun setTextViewOrHide(view: RemoteViews, viewId: Int, value: String?) {
        if (value != null) {
            view.setTextViewText(viewId, value)
            view.setViewVisibility(viewId, View.VISIBLE)
        } else {
            view.setViewVisibility(viewId, View.GONE)
        }
    }

    companion object {
        val logger = LoggerFactory.getLogger(AppWidgetView::class.java)!!
    }
}
