package li.klass.fhem.adapter.devices;

import android.view.LayoutInflater;
import android.view.View;
import li.klass.fhem.R;
import li.klass.fhem.adapter.devices.core.DeviceListOnlyAdapter;
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

        setTextView(view, R.id.deviceName, device.getAliasOrName());
        setTextViewOrHideTableRow(view, R.id.tableRowState, R.id.state, device.getState());
        setTextViewOrHideTableRow(view, R.id.tableRowMeasured, R.id.measured, device.getMeasured());
        
        return view;
    }
}
