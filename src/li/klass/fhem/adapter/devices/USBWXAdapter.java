package li.klass.fhem.adapter.devices;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import li.klass.fhem.R;
import li.klass.fhem.domain.Device;
import li.klass.fhem.domain.USBWXDevice;

public class USBWXAdapter extends DeviceListOnlyAdapter<USBWXDevice> {
    @Override
    public Class<? extends Device> getSupportedDeviceClass() {
        return USBWXDevice.class;
    }

    @Override
    protected View getDeviceView(LayoutInflater layoutInflater, USBWXDevice device) {

        View view = layoutInflater.inflate(R.layout.room_detail_usbwx, null);

        TextView deviceName = (TextView) view.findViewById(R.id.deviceName);
        deviceName.setText(device.getAliasOrName());

        TextView temperature = (TextView) view.findViewById(R.id.temperature);
        temperature.setText(device.getTemperature());

        TextView humidity = (TextView) view.findViewById(R.id.humidity);
        humidity.setText(device.getHumidity());

        TextView dewpoint = (TextView) view.findViewById(R.id.dewpoint);
        dewpoint.setText(device.getDewpoint());

        return view;
    }

}
