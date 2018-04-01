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

import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TableRow
import kotlinx.android.synthetic.main.device_detail_colorpicker_row.view.*
import li.klass.fhem.R
import li.klass.fhem.adapter.uiservice.StateUiService
import li.klass.fhem.domain.setlist.typeEntry.RGBSetListEntry
import li.klass.fhem.update.backend.xmllist.XmlListDevice
import li.klass.fhem.util.ColorUtil
import org.jetbrains.anko.backgroundColor

class StateChangingColorPickerRow(private val stateUiService: StateUiService,
                                  private val xmlListDevice: XmlListDevice,
                                  private val connectionId: String?,
                                  private val rgbSetListEntry: RGBSetListEntry
) {

    fun createRow(context: Context, inflater: LayoutInflater, viewGroup: ViewGroup): TableRow {
        val view = inflater.inflate(R.layout.device_detail_colorpicker_row, viewGroup, false)
        val rgb = xmlListDevice.getState(rgbSetListEntry.key, ignoreCase = true)!!

        view.color_value.backgroundColor = ColorUtil.fromRgbString(rgb) or -0x1000000
        view.description.text = ""
        view.set.setOnClickListener {
            RGBColorPickerDialog(context, rgb, object : RGBColorPickerDialog.Callback {
                override fun onColorChanged(newRGB: String, dialog: Dialog) {
                    stateUiService.setSubState(xmlListDevice, rgbSetListEntry.key, newRGB, connectionId, context)
                }

                override fun onColorUnchanged(dialog: Dialog) {}
            }).show()
        }

        return view as TableRow
    }
}
