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
import li.klass.fhem.domain.core.FhemDevice
import li.klass.fhem.domain.setlist.SetListEntry
import li.klass.fhem.domain.setlist.typeEntry.GroupSetListEntry
import li.klass.fhem.domain.setlist.typeEntry.MultipleStrictSetListEntry

class MultipleSetListTargetStateHandler : SetListTargetStateHandler<FhemDevice> {
    override fun canHandle(entry: SetListEntry): Boolean {
        return entry is GroupSetListEntry || entry is MultipleStrictSetListEntry
    }

    override fun handle(entry: SetListEntry, context: Context, device: FhemDevice, callback: OnTargetStateSelectedCallback<FhemDevice>) {
        val groupSetListEntry = entry as GroupSetListEntry

        val states = listOf(context.getString(R.string.customText)) + groupSetListEntry.groupStates
        AlertDialog.Builder(context)
                .setTitle(device.aliasOrName + " " + groupSetListEntry.key)
                .setItems(states.toTypedArray()) { dialog, which ->
                    if (which == 0) {
                        TextFieldTargetStateHandler().handle(entry, context, device, callback)
                    } else {
                        GlobalScope.launch(Dispatchers.Main) {
                            callback.onSubStateSelected(device, groupSetListEntry.key, states[which])
                        }
                    }
                    dialog.dismiss()
                }
                .setNegativeButton(R.string.cancelButton) { dialog, which ->
                    GlobalScope.launch(Dispatchers.Main) {
                        callback.onNothingSelected(device)
                    }
                    dialog.dismiss()
                }
                .show()
    }
}
