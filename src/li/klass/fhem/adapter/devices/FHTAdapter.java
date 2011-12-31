package li.klass.fhem.adapter.devices;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import li.klass.fhem.R;
import li.klass.fhem.activities.deviceDetail.FHTDeviceDetailActivity;
import li.klass.fhem.domain.Device;
import li.klass.fhem.domain.FHTDevice;

public class FHTAdapter extends DeviceAdapter<FHTDevice> {
    @Override
    public View getDeviceView(LayoutInflater layoutInflater, FHTDevice device) {
        View view = layoutInflater.inflate(R.layout.room_detail_fht, null);

        setTextView(view, R.id.deviceName, device.getAliasOrName());
        setTextViewOrHideTableRow(view, R.id.tableRowTemperature, R.id.temperature, device.getTemperature());
        setTextViewOrHideTableRow(view, R.id.tableRowActuator, R.id.actuator, device.getActuator());

        return view;
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
    public boolean supportsDetailView() {
        return true;
    }

    @Override
    protected View getDeviceDetailView(final Context context, LayoutInflater layoutInflater, final FHTDevice device) {
        View view = layoutInflater.inflate(getDetailViewLayout(), null);

        setTextViewOrHideTableRow(view, R.id.tableRowTemperature, R.id.temperature, device.getTemperature());
        setTextViewOrHideTableRow(view, R.id.tableRowActuator, R.id.actuator, device.getActuator());
        setTextViewOrHideTableRow(view, R.id.tableRowDesiredTemperature, R.id.actuator, device.getDesiredTemp());
        setTextViewOrHideTableRow(view, R.id.tableRowWarnings, R.id.warnings, device.getWarnings());

        hideIfNull(view, R.id.graphLayout, device.getFileLog());

        createPlotButton(context, view, R.id.temperatureGraph, device.getTemperature(),
                device, R.string.yAxisTemperature, FHTDevice.COLUMN_SPEC_TEMPERATURE);

        createPlotButton(context, view, R.id.actuatorGraph, device.getActuator(),
                device, R.string.yAxisActuator, FHTDevice.COLUMN_SPEC_ACTUATOR);

        return view;
    }

    @Override
    protected Intent onFillDeviceDetailIntent(Context context, Device device, Intent intent) {
        intent.setClass(context, FHTDeviceDetailActivity.class);

        return intent;
    }
}
