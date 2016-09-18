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
import android.content.DialogInterface;
import android.widget.TimePicker;

import li.klass.fhem.R;
import li.klass.fhem.domain.core.DeviceStateAdditionalInformationType;
import li.klass.fhem.domain.core.DeviceStateRequiringAdditionalInformation;
import li.klass.fhem.domain.core.FhemDevice;
import li.klass.fhem.domain.setlist.SetListEntry;
import li.klass.fhem.domain.setlist.typeEntry.NoArgSetListEntry;

public class SpecialButtonSecondsHandler<D extends FhemDevice<?>> implements SetListTargetStateHandler<D> {
    @Override
    public boolean canHandle(SetListEntry entry) {
        DeviceStateRequiringAdditionalInformation additionalInformation = DeviceStateRequiringAdditionalInformation.deviceStateForFHEM(entry.getKey());
        return entry instanceof NoArgSetListEntry
                && additionalInformation != null && additionalInformation.getAdditionalType() == DeviceStateAdditionalInformationType.SECONDS;
    }

    @Override
    public void handle(final SetListEntry entry, final Context context, final D device, final OnTargetStateSelectedCallback<D> callback) {
        final TimePicker timePicker = new TimePicker(context);
        timePicker.setCurrentHour(0);
        timePicker.setCurrentMinute(0);
        timePicker.setIs24HourView(true);

        new AlertDialog.Builder(context)
                .setTitle(device.getAliasOrName() + " " + entry.getKey())
                .setMessage(R.string.timeToSwitchAction)
                .setView(timePicker)
                .setNegativeButton(R.string.cancelButton, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        callback.onNothingSelected(device);
                        dialog.dismiss();
                    }
                })
                .setPositiveButton(R.string.okButton, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int seconds = 60 * (timePicker.getCurrentHour() * 60 + timePicker.getCurrentMinute());
                        callback.onSubStateSelected(device, entry.getKey(), String.valueOf(seconds));
                    }
                })
                .show();
    }
}
