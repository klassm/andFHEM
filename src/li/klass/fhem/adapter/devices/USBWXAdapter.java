package li.klass.fhem.adapter.devices;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import li.klass.fhem.R;
import li.klass.fhem.activities.deviceDetail.USBWXDeviceDetailActivity;
import li.klass.fhem.adapter.devices.core.DeviceDetailAvailableAdapter;
import li.klass.fhem.domain.Device;
import li.klass.fhem.domain.USBWXDevice;

public class USBWXAdapter extends DeviceDetailAvailableAdapter<USBWXDevice> {
    @Override
    protected View getDeviceView(LayoutInflater layoutInflater, USBWXDevice device) {

        View view = layoutInflater.inflate(R.layout.room_detail_usbwx, null);

        setTextView(view, R.id.deviceName, device.getAliasOrName());
        setTextViewOrHideTableRow(view, R.id.tableRowTemperature, R.id.temperature, device.getTemperature());
        setTextViewOrHideTableRow(view, R.id.tableRowHumidity, R.id.humidity, device.getHumidity());
        setTextViewOrHideTableRow(view, R.id.tableRowDewpoint, R.id.dewpoint, device.getDewpoint());

        return view;
    }

    @Override
    protected void fillDeviceDetailView(Context context, View view, USBWXDevice device) {
        setTextViewOrHideTableRow(view, R.id.tableRowTemperature, R.id.temperature, device.getTemperature());
        setTextViewOrHideTableRow(view, R.id.tableRowHumidity, R.id.humidity, device.getHumidity());
        setTextViewOrHideTableRow(view, R.id.tableRowDewpoint, R.id.dewpoint, device.getDewpoint());

        hideIfNull(view, R.id.graphLayout, device.getFileLog());

        createPlotButton(context, view, R.id.temperatureGraph, device.getTemperature(),
                device, R.string.yAxisTemperature, USBWXDevice.COLUMN_SPEC_TEMPERATURE);

        createPlotButton(context, view, R.id.humidityGraph, device.getHumidity(),
                device, R.string.yAxisHumidity, USBWXDevice.COLUMN_SPEC_HUMIDITY);
    }

    @Override
    public int getDetailViewLayout() {
        return R.layout.device_detail_usbwx;
    }

    @Override
    protected Class<? extends Activity> getDeviceDetailActivity() {
        return USBWXDeviceDetailActivity.class;
    }

    @Override
    public Class<? extends Device> getSupportedDeviceClass() {
        return USBWXDevice.class;
    }
}
