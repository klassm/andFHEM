package li.klass.fhem.adapter.devices;

import android.view.LayoutInflater;
import android.view.View;
import li.klass.fhem.R;
import li.klass.fhem.domain.Device;
import li.klass.fhem.domain.OwtempDevice;

public class OwtempAdapter extends DeviceListOnlyAdapter<OwtempDevice> {
    @Override
    public View getDeviceView(LayoutInflater layoutInflater, OwtempDevice device) {
        View view = layoutInflater.inflate(R.layout.room_detail_owtemp, null);

        setTextView(view, R.id.deviceName, device.getAliasOrName());
        setTextViewOrHideTableRow(view, R.id.tableRowTemperature, R.id.temperature, device.getTemperature());
        setTextViewOrHideTableRow(view, R.id.tableRowWarnings, R.id.warnings, device.getWarnings());

        return view;
    }

    @Override
    public Class<? extends Device> getSupportedDeviceClass() {
        return OwtempDevice.class;
    }

}
