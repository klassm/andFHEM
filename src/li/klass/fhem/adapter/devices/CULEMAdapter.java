package li.klass.fhem.adapter.devices;

import android.view.LayoutInflater;
import android.view.View;
import li.klass.fhem.R;
import li.klass.fhem.adapter.devices.core.DeviceListOnlyAdapter;
import li.klass.fhem.domain.CULEMDevice;
import li.klass.fhem.domain.Device;

public class CULEMAdapter extends DeviceListOnlyAdapter<CULEMDevice> {
    @Override
    public Class<? extends Device> getSupportedDeviceClass() {
        return CULEMDevice.class;
    }

    @Override
    protected View getDeviceView(LayoutInflater layoutInflater, CULEMDevice device) {
        View view = layoutInflater.inflate(R.layout.room_detail_culem, null);
        
        setTextView(view, R.id.deviceName, device.getAliasOrName());
        setTextViewOrHideTableRow(view, R.id.tableRowCurrentUsage, R.id.currentUsage, device.getCurrentUsage());
        setTextViewOrHideTableRow(view, R.id.tableRowDayUsage, R.id.dayUsage, device.getDayUsage());
        setTextViewOrHideTableRow(view, R.id.tableRowMonthUsage, R.id.monthUsage, device.getMonthUsage());

        return view;
    }
}
