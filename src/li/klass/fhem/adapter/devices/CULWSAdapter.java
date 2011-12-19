package li.klass.fhem.adapter.devices;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import li.klass.fhem.R;
import li.klass.fhem.domain.CULWSDevice;
import li.klass.fhem.domain.Device;

public class CULWSAdapter extends DeviceListOnlyAdapter<CULWSDevice> {

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
    public Class<? extends Device> getSupportedDeviceClass() {
        return CULWSDevice.class;
    }
}
