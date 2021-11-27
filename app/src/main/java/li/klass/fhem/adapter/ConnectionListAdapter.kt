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
package li.klass.fhem.adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import li.klass.fhem.R
import li.klass.fhem.connection.backend.FHEMServerSpec

class ConnectionListAdapter(context: Context?) : ListDataAdapter<FHEMServerSpec>(
    context!!, mutableListOf()
) {
    private var selectedConnectionId: String? = null

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val myView = convertView ?: inflater.inflate(R.layout.connection_list_entry, null)
        val server = getItem(position) as FHEMServerSpec
        val nameView = myView.findViewById<TextView>(R.id.connectionListName)
        nameView.text = server.name
        val typeView = myView.findViewById<TextView>(R.id.connectionListType)
        typeView.text = server.serverType.name
        myView.tag = server.id
        if (server.id == selectedConnectionId) {
            myView.setBackgroundColor(context.resources.getColor(R.color.android_green))
        }
        return myView
    }

    fun updateData(newData: List<FHEMServerSpec>?, selectedConnectionId: String?) {
        if (newData == null) return
        this.selectedConnectionId = selectedConnectionId
        updateData(newData.toMutableList())
    }
}