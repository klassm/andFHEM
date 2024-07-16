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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import li.klass.fhem.R
import li.klass.fhem.adapter.devices.core.generic.detail.actions.devices.FHTDetailActionProvider
import li.klass.fhem.adapter.devices.core.generic.detail.actions.devices.fht.HolidayShort
import li.klass.fhem.adapter.devices.genericui.SpinnerActionRow
import li.klass.fhem.adapter.devices.genericui.TemperatureChangeTableRow
import li.klass.fhem.constants.BundleExtraKeys
import li.klass.fhem.devices.backend.GenericDeviceService
import li.klass.fhem.domain.fht.FHTMode
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
    private val holidayShort: HolidayShort,
    private val genericDeviceService: GenericDeviceService
) : StateAttributeAction {

    override fun createRow(
        device: XmlListDevice,
        connectionId: String?,
        key: String,
        stateValue: String,
        context: Context,
        parent: ViewGroup
    ): TableRow {
        val selected = EnumUtils.positionOf(FHTMode.values(), modeFor(stateValue))
        return object : SpinnerActionRow(
            context,
            null,
            context.getString(R.string.setMode),
            toStringList(FHTMode.values()),
            selected
        ) {

            override fun onItemSelected(
                context: Context,
                device: XmlListDevice,
                connectionId: String?,
                item: String
            ) {
                val mode = modeFor(item)
                setMode(
                    device,
                    mode,
                    this,
                    parent,
                    context,
                    LayoutInflater.from(context),
                    connectionId
                )
            }
        }.createRow(device, connectionId, parent)
    }

    override fun supports(xmlListDevice: XmlListDevice): Boolean = true

    private fun modeFor(stateValue: String): FHTMode = try {
        FHTMode.valueOf(stateValue.uppercase(Locale.getDefault()))
    } catch (e: IllegalArgumentException) {
        FHTMode.UNKNOWN
    }

    private fun setMode(
        device: XmlListDevice, mode: FHTMode, spinnerActionRow: SpinnerActionRow,
        viewGroup: ViewGroup, context: Context, inflater: LayoutInflater, connectionId: String?
    ) {
        when (mode) {
            FHTMode.UNKNOWN -> {
            }
            FHTMode.HOLIDAY -> handleHolidayMode(
                device,
                spinnerActionRow,
                context,
                viewGroup,
                inflater,
                connectionId
            )
            FHTMode.HOLIDAY_SHORT -> {
                holidayShort.showDialog(
                    context,
                    viewGroup,
                    inflater,
                    spinnerActionRow,
                    device,
                    connectionId
                )
            }
            else -> {
                GlobalScope.launch(Dispatchers.Main) {
                    withContext(Dispatchers.IO) {
                        genericDeviceService.setSubState(
                            device,
                            "mode",
                            mode.name.lowercase(Locale.getDefault()),
                            connectionId
                        )
                    }
                    spinnerActionRow.commitSelection()
                    context.sendBroadcast(Intent(BundleExtraKeys.DO_REFRESH)
                        .apply { setPackage(context.packageName) })

                }
            }
        }
    }

    private fun handleHolidayMode(
        device: XmlListDevice, spinnerActionRow: SpinnerActionRow,
        context: Context, parent: ViewGroup, inflater: LayoutInflater,
        connectionId: String?
    ) {

        val dialogBuilder = AlertDialog.Builder(context)

        val contentView =
            inflater.inflate(R.layout.fht_holiday_dialog, parent, false) as TableLayout

        val datePicker = contentView.findViewById(R.id.datePicker) as DatePicker
        DatePickerUtil.hideYearField(datePicker)

        val temperatureUpdateRow = contentView.findViewById(R.id.updateRow) as TableRow

        val temperatureChangeTableRow = TemperatureChangeTableRow(
            context,
            FHTDetailActionProvider.MINIMUM_TEMPERATURE,
            temperatureUpdateRow,
            FHTDetailActionProvider.MINIMUM_TEMPERATURE,
            FHTDetailActionProvider.MAXIMUM_TEMPERATURE,
            applicationProperties
        )
        contentView.addView(temperatureChangeTableRow.createRow(inflater, device))

        dialogBuilder.setView(contentView)

        dialogBuilder.setNegativeButton(R.string.cancelButton) { dialogInterface, _ ->
            spinnerActionRow.revertSelection()
            dialogInterface.dismiss()
        }

        dialogBuilder.setPositiveButton(R.string.okButton) { dialogInterface, _ ->
            GlobalScope.launch(Dispatchers.Main) {
                withContext(Dispatchers.IO) {
                    genericDeviceService.setSubStates(
                        device, listOf(
                            StateToSet("desired-temp", "" + temperatureChangeTableRow.temperature),
                            StateToSet("holiday1", "" + datePicker.dayOfMonth),
                            StateToSet("holiday2", "" + (datePicker.month + 1)),
                            StateToSet(
                                "mode",
                                FHTMode.HOLIDAY.name.lowercase(Locale.getDefault())
                            )
                        ), connectionId
                    )
                }
                spinnerActionRow.commitSelection()
                dialogInterface.dismiss()
            }
        }

        dialogBuilder.show()
    }
}
