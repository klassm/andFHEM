package li.klass.fhem.adapter.devices;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import li.klass.fhem.R;
import li.klass.fhem.activities.deviceDetail.CULWSDeviceDetailActivity;
import li.klass.fhem.adapter.devices.core.DeviceDetailAvailableAdapter;
import li.klass.fhem.domain.CULWSDevice;
import li.klass.fhem.domain.Device;

public class CULWSAdapter extends DeviceDetailAvailableAdapter<CULWSDevice> {

    @Override
    public View getDeviceView(LayoutInflater layoutInflater, CULWSDevice device) {
        View view = layoutInflater.inflate(R.layout.room_detail_culws, null);

        setTextView(view, R.id.deviceName, device.getAliasOrName());
        setTextViewOrHideTableRow(view, R.id.tableRowTemperature, R.id.temperature, device.getTemperature());
        setTextViewOrHideTableRow(view, R.id.tableRowHumidity, R.id.humidity, device.getHumidity());

        return view;
    }

    @Override
    public int getDetailViewLayout() {
        return R.layout.device_detail_culws;
    }

    @Override
    protected void fillDeviceDetailView(Context context, View view, CULWSDevice device) {

        setTextViewOrHideTableRow(view, R.id.tableRowTemperature, R.id.temperature, device.getTemperature());
        setTextViewOrHideTableRow(view, R.id.tableRowHumidity, R.id.humidity, device.getHumidity());

        createPlotButton(context, view, R.id.temperatureGraph, device.getTemperature(),
                device, R.string.yAxisTemperature, CULWSDevice.COLUMN_SPEC_TEMPERATURE);
        createPlotButton(context, view, R.id.humidityGraph, device.getHumidity(),
                device, R.string.yAxisHumidity, CULWSDevice.COLUMN_SPEC_HUMIDITY);
    }

    @Override
    public Class<? extends Device> getSupportedDeviceClass() {
        return CULWSDevice.class;
    }

    @Override
    protected Class<? extends Activity> getDeviceDetailActivity() {
        return CULWSDeviceDetailActivity.class;
    }
}
