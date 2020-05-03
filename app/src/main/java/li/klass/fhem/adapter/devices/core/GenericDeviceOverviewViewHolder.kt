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

package li.klass.fhem.adapter.devices.core

import android.view.View
import android.widget.ImageView
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView

import li.klass.fhem.R

class GenericDeviceOverviewViewHolder(convertView: View) {

    val tableLayout: TableLayout = convertView.findViewById(R.id.device_overview_generic)
    val deviceName: TextView = convertView.findViewById(R.id.deviceName)
    private val deviceNameRow: TableRow = convertView.findViewById(R.id.overviewRow)
    private val tableRows = ArrayList<GenericDeviceTableRowHolder>()
    private val additionalHolders = HashMap<String, Any>()

    val tableRowCount: Int
        get() = tableRows.size

    class GenericDeviceTableRowHolder(
            val row: TableRow,
            val description: TextView,
            val value: TextView,
            val devStateIcon: ImageView
    )

    fun resetHolder() {
        deviceName.visibility = View.VISIBLE
        tableLayout.removeAllViews()
        tableLayout.addView(deviceNameRow)
    }

    fun addTableRow(row: GenericDeviceTableRowHolder) {
        tableRows.add(row)
    }

    fun getTableRowAt(index: Int): GenericDeviceTableRowHolder = tableRows[index]

    @Suppress("UNCHECKED_CAST")
    fun <T> getAdditionalHolderFor(key: String): T = additionalHolders[key] as T

    fun putAdditionalHolder(key: String, value: Any) {
        additionalHolders.put(key, value)
    }
}
