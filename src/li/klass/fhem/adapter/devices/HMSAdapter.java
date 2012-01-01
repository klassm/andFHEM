package li.klass.fhem.adapter.devices;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import li.klass.fhem.R;
import li.klass.fhem.activities.deviceDetail.HMSDeviceDetailActivity;
import li.klass.fhem.adapter.devices.core.DeviceDetailAvailableAdapter;
import li.klass.fhem.domain.Device;
import li.klass.fhem.domain.HMSDevice;

public class HMSAdapter extends DeviceDetailAvailableAdapter<HMSDevice> {
    @Override
    public View getDeviceView(LayoutInflater layoutInflater, HMSDevice device) {
        View view = layoutInflater.inflate(R.layout.room_detail_hms, null);

        setTextView(view, R.id.deviceName, device.getAliasOrName());
        setTextViewOrHideTableRow(view, R.id.tableRowTemperature, R.id.temperature, device.getTemperature());
        setTextViewOrHideTableRow(view, R.id.tableRowHumidity, R.id.humidity, device.getHumidity());

        return view;
    }

    @Override
    protected void fillDeviceDetailView(final Context context, View view, final HMSDevice device) {

        setTextViewOrHideTableRow(view, R.id.tableRowTemperature, R.id.temperature, device.getTemperature());
        setTextViewOrHideTableRow(view, R.id.tableRowHumidity, R.id.humidity, device.getHumidity());
        setTextViewOrHideTableRow(view, R.id.tableRowBattery, R.id.battery, device.getBattery());

        createPlotButton(context, view, R.id.temperatureGraph, device.getTemperature(),
                device, R.string.yAxisTemperature, HMSDevice.COLUMN_SPEC_TEMPERATURE);

        createPlotButton(context, view, R.id.humidityGraph, device.getHumidity(),
                device, R.string.yAxisHumidity, HMSDevice.COLUMN_SPEC_HUMIDITY);
    }

    @Override
    public int getDetailViewLayout() {
        return R.layout.device_detail_hms;
    }


    @Override
    protected Class<? extends Activity> getDeviceDetailActivity() {
        return HMSDeviceDetailActivity.class;
    }

    @Override
    public Class<? extends Device> getSupportedDeviceClass() {
        return HMSDevice.class;
    }
}
