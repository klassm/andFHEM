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

package li.klass.fhem.adapter.devices.genericui

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TableRow
import android.widget.TextView
import li.klass.fhem.R
import li.klass.fhem.domain.core.FhemDevice
import org.apmem.tools.layouts.FlowLayout

abstract class HolderActionRow<I>(private val description: String, private val layout: Int) {

    suspend fun createRow(context: Context, viewGroup: ViewGroup, device: FhemDevice, connectionId: String?): TableRow {
        val inflater = LayoutInflater.from(context)
        val row = inflater.inflate(layout, viewGroup, false) as TableRow

        val holder = row.findViewById<FlowLayout>(R.id.holder)

        val descriptionView = row.findViewById<TextView>(R.id.description)
        if (descriptionView != null) {
            descriptionView.text = description
        }

        for (item in getItems(device)) {
            val view = viewFor(item, device, inflater, context, holder, connectionId)
            if (view != null) {
                holder.addView(view)
            }
        }

        return row
    }

    abstract fun getItems(device: FhemDevice): List<I>

    abstract suspend fun viewFor(item: I, device: FhemDevice, inflater: LayoutInflater, context: Context, viewGroup: ViewGroup, connectionId: String?): View?

    companion object {

        val LAYOUT_DETAIL = R.layout.device_detail_holder_row
        val LAYOUT_OVERVIEW = R.layout.device_overview_holder_row
    }
}
