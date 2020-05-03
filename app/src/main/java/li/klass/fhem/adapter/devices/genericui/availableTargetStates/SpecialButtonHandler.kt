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

import android.app.AlertDialog
import android.content.Context
import android.widget.EditText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import li.klass.fhem.R
import li.klass.fhem.domain.core.DeviceStateAdditionalInformationType
import li.klass.fhem.domain.core.DeviceStateRequiringAdditionalInformation
import li.klass.fhem.domain.core.FhemDevice
import li.klass.fhem.domain.setlist.SetListEntry
import li.klass.fhem.domain.setlist.typeEntry.NoArgSetListEntry
import li.klass.fhem.domain.setlist.typeEntry.TimeSetListEntry
import li.klass.fhem.util.DialogUtil

class SpecialButtonHandler : SetListTargetStateHandler<FhemDevice> {
    override fun canHandle(entry: SetListEntry): Boolean {
        return entry is NoArgSetListEntry && DeviceStateRequiringAdditionalInformation.deviceStateForFHEM(entry.key) != null
    }

    override fun handle(entry: SetListEntry, context: Context, device: FhemDevice, callback: OnTargetStateSelectedCallback) {
        val additionalInformation = DeviceStateRequiringAdditionalInformation.deviceStateForFHEM(entry.key)
        val type = additionalInformation.additionalType
        if (type == DeviceStateAdditionalInformationType.TIME) {
            TimeTargetStateHandler().handle(TimeSetListEntry(entry.key), context, device, callback)
            return
        }

        val editText = EditText(context)
        AlertDialog.Builder(context)
                .setTitle(device.aliasOrName + " " + entry.key)
                .setView(editText)
                .setNegativeButton(R.string.cancelButton) { dialog, _ ->
                    dialog.dismiss()
                    GlobalScope.launch(Dispatchers.Main) {
                        callback.onNothingSelected(device)
                    }
                }
                .setPositiveButton(R.string.okButton) { dialog, _ ->
                    val value = editText.text.toString()
                    if (!type.matches(value)) {
                        DialogUtil.showAlertDialog(context, R.string.error, R.string.invalidInput)
                    } else {
                        GlobalScope.launch(Dispatchers.Main) {
                            callback.onSubStateSelected(device, entry.key, value)
                        }
                        dialog.dismiss()
                    }
                }
                .show()
    }
}
