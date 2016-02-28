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

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import li.klass.fhem.R;
import li.klass.fhem.adapter.devices.genericui.DeviceDimActionRowFullWidth;
import li.klass.fhem.domain.core.DimmableDevice;
import li.klass.fhem.domain.core.FhemDevice;
import li.klass.fhem.domain.setlist.SetListEntry;
import li.klass.fhem.domain.setlist.typeEntry.SliderSetListEntry;
import li.klass.fhem.service.room.xmllist.XmlListDevice;

public class SliderSetListTargetStateHandler<D extends FhemDevice<?>> implements SetListTargetStateHandler<D> {
    private float dimProgress = 0;

    @Override
    public boolean canHandle(SetListEntry entry) {
        return entry instanceof SliderSetListEntry;
    }

    @Override
    public void handle(SetListEntry entry, final Context context, final D device, final OnTargetStateSelectedCallback<D> callback) {
        final SliderSetListEntry sliderSetListEntry = (SliderSetListEntry) entry;

        float initialProgress = 0;
        if (device instanceof DimmableDevice) {
            initialProgress = ((DimmableDevice) device).getDimPosition();
        }

        LayoutInflater inflater = LayoutInflater.from(context);
        @SuppressLint("InflateParams") TableLayout tableLayout = (TableLayout) inflater.inflate(R.layout.availabletargetstates_action_with_seekbar, null, false);

        TableRow updateRow = (TableRow) tableLayout.findViewById(R.id.updateRow);
        assert updateRow != null;
        ((TextView) updateRow.findViewById(R.id.description)).setText("");
        ((TextView) updateRow.findViewById(R.id.value)).setText("");

        tableLayout.addView(new DeviceDimActionRowFullWidth(initialProgress,
                sliderSetListEntry.getStart(), sliderSetListEntry.getStep(), sliderSetListEntry.getStop(),
                updateRow, R.layout.device_detail_seekbarrow_full_width) {

            @Override
            public void onStopDim(Context context, XmlListDevice device, float progress) {
                dimProgress = progress;
            }

            @Override
            public String toDimUpdateText(XmlListDevice device, float progress) {
                return progress + "";
            }
        }.createRow(inflater, device));

        new AlertDialog.Builder(context)
                .setTitle(device.getAliasOrName() + " " + sliderSetListEntry.getKey())
                .setView(tableLayout)
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
                        callback.onSubStateSelected(device, sliderSetListEntry.getKey(), dimProgress + "");
                        dialog.dismiss();
                    }
                })
                .show();
    }
}
