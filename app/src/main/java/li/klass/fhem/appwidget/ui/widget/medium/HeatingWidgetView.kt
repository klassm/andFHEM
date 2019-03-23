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
import li.klass.fhem.appwidget.ui.widget.WidgetSize
import li.klass.fhem.appwidget.ui.widget.WidgetType
import li.klass.fhem.appwidget.ui.widget.base.DeviceAppWidgetView
import li.klass.fhem.appwidget.update.WidgetConfiguration
import li.klass.fhem.domain.core.FhemDevice
import java.util.*
import javax.inject.Inject

class HeatingWidgetView @Inject constructor() : DeviceAppWidgetView() {

    override fun getWidgetName(): Int = R.string.widget_heating

    override fun getContentView(): Int = R.layout.appwidget_heating

    override fun fillWidgetView(context: Context, view: RemoteViews, device: FhemDevice, widgetConfiguration: WidgetConfiguration) {
        val xmlListDevice = device.xmlListDevice

        val warnings = xmlListDevice.getState("warnings", false)
        val temperature = xmlListDevice.getFirstStateOf(TEMPERATURE_STATES)
        val desiredTemp = xmlListDevice.getFirstStateOf(DESIRED_TEMPERATURE_STATES)

        val boost = xmlListDevice.getFirstStateOf(BOOST_STATES)
        val boostText = if (boost != null && boost != "0") context.getString(R.string.boost) else null

        if (warnings != null && warnings.toLowerCase(Locale.getDefault()).contains("open")) {
            view.setViewVisibility(R.id.windowOpen, View.VISIBLE)
        } else {
            view.setViewVisibility(R.id.windowOpen, View.GONE)
        }

        val target = context.getString(R.string.target)
        setTextViewOrHide(view, R.id.temperature, "$target: $desiredTemp")

        val additionalValues = listOf(temperature, boostText)
                .filterNotNull()
                .joinToString(separator = ", ")
        if (!additionalValues.isBlank()) {
            setTextViewOrHide(view, R.id.additional, additionalValues)
        } else {
            view.setViewVisibility(R.id.additional, View.GONE)
        }

        openDeviceDetailPageWhenClicking(R.id.main, view, device, widgetConfiguration, context)
    }

    override fun supports(device: FhemDevice): Boolean {
        val xmlListDevice = device.xmlListDevice
        return xmlListDevice.containsAnyOfStates(DESIRED_TEMPERATURE_STATES)
    }

    override val widgetSize = WidgetSize.MEDIUM
    override val widgetType = WidgetType.HEATING

    companion object {
        private val TEMPERATURE_STATES = listOf("temperature", "measured-temp")
        private val BOOST_STATES = listOf("boost")
        private val DESIRED_TEMPERATURE_STATES = listOf("desired-temp", "desiredTemperature")
    }
}
