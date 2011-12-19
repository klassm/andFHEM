package li.klass.fhem.adapter.devices;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import li.klass.fhem.R;
import li.klass.fhem.domain.Device;
import li.klass.fhem.domain.FHTDevice;

public class FHTAdapter extends DeviceListOnlyAdapter<FHTDevice> {
    @Override
    public View getDeviceView(LayoutInflater layoutInflater, FHTDevice device) {
        View view = layoutInflater.inflate(R.layout.room_detail_fht, null);

        TextView deviceName = (TextView) view.findViewById(R.id.deviceName);
        TextView temperature = (TextView) view.findViewById(R.id.temperature);
        TextView actuator = (TextView) view.findViewById(R.id.actuator);

        deviceName.setText(device.getName());
        temperature.setText(device.getTemperature());
        actuator.setText(device.getActuator());

        return view;
    }

    @Override
    protected View getDeviceDetailView(LayoutInflater layoutInflater, FHTDevice device) {
        return null;
    }

    @Override
    public Class<? extends Device> getSupportedDeviceClass() {
        return FHTDevice.class;
    }
}
