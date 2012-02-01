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

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.view.View;
import android.widget.SeekBar;
import android.widget.ToggleButton;
import li.klass.fhem.AndFHEMApplication;
import li.klass.fhem.R;
import li.klass.fhem.adapter.devices.core.DeviceDetailAvailableAdapter;
import li.klass.fhem.constants.Actions;
import li.klass.fhem.constants.BundleExtraKeys;
import li.klass.fhem.domain.CULHMDevice;
import li.klass.fhem.domain.Device;

public class CULHMAdapter extends DeviceDetailAvailableAdapter<CULHMDevice> {

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

            context.startService(intent);
        }
    }

    @Override
    public Class<? extends Device> getSupportedDeviceClass() {
        return CULHMDevice.class;
    }

    @Override
    public int getOverviewLayout(CULHMDevice device) {
        switch (device.getSubType()) {
            case DIMMER:
                return R.layout.room_detail_culhm_seek;
            case SWITCH:
                return R.layout.room_detail_culhm_switch;
            case HEATING:
                return R.layout.room_detail_culhm_heating;
            case SMOKE_DETECTOR:
                return R.layout.room_detail_culhm_smoke;
            case THREE_STATE:
                return R.layout.room_detail_culhm_threestate;
            default:
                return android.R.layout.simple_list_item_1;
        }
    }

    @Override
    protected void fillDeviceOverviewView(View view, CULHMDevice device) {
        switch (device.getSubType()) {
            case DIMMER:
                fillDimOverview(view, device);
                break;
            case SWITCH:
                fillSwitchOverview(view, device);
                break;
            case HEATING:
                fillHeatingOverview(view, device);
                break;
            case SMOKE_DETECTOR:
                fillSmokeDetectorOverview(view, device);
                break;
            case THREE_STATE:
                fillThreeStateOverview(view, device);
                break;
        }

        setTextView(view, R.id.deviceName, device.getAliasOrName());
    }

    private void fillThreeStateOverview(View view, CULHMDevice device) {
        setTextViewOrHideTableRow(view, R.id.tableRowState, R.id.state, device.getState());
    }

    private void fillSmokeDetectorOverview(View view, CULHMDevice device) {
        setTextViewOrHideTableRow(view, R.id.tableRowState, R.id.state, device.getState());
    }

    private void fillSwitchOverview(View view, final CULHMDevice device) {
        ToggleButton button = (ToggleButton) view.findViewById(R.id.switchButton);
        button.setChecked(device.isOn());
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(Actions.DEVICE_TOGGLE_STATE);
                intent.putExtra(BundleExtraKeys.DEVICE_NAME, device.getName());
                intent.putExtra(BundleExtraKeys.RESULT_RECEIVER, new ResultReceiver(new Handler()) {
                    @Override
                    protected void onReceiveResult(int resultCode, Bundle resultData) {
                        AndFHEMApplication.getContext().sendBroadcast(new Intent(Actions.DO_UPDATE));
                    }
                });

                AndFHEMApplication.getContext().startService(intent);
            }
        });
    }

    private void fillDimOverview(View view, CULHMDevice device) {
        SeekBar seekBar = (SeekBar) view.findViewById(R.id.seekBar);
        final int initialProgress = device.getDimProgress();
        seekBar.setProgress(initialProgress);
        seekBar.setTag(device.getName());

        seekBar.setOnSeekBarChangeListener(new SeekBarChangeListener(device.getDimProgress()));
    }


    private void fillHeatingOverview(View view, CULHMDevice device) {
        setTextViewOrHideTableRow(view, R.id.tableRowActuator, R.id.actuator, device.getActuator());
        setTextViewOrHideTableRow(view, R.id.tableRowTemperature, R.id.temperature, device.getMeasuredTemp());
    }

    @Override
    public boolean supportsDetailView(Device device) {
        CULHMDevice culhmDevice = (CULHMDevice) device;
        return culhmDevice.getSubType() == CULHMDevice.SubType.HEATING;
    }

    @Override
    public int getDetailViewLayout() {
        return R.layout.device_detail_culhm_heating;
    }


    @Override
    protected void fillDeviceDetailView(Context context, View view, CULHMDevice device) {
        setTextViewOrHideTableRow(view, R.id.tableRowActuator, R.id.actuator, device.getActuator());
        setTextViewOrHideTableRow(view, R.id.tableRowTemperature, R.id.temperature, device.getMeasuredTemp());
        setTextViewOrHideTableRow(view, R.id.tableRowDesiredTemperature, R.id.desiredTemperature, device.getDesiredTemp());
        setTextViewOrHideTableRow(view, R.id.tableRowHumidity, R.id.humidity, device.getHumidity());
    }
}
