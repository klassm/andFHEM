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

package li.klass.fhem.adapter.devices.strategy

import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.widget.TableRow
import android.widget.TextView
import li.klass.fhem.adapter.devices.core.deviceItems.XmlDeviceViewItem
import li.klass.fhem.domain.core.FhemDevice

abstract class ViewStrategy {
    abstract suspend fun createOverviewView(layoutInflater: LayoutInflater, convertView: View?, rawDevice: FhemDevice, deviceItems: List<XmlDeviceViewItem>, connectionId: String?): View

    protected fun setTextView(textView: TextView?, value: String?) {
        val myValue = if (value == null) "" else value
        @Suppress("DEPRECATION")
        val toSet = if (myValue.contains("<")) Html.fromHtml(myValue) else myValue
        if (textView != null) {
            textView.text = toSet
        }
    }

    internal fun setTextViewOrHideTableRow(view: View, tableRowId: Int, textFieldLayoutId: Int, value: String) {
        val tableRow = view.findViewById<TableRow>(tableRowId)

        if (hideIfNull(tableRow, value)) {
            return
        }

        setTextView(view, textFieldLayoutId, value)
    }

    internal fun setTextView(view: View, textFieldLayoutId: Int, value: String) {
        val textView = view.findViewById<TextView>(textFieldLayoutId)
        if (textView != null) {
            textView.text = value
        }
    }

    private fun hideIfNull(layoutElement: View, valueToCheck: Any?): Boolean {
        if (valueToCheck == null || valueToCheck is String && valueToCheck.length == 0) {
            layoutElement.visibility = View.GONE
            return true
        }
        return false
    }

    abstract fun supports(fhemDevice: FhemDevice): Boolean
}
