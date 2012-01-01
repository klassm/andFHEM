package li.klass.fhem.adapter.devices;

import android.view.LayoutInflater;
import android.view.View;
import li.klass.fhem.R;
import li.klass.fhem.adapter.devices.core.DeviceListOnlyAdapter;
import li.klass.fhem.domain.Device;
import li.klass.fhem.domain.LGTVDevice;

public class LGTVAdapter extends DeviceListOnlyAdapter<LGTVDevice> {
    @Override
    public Class<? extends Device> getSupportedDeviceClass() {
        return LGTVDevice.class;
    }

    @Override
    protected View getDeviceView(LayoutInflater layoutInflater, LGTVDevice device) {
        View view = layoutInflater.inflate(R.layout.room_detail_lgtv, null);

        setTextView(view, R.id.deviceName, device.getAliasOrName());
        setTextViewOrHideTableRow(view, R.id.tableRowPower, R.id.power, device.getPower());
        setTextViewOrHideTableRow(view, R.id.tableRowAudio, R.id.audio, device.getAudio());
        setTextViewOrHideTableRow(view, R.id.tableRowInput, R.id.input, device.getInput());

        return view;
    }
}
