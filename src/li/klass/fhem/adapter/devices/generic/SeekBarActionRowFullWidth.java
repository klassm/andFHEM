/*
 * AndFHEM - Open Source Android application to control a FHEM home automation
 * server.
 *
 * Copyright (c) 2012, Matthias Klass or third-party contributors as
 * indicated by the @author tags or express copyright attribution
 * statements applied by the authors.  All third-party contributions are
 * distributed under license by Red Hat Inc.
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU GENERAL PUBLICLICENSE, as published by the Free Software Foundation.
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
 */

package li.klass.fhem.adapter.devices.generic;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.view.LayoutInflater;
import android.widget.SeekBar;
import android.widget.TableRow;
import li.klass.fhem.R;
import li.klass.fhem.constants.Actions;
import li.klass.fhem.constants.BundleExtraKeys;
import li.klass.fhem.domain.Device;

public class SeekBarActionRowFullWidth<T extends Device> {
    private int initialProgress;

    public SeekBarActionRowFullWidth(int initialProgress) {
        this.initialProgress = initialProgress;
    }

    public TableRow createRow(LayoutInflater inflater, T device) {
        TableRow row = (TableRow) inflater.inflate(R.layout.device_detail_seekbarrow_full_width, null);
        SeekBar seekBar = (SeekBar) row.findViewById(R.id.seekBar);
        seekBar.setOnSeekBarChangeListener(createListener(device));
        seekBar.setProgress(initialProgress);

        return row;
    }

    private SeekBar.OnSeekBarChangeListener createListener(final T device) {
        return new SeekBar.OnSeekBarChangeListener() {

            public int progress = initialProgress;

            @Override
            public void onProgressChanged(SeekBar seekBar, final int progress, boolean fromUser) {
                this.progress = progress;
                SeekBarActionRowFullWidth.this.onProgressChanged(seekBar.getContext(), device, progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(final SeekBar seekBar) {
                SeekBarActionRowFullWidth.this.onStopTrackingTouch(seekBar.getContext(), device, progress);
            }
        };
    }

    public void onProgressChanged(Context context, T device, int progress) {}

    public void onStopTrackingTouch(final Context context, T device, int progress) {
        Intent intent = new Intent(Actions.DEVICE_DIM);
        intent.putExtra(BundleExtraKeys.DEVICE_DIM_PROGRESS, progress);
        intent.putExtra(BundleExtraKeys.DEVICE_NAME, device.getName());
        intent.putExtra(BundleExtraKeys.RESULT_RECEIVER, new ResultReceiver(new Handler()) {
            @Override
            protected void onReceiveResult(int resultCode, Bundle resultData) {
                context.sendBroadcast(new Intent(Actions.DO_UPDATE));
            }
        });

        context.startService(intent);
    }
}
