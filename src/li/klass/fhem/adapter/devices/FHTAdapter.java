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
import li.klass.fhem.domain.Device;
import li.klass.fhem.domain.FHTDevice;
import li.klass.fhem.domain.fht.FHTMode;
import li.klass.fhem.service.device.FHTService;

import static li.klass.fhem.domain.FHTDevice.*;

public class FHTAdapter extends DeviceDetailAvailableAdapter<FHTDevice> {
    @Override
    public View getDeviceView(LayoutInflater layoutInflater, FHTDevice device) {
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
        setTextViewOrHideTableRow(view, R.id.tableRowWarnings, R.id.warnings, device.getWarningsDesc());

        final SeekBar desiredTempSeekBar = (SeekBar) view.findViewById(R.id.desiredTemperatureSeek);
        desiredTempSeekBar.setProgress((int) ((device.getDesiredTemp() - 5.5) / 0.5));
        desiredTempSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            private double value;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean byUser) {
                this.value = 5.5 + (progress * 0.5);
                setTextViewOrHideTableRow(view, R.id.tableRowDesiredTemperature, R.id.desiredTemperature, desiredTemperatureToString(value));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                FHTService.INSTANCE.setDesiredTemperature(seekBar.getContext(), device, value);
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
                FHTService.INSTANCE.setMode(context, device, mode);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        createPlotButton(context, view, R.id.temperatureGraph, device.getTemperature(),
                device, R.string.yAxisTemperature, COLUMN_SPEC_TEMPERATURE);

        createPlotButton(context, view, R.id.actuatorGraph, device.getActuator(),
                device, R.string.yAxisActuator, COLUMN_SPEC_ACTUATOR);

        Button timetableButton = (Button) view.findViewById(R.id.timetableButton);
        timetableButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, FHTTimetableControlListActivity.class);
                intent.putExtras(new Bundle());
                intent.putExtra("deviceName", device.getName());
                context.startActivity(intent);
            }
        });
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
}
