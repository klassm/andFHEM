package li.klass.fhem.adapter.devices;

import android.view.LayoutInflater;
import android.view.View;
import li.klass.fhem.R;
import li.klass.fhem.adapter.devices.core.DeviceListOnlyAdapter;
import li.klass.fhem.domain.Device;
import li.klass.fhem.domain.OWFSDevice;

public class OWFSAdapter extends DeviceListOnlyAdapter<OWFSDevice> {
    @Override
    public Class<? extends Device> getSupportedDeviceClass() {
        return OWFSDevice.class;
    }

    @Override
    protected View getDeviceView(LayoutInflater layoutInflater, OWFSDevice device) {
        View view = layoutInflater.inflate(R.layout.room_detail_owfs, null);
        
        setTextView(view, R.id.deviceName, device.getAliasOrName());
        setTextViewOrHideTableRow(view, R.id.tableRowState, R.id.state, device.getState());

        return view;
    }
}
