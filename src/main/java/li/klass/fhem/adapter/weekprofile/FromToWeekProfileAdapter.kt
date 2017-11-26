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

package li.klass.fhem.adapter.weekprofile

import android.app.TimePickerDialog
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import li.klass.fhem.R
import li.klass.fhem.domain.heating.schedule.DayProfile
import li.klass.fhem.domain.heating.schedule.configuration.HeatingIntervalConfiguration
import li.klass.fhem.domain.heating.schedule.interval.FromToHeatingInterval

class FromToWeekProfileAdapter(context: Context) : BaseWeekProfileAdapter<FromToHeatingInterval>(context) {


    protected interface OnTimeChangedListener {
        fun onTimeChanged(newTime: String)
    }

    override val numberOfAdditionalChildrenForParent: Int
        get() = 0

    override fun getChildView(parent: DayProfile<FromToHeatingInterval, HeatingIntervalConfiguration<FromToHeatingInterval>>, parentPosition: Int,
                              child: FromToHeatingInterval, view: View?, viewGroup: ViewGroup, relativeChildPosition: Int): View {
        val myView = layoutInflater.inflate(R.layout.weekprofile_from_to_item, viewGroup, false)

        setDetailTextView(myView, R.id.from, child.changedFromTime, child.fromTime, child.isNew)
        setDetailTextView(myView, R.id.to, child.changedToTime, child.toTime, child.isNew)

        setChangeTimeButton(myView, R.id.fromSet, child.changedFromTime, object : OnTimeChangedListener {
            override fun onTimeChanged(newTime: String) {
                var myNewTime = newTime
                myNewTime = weekProfile!!.formatTimeForCommand(myNewTime)
                child.changedFromTime = myNewTime
                notifyWeekProfileChangedListener()
            }
        })

        setChangeTimeButton(myView, R.id.toSet, child.changedToTime, object : OnTimeChangedListener {
            override fun onTimeChanged(newTime: String) {
                var myTime = newTime
                myTime = weekProfile!!.formatTimeForCommand(myTime)
                child.changedToTime = myTime
                notifyWeekProfileChangedListener()
            }
        })

        val position = relativeChildPosition + 1

        val fromText = myView.findViewById<TextView>(R.id.fromText)
        fromText.text = context.getString(R.string.fromTimetable, position)

        val toText = myView.findViewById<TextView>(R.id.toText)
        toText.text = context.getString(R.string.toTimetable, position)

        return myView
    }

    private fun setChangeTimeButton(view: View, buttonId: Int, currentTime: String, listener: OnTimeChangedListener) {

        val setTimeButton = view.findViewById<Button>(buttonId)
        setTimeButton.setOnClickListener {
            var hours = Integer.valueOf(currentTime.substring(0, 2))!!
            if (hours == 24) hours = 0
            val minutes = Integer.valueOf(currentTime.substring(3, 5))!!

            val timePickerDialog = TimePickerDialog(context, TimePickerDialog.OnTimeSetListener { _, hourOfDay, minuteOfDay ->
                val time = timeToTimeString(hourOfDay, minuteOfDay)
                listener.onTimeChanged(time)
            }, hours, minutes, true)

            timePickerDialog.show()
        }
    }
}
