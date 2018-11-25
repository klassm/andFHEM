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

package li.klass.fhem.adapter.devices.core.generic.detail.actions.devices.fht

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import kotlinx.android.synthetic.main.fht_holiday_short_dialog.view.*
import kotlinx.android.synthetic.main.fht_holiday_short_dialog_android_bug.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import li.klass.fhem.R
import li.klass.fhem.adapter.devices.core.generic.detail.actions.devices.FHTDetailActionProvider
import li.klass.fhem.adapter.devices.genericui.SeekBarActionRowFullWidthAndButton
import li.klass.fhem.adapter.devices.genericui.SpinnerActionRow
import li.klass.fhem.devices.backend.GenericDeviceService
import li.klass.fhem.domain.fht.FHTMode
import li.klass.fhem.ui.AndroidBug
import li.klass.fhem.ui.AndroidBug.handleColorStateBug
import li.klass.fhem.update.backend.xmllist.XmlListDevice
import li.klass.fhem.util.*
import java.util.*
import javax.inject.Inject

class HolidayShort @Inject constructor(private val applicationProperties: ApplicationProperties,
                                       private val dateTimeProvider: DateTimeProvider,
                                       private val holidayShortCalculator: HolidayShortCalculator,
                                       private val genericDeviceService: GenericDeviceService) {

    fun showDialog(context: Context, parent: ViewGroup, layoutInflater: LayoutInflater,
                   spinnerActionRow: SpinnerActionRow, device: XmlListDevice, connectionId: String?) {
        val currentDesiredTemp = device.stateValueFor("desired-temp")
                ?.let { ValueExtractUtil.extractLeadingDouble(it) }
                ?: FHTDetailActionProvider.MINIMUM_TEMPERATURE
        var model = Model(hour = 0, minute = 0, desiredTemp = currentDesiredTemp)
        val contentView = createContentView(layoutInflater, parent, object : HolidayShort.OnTimeChanged {
            override fun timeChanged(newHour: Int, newMinute: Int, endTimeValue: TextView) {
                updateHolidayShortEndTime(endTimeValue, newHour, newMinute)
                model = model.copy(hour = newHour, minute = newMinute)
            }
        })

        val temperatureUpdateRow = contentView.findViewById<TableRow>(R.id.updateTemperatureRow)
        val temperatureChangeTableRow = object : SeekBarActionRowFullWidthAndButton(
                context,
                currentDesiredTemp,
                0.5,
                FHTDetailActionProvider.MINIMUM_TEMPERATURE,
                FHTDetailActionProvider.MAXIMUM_TEMPERATURE,
                temperatureUpdateRow,
                applicationProperties) {
            override fun onProgressChange(context: Context, device: XmlListDevice?, progress: Double) {
                model = model.copy(desiredTemp = progress)
            }
        }

        contentView.addView(temperatureChangeTableRow.createRow(layoutInflater, device))

        AlertDialog.Builder(context)
                .setNegativeButton(R.string.cancelButton) { dialogInterface, _ ->
                    spinnerActionRow.revertSelection()
                    dialogInterface.dismiss()
                }
                .setPositiveButton(R.string.okButton) { dialogInterface, _ ->
                    val switchDate = holidayShortCalculator.holiday1SwitchTimeFor(model.hour, model.minute)

                    GlobalScope.launch(Dispatchers.Main) {
                        async {
                            genericDeviceService.setSubStates(device, listOf(
                                    StateToSet("desired-temp", "" + model.desiredTemp),
                                    StateToSet("holiday1", "" + holidayShortCalculator.calculateHoliday1ValueFrom(switchDate.hourOfDay, switchDate.minuteOfHour)),
                                    StateToSet("holiday2", "" + switchDate.dayOfMonth),
                                    StateToSet("mode", FHTMode.HOLIDAY_SHORT.name.toLowerCase(Locale.getDefault()))
                            ), connectionId)
                        }.await()
                        spinnerActionRow.commitSelection()
                        dialogInterface.dismiss()
                    }
                }
                .setView(contentView)
                .show()
    }

    private fun createContentView(layoutInflater: LayoutInflater, parent: ViewGroup, onTimeChanged: OnTimeChanged): TableLayout =
            handleColorStateBug(object : AndroidBug.BugHandler {
                override fun bugEncountered(): View {
                    val contentView = layoutInflater.inflate(R.layout.fht_holiday_short_dialog_android_bug, parent, false) as TableLayout
                    contentView.timePickerAndroidBug.apply {
                        minutes = 0
                        hours = dateTimeProvider.now().hourOfDay
                        setOnValueChangedListener { hours, minutes ->
                            onTimeChanged.timeChanged(hours, minutes, contentView.findViewById(R.id.endTimeValue))
                        }
                    }

                    return contentView
                }

                override fun defaultAction(): View {
                    val contentView = layoutInflater.inflate(R.layout.fht_holiday_short_dialog, parent, false) as TableLayout
                    contentView.timePicker
                            .apply {
                                setIs24HourView(true)
                                currentMinute = 0
                                currentHour = dateTimeProvider.now().hourOfDay

                                setOnTimeChangedListener { _, hourOfDay, minute ->
                                    onTimeChanged.timeChanged(hourOfDay, minute, contentView.findViewById(R.id.endTimeValue))
                                }
                            }
                    return contentView
                }
            }) as TableLayout

    private fun updateHolidayShortEndTime(endTimeValue: TextView, hour: Int, minute: Int) {
        val switchDate = holidayShortCalculator.holiday1SwitchTimeFor(hour, minute)
        val switchDateString = DateFormatUtil.toReadable(switchDate)

        endTimeValue.text = switchDateString
    }

    private data class Model(val hour: Int, val minute: Int, val desiredTemp: Double)

    private interface OnTimeChanged {
        fun timeChanged(newHour: Int, newMinute: Int, endTimeValue: TextView)
    }
}
