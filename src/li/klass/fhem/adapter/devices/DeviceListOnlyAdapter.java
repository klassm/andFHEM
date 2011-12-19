package li.klass.fhem.adapter.devices;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import li.klass.fhem.domain.Device;

public abstract class DeviceListOnlyAdapter<D extends Device> extends DeviceAdapter<D> {

    public int getDetailViewLayout() {
        return -1;
    }

    public boolean supportsDetailView() {
        return false;
    }

    public Class<? extends Activity> getDetailActivityClass() {
        return null;
    }

    protected View getDeviceDetailView(LayoutInflater layoutInflater, D device) {
        return null;
    }
}
