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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import li.klass.fhem.R
import li.klass.fhem.domain.core.DeviceStateAdditionalInformationType
import li.klass.fhem.domain.core.DeviceStateRequiringAdditionalInformation
import li.klass.fhem.domain.core.FhemDevice
import li.klass.fhem.domain.setlist.SetListEntry
import li.klass.fhem.domain.setlist.typeEntry.NoArgSetListEntry
import li.klass.fhem.widget.TimePickerWithSeconds

class SpecialButtonSecondsHandler : SetListTargetStateHandler<FhemDevice> {
    override fun canHandle(entry: SetListEntry): Boolean {
        val additionalInformation = DeviceStateRequiringAdditionalInformation.deviceStateForFHEM(entry.key)
        return (entry is NoArgSetListEntry
                && additionalInformation != null && additionalInformation.additionalType == DeviceStateAdditionalInformationType.SECONDS)
    }

    override fun handle(entry: SetListEntry, context: Context, device: FhemDevice, callback: OnTargetStateSelectedCallback) {
        val timePicker = TimePickerWithSeconds(context)
        timePicker.hours = 0
        timePicker.minutes = 0
        timePicker.seconds = 0

        AlertDialog.Builder(context)
                .setTitle(device.aliasOrName + " " + entry.key)
                .setMessage(R.string.blank)
                .setView(timePicker)
                .setNegativeButton(R.string.cancelButton) { dialog, _ ->
                    GlobalScope.launch(Dispatchers.Main) {
                        callback.onNothingSelected(device)
                    }
                    dialog.dismiss()
                }
                .setPositiveButton(R.string.okButton) { _, _ ->
                    val seconds = 60 * (timePicker.hours * 60 + timePicker.minutes) + timePicker.seconds
                    GlobalScope.launch(Dispatchers.Main) {
                        callback.onSubStateSelected(device, entry.key, seconds.toString())
                    }
                }
                .show()
    }
}
