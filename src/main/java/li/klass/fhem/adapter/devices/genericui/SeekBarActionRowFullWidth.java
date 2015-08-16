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

package li.klass.fhem.adapter.devices.genericui;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.SeekBar;
import android.widget.TableRow;
import android.widget.TextView;

import li.klass.fhem.R;
import li.klass.fhem.domain.core.FhemDevice;

public abstract class SeekBarActionRowFullWidth {
    protected int initialProgress;
    private int layoutId;
    protected int maximumProgress;
    protected int minimumProgress;
    protected TextView updateView;

    public SeekBarActionRowFullWidth(int initialProgress, int minimumProgress, int maximumProgress, int layoutId,
                                     TableRow updateRow) {
        this.initialProgress = initialProgress - minimumProgress;
        this.maximumProgress = maximumProgress;
        this.minimumProgress = minimumProgress;
        this.layoutId = layoutId;

        setUpdateRow(updateRow);
    }

    protected void setUpdateRow(TableRow updateRow) {
        if (updateRow != null) {
            updateView = (TextView) updateRow.findViewById(R.id.value);
        }
    }

    public TableRow createRow(LayoutInflater inflater, FhemDevice device) {
        return createRow(inflater, device, 1);
    }

    public TableRow createRow(LayoutInflater inflater, FhemDevice device, int layoutSpan) {
        TableRow row = (TableRow) inflater.inflate(layoutId, null);
        SeekBar seekBar = (SeekBar) row.findViewById(R.id.seekBar);
        seekBar.setOnSeekBarChangeListener(createListener(device));
        seekBar.setMax(maximumProgress - minimumProgress);
        seekBar.setProgress(initialProgress);

        if (layoutSpan != 1) {
            TableRow.LayoutParams layoutParams = (TableRow.LayoutParams) seekBar.getLayoutParams();
            layoutParams.span = layoutSpan;
            seekBar.setLayoutParams(layoutParams);
        }

        return row;
    }

    private SeekBar.OnSeekBarChangeListener createListener(final FhemDevice device) {
        return new SeekBar.OnSeekBarChangeListener() {

            public int progress = initialProgress;

            @Override
            public void onProgressChanged(SeekBar seekBar, final int progress, boolean fromUser) {
                this.progress = progress + minimumProgress;
                if (updateView != null && fromUser) {
                    SeekBarActionRowFullWidth.this.onProgressChanged(updateView, seekBar.getContext(), device, progress);
                }
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

    public void onProgressChanged(TextView updateView, Context context, FhemDevice device, int progress) {
        updateView.setText(toUpdateText(device, progress));
    }

    public String toUpdateText(FhemDevice device, int progress) {
        return progress + "";
    }

    public abstract void onStopTrackingTouch(final Context context, FhemDevice device, int progress);
}
