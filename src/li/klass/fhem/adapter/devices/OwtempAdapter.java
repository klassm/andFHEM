package li.klass.fhem.adapter.devices;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import li.klass.fhem.R;
import li.klass.fhem.domain.Device;
import li.klass.fhem.domain.OwtempDevice;

public class OwtempAdapter extends DeviceAdapter<OwtempDevice> {
    @Override
    public View getDeviceView(LayoutInflater layoutInflater, OwtempDevice device) {
        View view = layoutInflater.inflate(R.layout.room_detail_owtemp, null);

        TextView deviceName = (TextView) view.findViewById(R.id.deviceName);
        deviceName.setText(device.getName());

        TextView temperature = (TextView) view.findViewById(R.id.temperature);
        temperature.setText(device.getTemperature());

        TextView battery = (TextView) view.findViewById(R.id.warnings);
        battery.setText(device.getWarnings());

        return view;
    }

    @Override
    public Class<? extends Device> getSupportedDeviceClass() {
        return OwtempDevice.class;
    }
}
