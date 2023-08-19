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

import android.app.AlertDialog
import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import li.klass.fhem.R
import li.klass.fhem.adapter.devices.genericui.TemperatureChangeTableRow
import li.klass.fhem.databinding.WeekprofileTemperatureTimeSelectorBinding
import li.klass.fhem.domain.heating.schedule.DayProfile
import li.klass.fhem.domain.heating.schedule.configuration.HeatingIntervalConfiguration
import li.klass.fhem.domain.heating.schedule.interval.FilledTemperatureInterval
import li.klass.fhem.util.ApplicationProperties
import li.klass.fhem.util.DialogUtil
import li.klass.fhem.util.ValueDescriptionUtil.appendTemperature
import org.slf4j.LoggerFactory

class IntervalWeekProfileAdapter(
    context: Context,
    private val applicationProperties: ApplicationProperties
) : BaseWeekProfileAdapter<FilledTemperatureInterval>(context) {

    override val numberOfAdditionalChildrenForParent: Int
        get() = 1

    override fun getChildView(
        parent: DayProfile<FilledTemperatureInterval, HeatingIntervalConfiguration<FilledTemperatureInterval>>,
        parentPosition: Int,
        child: FilledTemperatureInterval?,
        v: View?,
        viewGroup: ViewGroup,
        relativeChildPosition: Int
    ): View {

        if (child == null) {
            return addView(parent, viewGroup)
        }

        val view = layoutInflater.inflate(R.layout.weekprofile_interval_item, viewGroup, false)!!

        val isNew = child.isNew

        val intervalStringId = weekProfile!!.intervalType.stringId
        val intervalTypeTextView = view.findViewById<TextView>(R.id.intervalType)
        intervalTypeTextView.setText(intervalStringId)

        setDetailTextView(view, R.id.time, child.changedSwitchTime, child.switchTime, isNew)
        setDetailTextView(
            view, R.id.temperature, appendTemperature(child.changedTemperature),
            appendTemperature(child.temperature), isNew
        )

        setTemperatureAndInterval(
            view,
            R.id.set,
            child,
            viewGroup,
            object : OnIntervalTemperatureChangedListener {
                override fun onIntervalTemperatureChanged(time: String, temperature: Double) {
                    LOGGER.info(
                        "onIntervalTemperatureChanged(time={}, temperature={})",
                        time,
                        temperature
                    )
                    child.changedTemperature = temperature

                    if (!child.isTimeFixed) {
                        child.changedSwitchTime = time
                    } else {
                        LOGGER.info("onIntervalTemperatureChanged() - cannot change switch time, time is fixed!")
                    }
                    notifyWeekProfileChangedListener()
                }
            })

        val deleteButton = view.findViewById<Button>(R.id.delete)
        deleteButton.setOnClickListener {
            DialogUtil.showConfirmBox(
                context,
                R.string.areYouSure,
                R.string.deleteConfirmIntervalText
            ) {
                parent.deleteHeatingIntervalAt(relativeChildPosition)
                notifyWeekProfileChangedListener()
            }
        }

        if (child.isTimeFixed) {
            deleteButton.visibility = View.INVISIBLE
        }

        return view
    }

    private fun addView(
        parent: DayProfile<FilledTemperatureInterval, *>,
        viewGroup: ViewGroup
    ): View {
        val view = layoutInflater.inflate(R.layout.weekprofile_interval_add, viewGroup, false)

        val interval = FilledTemperatureInterval()

        setTemperatureAndInterval(
            view,
            R.id.addInterval,
            interval,
            viewGroup,
            object : OnIntervalTemperatureChangedListener {
                override fun onIntervalTemperatureChanged(time: String, temperature: Double) {
                    interval.changedSwitchTime = time
                    interval.changedTemperature = temperature
                    interval.isNew = true

                    parent.addHeatingInterval(interval)
                    notifyWeekProfileChangedListener()
                }
            })

        return view
    }

    private fun setTemperatureAndInterval(
        view: View, buttonId: Int, interval: FilledTemperatureInterval,
        viewGroup: ViewGroup, listener: OnIntervalTemperatureChangedListener
    ) {

        val button = view.findViewById<Button>(buttonId)
        button.setOnClickListener {
            IntervalEditHolder(interval, listener).showDialog(
                context,
                viewGroup
            )
        }
    }

    private inner class IntervalEditHolder constructor(
        var interval: FilledTemperatureInterval,
        private val listener: OnIntervalTemperatureChangedListener?
    ) {

        private var hours: Int = 0
        private var minutes: Int = 0

        init {

            val time = interval.changedSwitchTime

            val hoursPart = time?.substring(0, 2) ?: "0"
            val minutesPart = time?.substring(3, 5) ?: "0"

            hours = if (hoursPart == "24") 0 else Integer.valueOf(hoursPart)
            minutes = Integer.valueOf(minutesPart)
        }

        fun showDialog(context: Context, viewGroup: ViewGroup) {
            val builder = AlertDialog.Builder(context)
            val layoutInflater = LayoutInflater.from(context)

            val viewBinding = createContentViewWith(layoutInflater, viewGroup)

            val tableLayout = viewBinding.tableLayout
            val updateRow = viewBinding.updateRow
            val temperatureChangeTableRow = object : TemperatureChangeTableRow(
                context, interval.changedTemperature,
                updateRow, 5.5, 30.0, applicationProperties
            ) {
                override fun showButton(): Boolean = false
            }
            tableLayout.addView(temperatureChangeTableRow.createRow(layoutInflater, null))

            val dialog = builder.setView(viewBinding.root).show()
            viewBinding.okButton
                .setOnClickListener {
                    dialog.dismiss()
                    val temperature = temperatureChangeTableRow.temperature

                    val time = timeToTimeString(hours, minutes)

                    if (listener != null) {
                        LOGGER.debug("showDialog() - notifying listener")
                        listener.onIntervalTemperatureChanged(time, temperature)
                    } else {
                        LOGGER.error("showDialog() - no listener")
                    }
                    notifyWeekProfileChangedListener()
                }
        }

        private fun createContentViewWith(
            layoutInflater: LayoutInflater,
            viewGroup: ViewGroup
        ): WeekprofileTemperatureTimeSelectorBinding {
            val viewBinding =
                WeekprofileTemperatureTimeSelectorBinding.inflate(layoutInflater, viewGroup, false)
            val timePicker = viewBinding.timePicker
            timePicker.setIs24HourView(true)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                timePicker.hour = hours
                timePicker.minute = minutes
            } else {
                @Suppress("DEPRECATION")
                timePicker.currentHour = hours
                @Suppress("DEPRECATION")
                timePicker.currentMinute = minutes
            }

            timePicker.setOnTimeChangedListener { _, hourOfDay, minute ->
                hours = hourOfDay
                minutes = minute
            }

            timePicker.isEnabled = !interval.isTimeFixed

            return viewBinding
        }
    }

    private interface OnIntervalTemperatureChangedListener {
        fun onIntervalTemperatureChanged(time: String, temperature: Double)
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(IntervalWeekProfileAdapter::class.java)
    }
}
