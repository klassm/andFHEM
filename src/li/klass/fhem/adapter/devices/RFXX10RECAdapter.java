package li.klass.fhem.adapter.devices;

import android.view.LayoutInflater;
import android.view.View;
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

        setTextView(view, R.id.deviceName, device.getAliasOrName());
        setTextViewOrHideTableRow(view, R.id.tableRowState, R.id.state, device.getState());
        setTextViewOrHideTableRow(view, R.id.tableRowMeasured, R.id.measured, device.getMeasured());

        return view;
    }
}
