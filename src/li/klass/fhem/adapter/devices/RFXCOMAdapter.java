package li.klass.fhem.adapter.devices;

import android.view.LayoutInflater;
import android.view.View;
import li.klass.fhem.R;
import li.klass.fhem.adapter.devices.core.DeviceListOnlyAdapter;
import li.klass.fhem.domain.Device;
import li.klass.fhem.domain.RFXCOMDevice;

public class RFXCOMAdapter extends DeviceListOnlyAdapter<RFXCOMDevice> {
    @Override
    public Class<? extends Device> getSupportedDeviceClass() {
        return RFXCOMDevice.class;
    }

    @Override
    protected View getDeviceView(LayoutInflater layoutInflater, RFXCOMDevice device) {
        View view = layoutInflater.inflate(R.layout.room_detail_rfxcom, null);
        
        setTextView(view, R.id.deviceName, device.getAliasOrName());
        setTextViewOrHideTableRow(view, R.id.tableRowState, R.id.state, device.getState());

        return view;
    }
}
