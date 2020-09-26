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

package li.klass.fhem.appwidget.ui.widget.medium

import android.content.Context
import android.view.View
import android.widget.RemoteViews

import li.klass.fhem.R
import li.klass.fhem.appwidget.annotation.WidgetTemperatureAdditionalField
import li.klass.fhem.appwidget.annotation.WidgetTemperatureField
import li.klass.fhem.appwidget.ui.widget.WidgetSize
import li.klass.fhem.appwidget.ui.widget.WidgetType
import li.klass.fhem.appwidget.ui.widget.base.DeviceAppWidgetView
import li.klass.fhem.appwidget.update.WidgetConfiguration
import li.klass.fhem.domain.core.FhemDevice
import li.klass.fhem.util.toHtml
import javax.inject.Inject

class TemperatureWidgetView @Inject constructor() : DeviceAppWidgetView() {

    override val widgetSize: WidgetSize = WidgetSize.MEDIUM

    override fun getWidgetName() = R.string.widget_temperature

    public override fun getContentView() = R.layout.appwidget_temperature

    public override fun fillWidgetView(context: Context, view: RemoteViews, device: FhemDevice?, widgetConfiguration: WidgetConfiguration) {
        if (device == null) {
            view.setViewVisibility(R.id.additional, View.GONE)
            view.setViewVisibility(R.id.temperature, View.GONE)
            return
        }
        val temperature = valueForMarker(device, WidgetTemperatureField::class.java)
        val additionalFieldValue = valueForMarker(device, WidgetTemperatureAdditionalField::class.java)
        setTextViewOrHide(view, R.id.additional, additionalFieldValue)

        view.setTextViewText(R.id.temperature, temperature?.toHtml())

        openDeviceDetailPageWhenClicking(R.id.main, view, device, widgetConfiguration, context)
    }

    override val widgetType = WidgetType.TEMPERATURE

}
