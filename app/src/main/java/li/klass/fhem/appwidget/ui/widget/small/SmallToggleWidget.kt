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
import li.klass.fhem.R
import li.klass.fhem.adapter.devices.hook.DeviceHookProvider
import li.klass.fhem.appwidget.ui.widget.WidgetSize
import li.klass.fhem.appwidget.ui.widget.WidgetType
import li.klass.fhem.appwidget.ui.widget.medium.ToggleWidgetView
import li.klass.fhem.appwidget.update.WidgetConfiguration
import li.klass.fhem.behavior.toggle.OnOffBehavior
import li.klass.fhem.domain.core.FhemDevice
import javax.inject.Inject

class SmallToggleWidget @Inject constructor(
        deviceHookProvider: DeviceHookProvider,
        onOffBehavior: OnOffBehavior
) : ToggleWidgetView(deviceHookProvider, onOffBehavior) {
    override fun fillWidgetView(context: Context, view: RemoteViews, device: FhemDevice?, widgetConfiguration: WidgetConfiguration) {
        super.fillWidgetView(context, view, device, widgetConfiguration)

        view.setTextViewText(R.id.toggleOff, device?.widgetName)
        view.setTextViewText(R.id.toggleOn, device?.widgetName)
    }

    override fun getContentView(): Int = R.layout.appwidget_toggle_small

    override fun shouldSetDeviceName(): Boolean = false

    override val widgetSize = WidgetSize.SMALL
    override val widgetType = WidgetType.TOGGLE_SMALL
}
