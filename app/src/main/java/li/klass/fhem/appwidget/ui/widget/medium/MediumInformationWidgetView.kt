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
import android.widget.RemoteViews

import li.klass.fhem.R
import li.klass.fhem.appwidget.annotation.WidgetMediumLine1
import li.klass.fhem.appwidget.annotation.WidgetMediumLine2
import li.klass.fhem.appwidget.annotation.WidgetMediumLine3
import li.klass.fhem.appwidget.ui.widget.WidgetSize
import li.klass.fhem.appwidget.ui.widget.WidgetType
import li.klass.fhem.appwidget.ui.widget.base.DeviceAppWidgetView
import li.klass.fhem.appwidget.update.WidgetConfiguration
import li.klass.fhem.domain.core.FhemDevice
import javax.inject.Inject

class MediumInformationWidgetView @Inject constructor() : DeviceAppWidgetView() {
    override fun getWidgetName(): Int = R.string.widget_information

    override fun getContentView(): Int = R.layout.appwidget_information_medium

    override fun fillWidgetView(context: Context, view: RemoteViews, device: FhemDevice, widgetConfiguration: WidgetConfiguration) {
        val line1 = valueForMarker(device, WidgetMediumLine1::class.java)
        val line2 = valueForMarker(device, WidgetMediumLine2::class.java)
        val line3 = valueForMarker(device, WidgetMediumLine3::class.java)

        setTextViewOrHide(view, R.id.line1, line1)
        setTextViewOrHide(view, R.id.line2, line2)
        setTextViewOrHide(view, R.id.line3, line3)

        openDeviceDetailPageWhenClicking(R.id.main, view, device, widgetConfiguration, context)
    }

    override val widgetType = WidgetType.INFORMATION
    override val widgetSize = WidgetSize.MEDIUM
}
