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

package li.klass.fhem.adapter.devices.genericui.onoff

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.TableRow
import android.widget.TextView
import com.google.api.client.repackaged.com.google.common.base.Objects
import li.klass.fhem.R
import li.klass.fhem.domain.core.FhemDevice
import li.klass.fhem.widget.CheckableButton

abstract class AbstractOnOffActionRow(protected val layoutId: Int,
                                      protected val description: Int?,
                                      protected var connectionId: String?) {

    open fun createRow(device: FhemDevice, context: Context): TableRow {
        val inflater = LayoutInflater.from(context)
        val tableRow = inflater.inflate(layoutId, null) as TableRow
        val descriptionView = tableRow.findViewById<TextView>(R.id.description)
        val onButton = findOnButton(tableRow) as CheckableButton
        val offButton = findOffButton(tableRow) as CheckableButton

        val text = description?.let { context.getString(description) } ?: device.aliasOrName
        descriptionView.text = text

        val onStateName = getOnStateName(device, context)
        onButton.setOnClickListener(createListener(context, device, onStateName))
        onButton.text = getOnStateText(device, context)

        val offStateName = getOffStateName(device, context)
        offButton.setOnClickListener(createListener(context, device, offStateName))
        offButton.text = getOffStateText(device, context)

        val on = isOn(device, context)
        onButton.isChecked = on
        offButton.isChecked = !on
        if (on) {
            onButton.setBackgroundDrawable(context.resources.getDrawable(R.drawable.theme_toggle_on_normal))
            offButton.setBackgroundDrawable(context.resources.getDrawable(R.drawable.theme_toggle_default_normal))
        } else {
            onButton.setBackgroundDrawable(context.resources.getDrawable(R.drawable.theme_toggle_default_normal))
            offButton.setBackgroundDrawable(context.resources.getDrawable(R.drawable.theme_toggle_off_normal))
        }

        return tableRow
    }

    protected fun findOffButton(tableRow: TableRow): Button =
            tableRow.findViewById<View>(R.id.offButton) as Button

    protected fun findOnButton(tableRow: TableRow): Button =
            tableRow.findViewById<View>(R.id.onButton) as Button

    protected open fun getOnStateName(device: FhemDevice, context: Context): String {
        val state = device.setList.getFirstPresentStateOf("on", "ON")
        return Objects.firstNonNull(state, "on")
    }

    protected open fun getOffStateName(device: FhemDevice, context: Context): String {
        val state = device.setList.getFirstPresentStateOf("off", "OFF")
        return Objects.firstNonNull(state, "off")
    }

    protected open fun getOnStateText(device: FhemDevice, context: Context): String {
        val eventMap = device.eventMap

        var onStateName: String? = getOnStateName(device, context)
        if (onStateName == null) onStateName = "on"
        return eventMap.getValueOr(onStateName, "on")
    }

    protected open fun getOffStateText(device: FhemDevice, context: Context): String {
        val eventMap = device.eventMap

        var offStateName: String? = getOffStateName(device, context)
        if (offStateName == null) offStateName = "off"
        return eventMap.getValueOr(offStateName, "off")
    }

    protected open fun isOn(device: FhemDevice, context: Context): Boolean = false

    private fun createListener(context: Context, device: FhemDevice, targetState: String): View.OnClickListener =
            View.OnClickListener { onButtonClick(context, device, connectionId, targetState) }

    abstract fun onButtonClick(context: Context, device: FhemDevice, connectionId: String?, targetState: String)

    companion object {
        const val HOLDER_KEY = "OnOffActionRow"
        const val LAYOUT_DETAIL = R.layout.device_detail_onoffbuttonrow
        const val LAYOUT_OVERVIEW = R.layout.device_overview_onoffbuttonrow
    }
}
