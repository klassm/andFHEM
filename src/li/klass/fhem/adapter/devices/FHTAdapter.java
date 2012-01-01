package li.klass.fhem.adapter.devices;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import li.klass.fhem.R;
import li.klass.fhem.activities.deviceDetail.FHTDeviceDetailActivity;
import li.klass.fhem.adapter.devices.core.DeviceDetailAvailableAdapter;
import li.klass.fhem.domain.Device;
import li.klass.fhem.domain.FHTDevice;

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
    protected void fillDeviceDetailView(final Context context, View view, final FHTDevice device) {

        setTextViewOrHideTableRow(view, R.id.tableRowTemperature, R.id.temperature, device.getTemperature());
        setTextViewOrHideTableRow(view, R.id.tableRowActuator, R.id.actuator, device.getActuator());
        setTextViewOrHideTableRow(view, R.id.tableRowDesiredTemperature, R.id.desiredTemperature, device.getDesiredTemp());
        setTextViewOrHideTableRow(view, R.id.tableRowWarnings, R.id.warnings, device.getWarnings());

        createPlotButton(context, view, R.id.temperatureGraph, device.getTemperature(),
                device, R.string.yAxisTemperature, FHTDevice.COLUMN_SPEC_TEMPERATURE);

        createPlotButton(context, view, R.id.actuatorGraph, device.getActuator(),
                device, R.string.yAxisActuator, FHTDevice.COLUMN_SPEC_ACTUATOR);
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
