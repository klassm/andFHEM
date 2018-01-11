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
import android.view.View
import android.widget.RemoteViews
import li.klass.fhem.R
import li.klass.fhem.appwidget.ui.widget.base.DeviceAppWidgetView
import li.klass.fhem.appwidget.update.WidgetConfiguration
import li.klass.fhem.dagger.ApplicationComponent
import li.klass.fhem.domain.core.FhemDevice

class SmallPresenceWidget : DeviceAppWidgetView() {
    override fun fillWidgetView(context: Context, view: RemoteViews, device: FhemDevice, widgetConfiguration: WidgetConfiguration) {
        view.setTextViewText(R.id.present, device.widgetName)
        view.setTextViewText(R.id.absent, device.widgetName)

        if (device.state == "present") {
            view.setViewVisibility(R.id.present, View.VISIBLE)
            view.setViewVisibility(R.id.absent, View.GONE)
        } else {
            view.setViewVisibility(R.id.present, View.GONE)
            view.setViewVisibility(R.id.absent, View.VISIBLE)
        }
    }

    override fun getWidgetName(): Int = R.string.widget_presence

    override fun getContentView(): Int = R.layout.appwidget_presence_small

    override fun shouldSetDeviceName(): Boolean = false

    override fun inject(applicationComponent: ApplicationComponent) {
        applicationComponent.inject(this)
    }
}
