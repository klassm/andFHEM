package li.klass.fhem.adapter.devices;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import li.klass.fhem.R;
import li.klass.fhem.activities.deviceDetail.HMSDeviceDetailActivity;
import li.klass.fhem.domain.Device;
import li.klass.fhem.domain.HMSDevice;

public class HMSAdapter extends DeviceAdapter<HMSDevice> {
    @Override
    public View getDeviceView(LayoutInflater layoutInflater, HMSDevice device) {
        View view = layoutInflater.inflate(R.layout.room_detail_hms, null);

        setTextView(view, R.id.deviceName, device.getAliasOrName());
        setTextViewOrHideTableRow(view, R.id.tableRowTemperature, R.id.temperature, device.getTemperature());
        setTextViewOrHideTableRow(view, R.id.tableRowHumidity, R.id.humidity, device.getHumidity());

        return view;
    }

    @Override
    public int getDetailViewLayout() {
        return R.layout.device_detail_hms;
    }

    @Override
    public boolean supportsDetailView() {
        return true;
    }

    @Override
    protected View getDeviceDetailView(final Context context, LayoutInflater layoutInflater, final HMSDevice device) {
        View view = layoutInflater.inflate(getDetailViewLayout(), null);

        setTextViewOrHideTableRow(view, R.id.tableRowTemperature, R.id.temperature, device.getTemperature());
        setTextViewOrHideTableRow(view, R.id.tableRowHumidity, R.id.humidity, device.getHumidity());
        setTextViewOrHideTableRow(view, R.id.tableRowBattery, R.id.battery, device.getBattery());

        hideIfNull(view, R.id.graphLayout, device.getFileLog());

        createPlotButton(context, view, R.id.temperatureGraph, device.getTemperature(),
                device, R.string.yAxisTemperature, HMSDevice.COLUMN_SPEC_TEMPERATURE);

        createPlotButton(context, view, R.id.humidityGraph, device.getHumidity(),
                device, R.string.yAxisHumidity, HMSDevice.COLUMN_SPEC_HUMIDITY);

        return view;
    }

    @Override
    protected Intent onFillDeviceDetailIntent(Context context, Device device, Intent intent) {
        intent.setClass(context, HMSDeviceDetailActivity.class);
        return intent;
    }

    @Override
    public Class<? extends Device> getSupportedDeviceClass() {
        return HMSDevice.class;
    }
}
