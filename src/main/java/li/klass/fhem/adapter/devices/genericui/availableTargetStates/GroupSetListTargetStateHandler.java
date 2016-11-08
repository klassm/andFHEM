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

package li.klass.fhem.adapter.devices.genericui.availableTargetStates;

import android.app.AlertDialog;
import android.content.Context;

import com.google.common.collect.Iterables;

import li.klass.fhem.R;
import li.klass.fhem.domain.core.FhemDevice;
import li.klass.fhem.domain.setlist.SetListEntry;
import li.klass.fhem.domain.setlist.typeEntry.GroupSetListEntry;
import li.klass.fhem.domain.setlist.typeEntry.MultipleStrictSetListEntry;

public class GroupSetListTargetStateHandler<D extends FhemDevice<?>> implements SetListTargetStateHandler<D> {
    @Override
    public boolean canHandle(SetListEntry entry) {
        return entry instanceof GroupSetListEntry || entry instanceof MultipleStrictSetListEntry;
    }

    @Override
    public void handle(SetListEntry entry, final Context context, final D device, final OnTargetStateSelectedCallback<D> callback) {
        final GroupSetListEntry groupSetListEntry = (GroupSetListEntry) entry;

        new AlertDialog.Builder(context)
                .setTitle(device.getAliasOrName() + " " + groupSetListEntry.getKey())
                .setItems(Iterables.toArray(groupSetListEntry.getGroupStates(), CharSequence.class), (dialog, which) -> {
                    String subState = groupSetListEntry.getGroupStates().get(which);
                    callback.onSubStateSelected(device, groupSetListEntry.getKey(), subState);
                    dialog.dismiss();
                })
                .setNegativeButton(R.string.cancelButton, (dialog, which) -> {
                    callback.onNothingSelected(device);
                    dialog.dismiss();
                })
                .show();
    }
}
