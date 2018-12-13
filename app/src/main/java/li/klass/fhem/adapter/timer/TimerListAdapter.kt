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

package li.klass.fhem.adapter.timer

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.timer_list_item.view.*
import li.klass.fhem.R
import li.klass.fhem.adapter.ListDataAdapter
import li.klass.fhem.devices.backend.at.TimerDevice

class TimerListAdapter(context: Context, data: List<TimerDevice>) : ListDataAdapter<TimerDevice>(context, R.layout.timer_list_item, data) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val device = data[position]

        val view: LinearLayout
        if (convertView != null && convertView is LinearLayout) {
            view = convertView
        } else {
            view = inflater.inflate(resource, null) as LinearLayout
        }

        view.timerName.text = device.name

        view.timerNameAddition.text = if (device.isActive) "" else "(" + context.getString(R.string.deactivated) + ")"

        val definition = device.definition
        val formatString = context.getString(R.string.timer_overview)
        val repetition = context.getString(definition.repetition.stringId)
        val interval = context.getString(definition.type.text)
        val date = definition.switchTime.toString("HH:mm:ss")
        val targetDevice = definition.targetDeviceName
        val targetState = definition.targetState + (definition.targetStateAppendix?.let { " " + it } ?: "")

        view.timerContent.text = String.format(formatString, repetition, interval, date, targetDevice, targetState)

        view.timerNextTrigger.text = String.format(context.getString(R.string.timer_next_trigger), device.next)

        val color = if (device.isActive) android.R.color.transparent else R.color.inactiveBackground
        val colorResource = ContextCompat.getColor(context, color)
        view.setBackgroundColor(colorResource)

        view.tag = device

        return view
    }
}
