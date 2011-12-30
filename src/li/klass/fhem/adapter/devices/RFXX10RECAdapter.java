package li.klass.fhem.adapter.devices;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import li.klass.fhem.R;
import li.klass.fhem.domain.Device;
import li.klass.fhem.domain.RFXX10RECDevice;

public class RFXX10RECAdapter extends DeviceListOnlyAdapter<RFXX10RECDevice> {
    @Override
    public Class<? extends Device> getSupportedDeviceClass() {
        return RFXX10RECDevice.class;
    }

    @Override
    protected View getDeviceView(LayoutInflater layoutInflater, RFXX10RECDevice device) {
        View view = layoutInflater.inflate(R.layout.room_detail_rfxx10rec, null);

        TextView deviceName = (TextView) view.findViewById(R.id.deviceName);
        deviceName.setText(device.getAliasOrName());

        TextView state = (TextView) view.findViewById(R.id.state);
        state.setText(device.getState());

        TextView measured = (TextView) view.findViewById(R.id.measured);
        measured.setText(device.getMeasured());

        return view;
    }
}
