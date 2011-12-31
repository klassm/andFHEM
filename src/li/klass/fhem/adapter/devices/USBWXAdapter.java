package li.klass.fhem.adapter.devices;

import android.view.LayoutInflater;
import android.view.View;
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

        setTextView(view, R.id.deviceName, device.getAliasOrName());
        setTextViewOrHideTableRow(view, R.id.tableRowTemperature, R.id.temperature, device.getTemperature());
        setTextViewOrHideTableRow(view, R.id.tableRowHumidity, R.id.humidity, device.getHumidity());
        setTextViewOrHideTableRow(view, R.id.tableRowDewpoint, R.id.dewpoint, device.getDewpoint());

        return view;
    }

}
