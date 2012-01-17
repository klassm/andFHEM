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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import li.klass.fhem.R;
import li.klass.fhem.activities.deviceDetail.FHTDeviceDetailActivity;
import li.klass.fhem.activities.fhtControl.FHTTimetableControlListActivity;
import li.klass.fhem.adapter.devices.core.DeviceDetailAvailableAdapter;
import li.klass.fhem.constants.Actions;
import li.klass.fhem.constants.BundleExtraKeys;
import li.klass.fhem.domain.Device;
import li.klass.fhem.domain.FHTDevice;
import li.klass.fhem.domain.fht.FHTMode;
import li.klass.fhem.service.graph.ChartSeriesDescription;

import static li.klass.fhem.domain.FHTDevice.*;

public class FHTAdapter extends DeviceDetailAvailableAdapter<FHTDevice> {

    private interface TemperatureValueSeekBarChangeListener {
        void onSeekBarValueChanged(double newTemperature);
    }

    private abstract class TemperatureViewSeekBarChangeListener implements SeekBar.OnSeekBarChangeListener {
        protected double value;
        protected View view;
        protected int updateTextFieldId;
        private int tableRowUpdateTextFieldId;

        protected TemperatureViewSeekBarChangeListener(View view, int tableRowUpdateTextFieldId, int updateTextFieldId) {
            this.view = view;
            this.updateTextFieldId = updateTextFieldId;
            this.tableRowUpdateTextFieldId = tableRowUpdateTextFieldId;
        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean byUser) {
            this.value = 5.5 + (progress * 0.5);
            setTextViewOrHideTableRow(view, tableRowUpdateTextFieldId, updateTextFieldId, temperatureToString(value));
        }
        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }
    }
    
    @Override
    public View getOverviewView(LayoutInflater layoutInflater, FHTDevice device) {
        View view = layoutInflater.inflate(R.layout.room_detail_fht, null);

        setTextView(view, R.id.deviceName, device.getAliasOrName());
        setTextViewOrHideTableRow(view, R.id.tableRowTemperature, R.id.temperature, device.getTemperature());
        setTextViewOrHideTableRow(view, R.id.tableRowActuator, R.id.actuator, device.getActuator());

        return view;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void fillDeviceDetailView(final Context context, final View view, final FHTDevice device) {

        setTextViewOrHideTableRow(view, R.id.tableRowTemperature, R.id.temperature, device.getTemperature());
        setTextViewOrHideTableRow(view, R.id.tableRowActuator, R.id.actuator, device.getActuator());
        setTextViewOrHideTableRow(view, R.id.tableRowDesiredTemperature, R.id.desiredTemperature, device.getDesiredTempDesc());
        setTextViewOrHideTableRow(view, R.id.tableRowDayTemp, R.id.dayTemperature, device.getDayTemperatureDesc());
        setTextViewOrHideTableRow(view, R.id.tableRowNightTemp, R.id.nightTemperature, device.getNightTemperatureDesc());
        setTextViewOrHideTableRow(view, R.id.tableRowWindowOpenTemp, R.id.windowOpenTemp, device.getWindowOpenTempDesc());
        setTextViewOrHideTableRow(view, R.id.tableRowWarnings, R.id.warnings, device.getWarnings());

        createSeekBar(view, R.id.desiredTemperatureSeek, R.id.tableRowDesiredTemperature, R.id.desiredTemperature, device.getDesiredTemp(),
                new TemperatureValueSeekBarChangeListener() {
            @Override
            public void onSeekBarValueChanged(double newTemperature) {
                String action = Actions.DEVICE_SET_DESIRED_TEMPERATURE;
                sendTemperatureIntent(newTemperature, action, device, context);
            }
        });

        createSeekBar(view, R.id.dayTemperatureSeek, R.id.tableRowDayTemp, R.id.dayTemperature, device.getDayTemperature(),
                new TemperatureValueSeekBarChangeListener() {
            @Override
            public void onSeekBarValueChanged(double newTemperature) {
                String action = Actions.DEVICE_SET_DAY_TEMPERATURE;
                sendTemperatureIntent(newTemperature, action, device, context);
            }
        });

        createSeekBar(view, R.id.nightTemperatureSeek, R.id.tableRowNightTemp, R.id.nightTemperature, device.getNightTemperature(),
                new TemperatureValueSeekBarChangeListener() {
            @Override
            public void onSeekBarValueChanged(double newTemperature) {
                String action = Actions.DEVICE_SET_NIGHT_TEMPERATURE;
                sendTemperatureIntent(newTemperature, action, device, context);
            }
        });

        createSeekBar(view, R.id.windowOpenTempSeek, R.id.tableRowWindowOpenTemp, R.id.windowOpenTemp, device.getWindowOpenTemp(),
                new TemperatureValueSeekBarChangeListener() {
            @Override
            public void onSeekBarValueChanged(double newTemperature) {
                String action = Actions.DEVICE_SET_WINDOW_OPEN_TEMPERATURE;
                sendTemperatureIntent(newTemperature, action, device, context);
            }
        });

        Spinner modeSpinner = (Spinner) view.findViewById(R.id.mode);

        ArrayAdapter adapter = new ArrayAdapter(context, R.layout.spinnercontent, FHTMode.values());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        modeSpinner.setAdapter(adapter);
        modeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                FHTMode mode = FHTMode.values()[position];
                Intent intent = new Intent(Actions.DEVICE_SET_MODE);
                intent.putExtra(BundleExtraKeys.DEVICE_NAME, device.getName());
                intent.putExtra(BundleExtraKeys.DEVICE_MODE, mode);
                context.startService(intent);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
        modeSpinner.setSelection(FHTMode.positionOf(device.getMode()));

        createPlotButton(context, view, R.id.temperatureGraph, device.getTemperature(),
                device, R.string.yAxisTemperature, new ChartSeriesDescription(COLUMN_SPEC_DESIRED_TEMPERATURE, true),
                new ChartSeriesDescription(COLUMN_SPEC_TEMPERATURE, false));

        createPlotButton(context, view, R.id.actuatorGraph, device.getActuator(),
                device, R.string.yAxisActuator, COLUMN_SPEC_ACTUATOR);

        Button timetableButton = (Button) view.findViewById(R.id.timetableButton);
        timetableButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, FHTTimetableControlListActivity.class);
                intent.putExtras(new Bundle());
                intent.putExtra(BundleExtraKeys.DEVICE_NAME, device.getName());
                context.startActivity(intent);
            }
        });
    }

    private void sendTemperatureIntent(double newTemperature, String action, FHTDevice device, Context context) {
        Intent intent = new Intent(action);
        intent.putExtra(BundleExtraKeys.DEVICE_NAME, device.getName());
        intent.putExtra(BundleExtraKeys.DEVICE_TEMPERATURE, newTemperature);
        context.startService(intent);
    }

    @Override
    public Class<? extends Device> getSupportedDeviceClass() {
        return FHTDevice.class;
    }

    @Override
    public int getDetailViewLayout() {
        return R.layout.device_detail_fht;
    }

    @Override
    protected Class<? extends Activity> getDeviceDetailActivity() {
        return FHTDeviceDetailActivity.class;
    }

    private void createSeekBar(View view, int seekBarLayoutId, int tableRowUpdateTextFieldId, int updateTextFieldId,
                               double initialTemperature, final TemperatureValueSeekBarChangeListener listener) {
        final SeekBar desiredTempSeekBar = (SeekBar) view.findViewById(seekBarLayoutId);
        desiredTempSeekBar.setProgress((int) ((initialTemperature - 5.5) / 0.5));
        desiredTempSeekBar.setOnSeekBarChangeListener(new TemperatureViewSeekBarChangeListener(view, tableRowUpdateTextFieldId, updateTextFieldId) {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                listener.onSeekBarValueChanged(value);
            }
        });

    }
}
