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

package li.klass.fhem.adapter.devices.core.generic.detail.actions.state

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.DatePicker
import android.widget.TableLayout
import android.widget.TableRow
import com.google.common.collect.Lists.newArrayList
import li.klass.fhem.R
import li.klass.fhem.adapter.devices.core.UpdatingResultReceiver
import li.klass.fhem.adapter.devices.core.generic.detail.actions.devices.FHTDetailActionProvider
import li.klass.fhem.adapter.devices.core.generic.detail.actions.devices.fht.HolidayShort
import li.klass.fhem.adapter.devices.genericui.SpinnerActionRow
import li.klass.fhem.adapter.devices.genericui.TemperatureChangeTableRow
import li.klass.fhem.constants.Actions
import li.klass.fhem.constants.BundleExtraKeys
import li.klass.fhem.domain.fht.FHTMode
import li.klass.fhem.service.intent.DeviceIntentService
import li.klass.fhem.ui.AndroidBug.showMessageIfColorStateBugIsEncountered
import li.klass.fhem.update.backend.xmllist.XmlListDevice
import li.klass.fhem.util.ApplicationProperties
import li.klass.fhem.util.DatePickerUtil
import li.klass.fhem.util.EnumUtils
import li.klass.fhem.util.EnumUtils.toStringList
import li.klass.fhem.util.StateToSet
import java.util.*
import javax.inject.Inject

class FHTModeStateOverwrite @Inject constructor(
        private val applicationProperties: ApplicationProperties,
        private val holidayShort: HolidayShort
) : StateAttributeAction {

    override fun createRow(device: XmlListDevice, connectionId: String?, key: String, stateValue: String, context: Context, parent: ViewGroup): TableRow {
        val selected = EnumUtils.positionOf(FHTMode.values(), modeFor(stateValue))
        return object : SpinnerActionRow(context, null, context.getString(R.string.setMode), toStringList(FHTMode.values()), selected) {

            override fun onItemSelected(context: Context, device: XmlListDevice, connectionId: String?, item: String) {
                val mode = modeFor(item)
                setMode(device, mode, this, parent, context, LayoutInflater.from(context))
            }
        }.createRow(device, connectionId, parent)
    }

    override fun supports(xmlListDevice: XmlListDevice): Boolean = true

    private fun modeFor(stateValue: String): FHTMode = try {
        FHTMode.valueOf(stateValue.toUpperCase(Locale.getDefault()))
    } catch (e: IllegalArgumentException) {
        FHTMode.UNKNOWN
    }

    private fun setMode(device: XmlListDevice, mode: FHTMode, spinnerActionRow: SpinnerActionRow, viewGroup: ViewGroup, context: Context, inflater: LayoutInflater) {
        val intent = Intent(Actions.DEVICE_SET_SUB_STATES)
                .setClass(context, DeviceIntentService::class.java)
                .putExtra(BundleExtraKeys.DEVICE_NAME, device.name)
                .putExtra(BundleExtraKeys.STATES, newArrayList(StateToSet("mode", mode.name.toLowerCase(Locale.getDefault()))))
                .putExtra(BundleExtraKeys.RESULT_RECEIVER, UpdatingResultReceiver(context))

        when (mode) {
            FHTMode.UNKNOWN -> {
            }
            FHTMode.HOLIDAY -> handleHolidayMode(device, spinnerActionRow, intent, context, viewGroup, inflater)
            FHTMode.HOLIDAY_SHORT -> handleHolidayShortMode(device, spinnerActionRow, intent, viewGroup, context, inflater)
            else -> {
                context.startService(intent)
                spinnerActionRow.commitSelection()
            }
        }
    }

    private fun handleHolidayMode(device: XmlListDevice, spinnerActionRow: SpinnerActionRow,
                                  intent: Intent, context: Context, parent: ViewGroup, inflater: LayoutInflater) {

        showMessageIfColorStateBugIsEncountered(context) {
            val dialogBuilder = AlertDialog.Builder(context)

            val contentView = inflater.inflate(R.layout.fht_holiday_dialog, parent, false) as TableLayout

            val datePicker = contentView.findViewById(R.id.datePicker) as DatePicker
            DatePickerUtil.hideYearField(datePicker)

            val temperatureUpdateRow = contentView.findViewById(R.id.updateRow) as TableRow

            val temperatureChangeTableRow = TemperatureChangeTableRow(context, FHTDetailActionProvider.MINIMUM_TEMPERATURE, temperatureUpdateRow,
                    FHTDetailActionProvider.MINIMUM_TEMPERATURE, FHTDetailActionProvider.MAXIMUM_TEMPERATURE, applicationProperties)
            contentView.addView(temperatureChangeTableRow.createRow(inflater, device))

            dialogBuilder.setView(contentView)

            dialogBuilder.setNegativeButton(R.string.cancelButton) { dialogInterface, _ ->
                spinnerActionRow.revertSelection()
                dialogInterface.dismiss()
            }

            dialogBuilder.setPositiveButton(R.string.okButton) { dialogInterface, _ ->
                @Suppress("UNCHECKED_CAST")
                (intent.getSerializableExtra(BundleExtraKeys.STATES) as MutableList<StateToSet>).apply {
                    add(StateToSet("desired-temp", "" + temperatureChangeTableRow.temperature))
                    add(StateToSet("holiday1", "" + datePicker.dayOfMonth))
                    add(StateToSet("holiday2", "" + (datePicker.month + 1)))
                }
                context.startService(intent)

                spinnerActionRow.commitSelection()
                dialogInterface.dismiss()
            }

            dialogBuilder.show()
        }
    }

    private fun handleHolidayShortMode(device: XmlListDevice, spinnerActionRow: SpinnerActionRow, intent: Intent, parent: ViewGroup,
                                       context: Context, layoutInflater: LayoutInflater) {
        holidayShort.showDialog(context, parent, layoutInflater, spinnerActionRow, device, intent)
    }
}
