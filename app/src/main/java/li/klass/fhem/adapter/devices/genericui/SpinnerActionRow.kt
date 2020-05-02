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
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
import li.klass.fhem.R
import li.klass.fhem.update.backend.xmllist.XmlListDevice

abstract class SpinnerActionRow {
    private var description: String?
    private var prompt: String
    private var spinnerValues: List<String?>
    private var selectedPosition: Int
    private var temporarySelectedPosition = 0
    private var context: Context
    private var rowView: TableRow? = null
    private var ignoreItemSelection = false

    constructor(context: Context, description: String?, prompt: String, spinnerValues: List<String?>, selectedValue: String?) {
        var values = spinnerValues
        var selected = selectedValue
        if (!values.contains(selected)) {
            selected = context.getString(R.string.selectValue)
            values = listOf(selected) + (values)
        }
        this.description = description
        this.prompt = prompt
        this.spinnerValues = values
        selectedPosition = values.indexOf(selected)
        this.context = context
    }

    constructor(context: Context, description: String?, prompt: String, spinnerValues: List<String?>, selectedPosition: Int) {
        this.description = description
        this.prompt = prompt
        this.spinnerValues = spinnerValues
        this.selectedPosition = selectedPosition
        this.context = context
    }

    fun createRow(device: XmlListDevice, connectionId: String?, viewGroup: ViewGroup?): TableRow {
        ignoreItemSelection = true
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.device_detail_spinnerrow, viewGroup, false) as TableRow
        rowView = view
        (rowView!!.findViewById<View>(R.id.description) as TextView).text = description
        val spinner = rowView!!.findViewById<Spinner>(R.id.spinner)
        spinner.prompt = prompt
        val adapter: ArrayAdapter<*> = ArrayAdapter(context, R.layout.spinnercontent, spinnerValues)
        spinner.adapter = adapter
        spinner.setSelection(selectedPosition)
        spinner.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>?, view: View, position: Int, id: Long) {
                if (ignoreItemSelection || selectedPosition == position) {
                    revertSelection()
                    return
                }
                temporarySelectedPosition = position
                this@SpinnerActionRow.onItemSelected(context, device, connectionId, spinnerValues[position]!!)
            }

            override fun onNothingSelected(adapterView: AdapterView<*>?) {}
        }
        ignoreItemSelection = false
        return view
    }

    fun revertSelection() {
        val spinner = rowView!!.findViewById<Spinner>(R.id.spinner)
        spinner.setSelection(selectedPosition)
    }

    fun commitSelection() {
        selectedPosition = temporarySelectedPosition
    }

    abstract fun onItemSelected(context: Context, device: XmlListDevice, connectionId: String?, item: String)
}