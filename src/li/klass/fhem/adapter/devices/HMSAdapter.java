package li.klass.fhem.adapter.devices;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import li.klass.fhem.R;
import li.klass.fhem.domain.Device;
import li.klass.fhem.domain.HMSDevice;

public class HMSAdapter extends DeviceListOnlyAdapter<HMSDevice> {
    @Override
    public View getDeviceView(LayoutInflater layoutInflater, HMSDevice device) {
        View view = layoutInflater.inflate(R.layout.room_detail_hms, null);

        TextView deviceName = (TextView) view.findViewById(R.id.deviceName);
        deviceName.setText(device.getName());

        TextView temperature = (TextView) view.findViewById(R.id.temperature);
        temperature.setText(device.getTemperature());

        TextView battery = (TextView) view.findViewById(R.id.battery);
        battery.setText(device.getBattery());

        return view;
    }

    @Override
    public Class<? extends Device> getSupportedDeviceClass() {
        return HMSDevice.class;
    }
}
