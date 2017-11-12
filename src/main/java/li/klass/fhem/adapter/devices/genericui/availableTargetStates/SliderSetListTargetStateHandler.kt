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

package li.klass.fhem.adapter.devices.genericui.availableTargetStates

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import li.klass.fhem.R
import li.klass.fhem.adapter.devices.genericui.DeviceDimActionRowFullWidth
import li.klass.fhem.domain.core.DimmableDevice
import li.klass.fhem.domain.core.FhemDevice
import li.klass.fhem.domain.setlist.SetListEntry
import li.klass.fhem.domain.setlist.typeEntry.SliderSetListEntry
import li.klass.fhem.update.backend.xmllist.XmlListDevice

class SliderSetListTargetStateHandler<D : FhemDevice> : SetListTargetStateHandler<D> {
    private var dimProgress = 0f

    override fun canHandle(entry: SetListEntry): Boolean {
        return entry is SliderSetListEntry
    }

    override fun handle(entry: SetListEntry, context: Context, device: D, callback: OnTargetStateSelectedCallback<D>) {
        val sliderSetListEntry = entry as SliderSetListEntry

        var initialProgress = 0f
        if (device is DimmableDevice<*>) {
            initialProgress = device.getDimPosition()
        }

        val inflater = LayoutInflater.from(context)
        @SuppressLint("InflateParams") val tableLayout = inflater.inflate(R.layout.availabletargetstates_action_with_seekbar, null, false) as TableLayout

        val updateRow = tableLayout.findViewById<TableRow>(R.id.updateRow)
        (updateRow.findViewById<TextView>(R.id.description)).text = ""
        (updateRow.findViewById<TextView>(R.id.value)).text = ""

        tableLayout.addView(object : DeviceDimActionRowFullWidth(initialProgress,
                sliderSetListEntry.start, sliderSetListEntry.step, sliderSetListEntry.stop,
                updateRow, R.layout.device_detail_seekbarrow_full_width) {

            override fun onStopDim(context: Context, device: XmlListDevice, progress: Float) {
                dimProgress = progress
            }

            override fun toDimUpdateText(device: XmlListDevice, progress: Float): String {
                return getFormattedDimProgress(progress)
            }
        }.createRow(inflater, device))

        AlertDialog.Builder(context)
                .setTitle(device.getAliasOrName() + " " + sliderSetListEntry.key)
                .setView(tableLayout)
                .setNegativeButton(R.string.cancelButton) { dialog, _ ->
                    callback.onNothingSelected(device)
                    dialog.dismiss()
                }
                .setPositiveButton(R.string.okButton) { dialog, _ ->
                    callback.onSubStateSelected(device, sliderSetListEntry.key, getFormattedDimProgress(dimProgress))
                    dialog.dismiss()
                }
                .show()
    }

    private fun getFormattedDimProgress(progress: Float): String {
        if (progress % 1 == 0f) {
            return progress.toInt().toString() + ""
        }
        return progress.toString() + ""
    }
}
