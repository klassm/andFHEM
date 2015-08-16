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
import android.content.Intent;
import android.view.LayoutInflater;
import android.widget.SeekBar;
import android.widget.TableRow;
import android.widget.TextView;

import li.klass.fhem.R;
import li.klass.fhem.adapter.devices.core.UpdatingResultReceiver;
import li.klass.fhem.adapter.uiservice.StateUiService;
import li.klass.fhem.behavior.dim.DimmableBehavior;
import li.klass.fhem.constants.Actions;
import li.klass.fhem.domain.core.DimmableDevice;
import li.klass.fhem.domain.core.FhemDevice;
import li.klass.fhem.service.intent.DeviceIntentService;

import static li.klass.fhem.constants.BundleExtraKeys.DEVICE_DIM_PROGRESS;
import static li.klass.fhem.constants.BundleExtraKeys.DEVICE_NAME;
import static li.klass.fhem.constants.BundleExtraKeys.RESULT_RECEIVER;

public class DimActionRow {
    private final StateUiService stateUiService;
    private final Context context;
    private TextView updateView;
    private TextView description;

    public static final int LAYOUT_OVERVIEW = R.layout.device_overview_seekbarrow;
    public static final String HOLDER_KEY = "DimActionRow";
    private SeekBar seekBar;
    private TableRow tableRow;

    public DimActionRow(LayoutInflater inflater, StateUiService stateUiService, Context context) {
        tableRow = (TableRow) inflater.inflate(LAYOUT_OVERVIEW, null);
        description = (TextView) tableRow.findViewById(R.id.description);
        seekBar = (SeekBar) tableRow.findViewById(R.id.seekBar);
        this.stateUiService = stateUiService;
        this.context = context;
    }

    public TableRow getView() {
        return tableRow;
    }

    public void fillWith(final FhemDevice device, TableRow updateRow) {
        DimmableBehavior behavior = DimmableBehavior.behaviorFor(device).get();

        DimmableDevice dimmableDevice = (DimmableDevice) device;
        seekBar.setOnSeekBarChangeListener(createListener(behavior));
        seekBar.setMax(behavior.getDimUpperBound());
        seekBar.setProgress(behavior.getCurrentDimPosition());
        description.setText(dimmableDevice.getAliasOrName());
        if (updateRow != null) {
            updateView = (TextView) updateRow.findViewById(R.id.value);
        }
    }

    private SeekBar.OnSeekBarChangeListener createListener(final DimmableBehavior behavior) {
        return new SeekBar.OnSeekBarChangeListener() {

            public int progress = behavior.getCurrentDimPosition();

            @Override
            public void onProgressChanged(SeekBar seekBar, final int progress, boolean fromUser) {
                this.progress = progress;

                if (updateView != null) {
                    updateView.setText(behavior.getDimStateForPosition(progress));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(final SeekBar seekBar) {
                behavior.switchTo(stateUiService, context, progress);
            }
        };
    }
}
