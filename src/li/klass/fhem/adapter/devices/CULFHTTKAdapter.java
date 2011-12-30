package li.klass.fhem.adapter.devices;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import li.klass.fhem.R;
import li.klass.fhem.domain.CULFHTTKDevice;
import li.klass.fhem.domain.Device;

public class CULFHTTKAdapter extends DeviceListOnlyAdapter<CULFHTTKDevice> {
    @Override
    public Class<? extends Device> getSupportedDeviceClass() {
        return CULFHTTKDevice.class;
    }

    @Override
    protected View getDeviceView(LayoutInflater layoutInflater, CULFHTTKDevice device) {
        View view = layoutInflater.inflate(R.layout.room_detail_culfhttk, null);

        TextView deviceName = (TextView) view.findViewById(R.id.deviceName);
        deviceName.setText(device.getAliasOrName());

        TextView state = (TextView) view.findViewById(R.id.state);
        state.setText(device.getState());
        
        TextView measured = (TextView) view.findViewById(R.id.measured);
        measured.setText(device.getMeasured());

        return view;
    }
}
