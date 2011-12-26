package li.klass.fhem.adapter.devices;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import li.klass.fhem.R;
import li.klass.fhem.domain.CULWSDevice;
import li.klass.fhem.domain.Device;
import li.klass.fhem.graph.TimePlot;

public class CULWSAdapter extends DeviceAdapter<CULWSDevice> {

    @Override
    public View getDeviceView(LayoutInflater layoutInflater, CULWSDevice device) {
        View view = layoutInflater.inflate(R.layout.room_detail_culws, null);

        TextView deviceName = (TextView) view.findViewById(R.id.deviceName);
        deviceName.setText(device.getName());

        TextView temperature = (TextView) view.findViewById(R.id.temperature);
        temperature.setText(device.getTemperature());

        TextView battery = (TextView) view.findViewById(R.id.humidity);
        battery.setText(device.getHumidity());

        return view;
    }

    @Override
    public int getDetailViewLayout() {
        return 0;
    }

    @Override
    public boolean supportsDetailView() {
        return true;
    }

    @Override
    protected View getDeviceDetailView(Context context, LayoutInflater layoutInflater, CULWSDevice device) {
        return null;
    }

    @Override
    protected Intent onFillDeviceDetailIntent(Context context, Device device, Intent intent) {
        if (device.getFileLog() != null) {
            String yTitle = context.getResources().getString(R.string.yAxisActuator);
            TimePlot.INSTANCE.execute(context, device, yTitle, CULWSDevice.COLUMN_SPEC_TEMPERATURE);
        }
        return null;
    }

    @Override
    public Class<? extends Device> getSupportedDeviceClass() {
        return CULWSDevice.class;
    }
}
