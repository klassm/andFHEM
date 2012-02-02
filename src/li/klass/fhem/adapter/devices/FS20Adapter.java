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

package li.klass.fhem.adapter.devices;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.view.View;
import android.widget.*;
import li.klass.fhem.AndFHEMApplication;
import li.klass.fhem.R;
import li.klass.fhem.adapter.devices.core.DeviceDetailAvailableAdapter;
import li.klass.fhem.constants.Actions;
import li.klass.fhem.constants.BundleExtraKeys;
import li.klass.fhem.domain.Device;
import li.klass.fhem.domain.FS20Device;

import java.util.List;

public class FS20Adapter extends DeviceDetailAvailableAdapter<FS20Device> {

    private class SeekBarChangeListener implements SeekBar.OnSeekBarChangeListener {

        public int progress;

        private SeekBarChangeListener(int progress) {
            this.progress = progress;
        }

        @Override
        public void onProgressChanged(SeekBar seekBar, final int progress, boolean fromUser) {
            this.progress = progress;
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(final SeekBar seekBar) {
            final Context context = seekBar.getContext();
            String deviceName = (String) seekBar.getTag();

            Intent intent = new Intent(Actions.DEVICE_DIM);
            intent.putExtra(BundleExtraKeys.DEVICE_DIM_PROGRESS, progress);
            intent.putExtra(BundleExtraKeys.DEVICE_NAME, deviceName);
            intent.putExtra(BundleExtraKeys.RESULT_RECEIVER, new ResultReceiver(new Handler()) {
                @Override
                protected void onReceiveResult(int resultCode, Bundle resultData) {
                    context.sendBroadcast(new Intent(Actions.DO_UPDATE));
                }
            });

            context.startService(intent);
        }
    }
    
    private class SwitchButtonListener implements View.OnClickListener {

        private String deviceName;

        public SwitchButtonListener(String deviceName) {
            this.deviceName = deviceName;
        }

        @Override
        public void onClick(View view) {
            Intent intent = new Intent(Actions.DEVICE_TOGGLE_STATE);
            intent.putExtras(new Bundle());
            intent.putExtra(BundleExtraKeys.DEVICE_NAME, deviceName);
            AndFHEMApplication.getContext().startService(intent);
        }
    }

    @Override
    public int getOverviewLayout(FS20Device device) {
        if (device.isDimDevice()) {
            return R.layout.room_detail_fs20_seek;
        } else {
            return R.layout.room_detail_fs20;
        }
    }

    @Override
    public void fillDeviceOverviewView(View view, FS20Device device) {
        if (device.isDimDevice()) {
            fillFS20SeekView(view, device);
        } else {
            fillFS20ToggleView(view, device);
        }
    }

    @Override
    protected void fillDeviceDetailView(final Context context, View view, final FS20Device device) {


        setTextViewOrHideTableRow(view, R.id.tableRowState, R.id.state, device.getState());

        TableRow seekBarRow = (TableRow) view.findViewById(R.id.switchSeekBarRow);
        TableRow toggleButtonRow = (TableRow) view.findViewById(R.id.switchToggleButtonRow);

        SeekBar seekBar = (SeekBar) view.findViewById(R.id.seekBar);
        ToggleButton switchButton = (ToggleButton) view.findViewById(R.id.switchButton);

        if (device.isDimDevice()) {
            int initialProgress = device.getFS20DimState();
            seekBar.setProgress(initialProgress);
            seekBar.setOnSeekBarChangeListener(new SeekBarChangeListener(device.getFS20DimState()));
            seekBar.setTag(device.getName());

            toggleButtonRow.setVisibility(View.GONE);
        } else {
            switchButton.setChecked(device.isOn());
            switchButton.setOnClickListener(new SwitchButtonListener(device.getName()));
            switchButton.setTag(device.getName());

            seekBarRow.setVisibility(View.GONE);
        }

        Button switchSetOptions = (Button) view.findViewById(R.id.switchSetOptions);
        switchSetOptions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder contextMenu = new AlertDialog.Builder(context);
                contextMenu.setTitle(context.getResources().getString(R.string.switchDevice));
                final List<String> setOptions = device.getSetOptions();

                contextMenu.setItems(setOptions.toArray(new CharSequence[setOptions.size()]), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        final String option = setOptions.get(item);

                        if (option.equals("off-for-timer") || option.equals("on-for-timer")) {
                            final EditText input = new EditText(context);
                            new AlertDialog.Builder(context)
                                    .setTitle(R.string.howLong)
                                    .setView(input)
                                    .setPositiveButton(R.string.okButton, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int whichButton) {
                                            String time = input.getText().toString();
                                            try {
                                                Integer.valueOf(time);
                                                switchDeviceState(option + " " + time, device, context);
                                            } catch (NumberFormatException e) {
                                                Toast.makeText(context, R.string.notNumericError, Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    }).setNegativeButton(R.string.cancelButton, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                }
                            }).show();
                        } else {
                            switchDeviceState(option, device, context);
                        }
                        dialog.dismiss();
                    }


                });
                contextMenu.show();

            }
        });
    }

    private void switchDeviceState(String newState, FS20Device device, final Context context) {
        Intent intent = new Intent(Actions.DEVICE_SET_STATE);
        intent.putExtra(BundleExtraKeys.DEVICE_NAME, device.getName());
        intent.putExtra(BundleExtraKeys.DEVICE_TARGET_STATE, newState);
        intent.putExtra(BundleExtraKeys.RESULT_RECEIVER, new ResultReceiver(new Handler()) {
            @Override
            protected void onReceiveResult(int resultCode, Bundle resultData) {
                super.onReceiveResult(resultCode, resultData);
                context.sendBroadcast(new Intent(Actions.DO_UPDATE));
            }
        });
        context.startService(intent);
    }

    private void fillFS20ToggleView(View view, final FS20Device child) {
        setTextView(view, R.id.deviceName, child.getAliasOrName());

        ToggleButton switchButton = (ToggleButton) view.findViewById(R.id.switchButton);
        switchButton.setChecked(child.isOn());
        switchButton.setOnClickListener(new SwitchButtonListener(child.getName()));
    }

    private void fillFS20SeekView(View view, final FS20Device child) {
        setTextView(view, R.id.deviceName, child.getAliasOrName());

        SeekBar seekBar = (SeekBar) view.findViewById(R.id.seekBar);
        final int initialProgress = child.getFS20DimState();
        seekBar.setProgress(initialProgress);
        seekBar.setTag(child.getName());

        seekBar.setOnSeekBarChangeListener(new SeekBarChangeListener(child.getFS20DimState()));
    }

    @Override
    public int getDetailViewLayout() {
        return R.layout.device_detail_fs20;
    }

    @Override
    public Class<? extends Device> getSupportedDeviceClass() {
        return FS20Device.class;
    }
}
