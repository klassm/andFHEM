package li.klass.fhem.adapter.devices;

import android.view.LayoutInflater;
import android.view.View;
import li.klass.fhem.R;
import li.klass.fhem.adapter.devices.core.DeviceListOnlyAdapter;
import li.klass.fhem.domain.Device;
import li.klass.fhem.domain.OwcountDevice;

public class OwcountAdapter extends DeviceListOnlyAdapter<OwcountDevice> {
    @Override
    public Class<? extends Device> getSupportedDeviceClass() {
        return OwcountDevice.class;
    }

    @Override
    protected View getDeviceView(LayoutInflater layoutInflater, OwcountDevice device) {
        View view = layoutInflater.inflate(R.layout.room_detail_owcount, null);

        String counterAState = "";
        if (device.getCounterA() > 0) {
            counterAState = device.getCounterA() + " (" + (device.getCounterA() * device.getCorrelationA()) + ")";
        }

        String counterBState = "";
        if (device.getCounterB() > 0) {
            counterBState = device.getCounterB() + " (" + (device.getCounterB() * device.getCorrelationB()) + ")";
        }
        
        setTextView(view, R.id.deviceName, device.getAliasOrName());
        setTextViewOrHideTableRow(view, R.id.tableRowCounterA, R.id.counterA, counterAState);
        setTextViewOrHideTableRow(view, R.id.tableRowCounterB, R.id.counterB, counterBState);
        setTextViewOrHideTableRow(view, R.id.tableRowPresent, R.id.present, device.getPresent());

        return view;
    }
}
