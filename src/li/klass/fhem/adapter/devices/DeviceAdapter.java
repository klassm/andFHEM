package li.klass.fhem.adapter.devices;

import android.view.LayoutInflater;
import android.view.View;
import li.klass.fhem.domain.Device;

public abstract class DeviceAdapter<D extends Device> {
    public boolean supports(Class<? extends Device> deviceClass) {
        return getSupportedDeviceClass().isAssignableFrom(deviceClass);
    }

    @SuppressWarnings("unchecked")
    public View getView(LayoutInflater layoutInflater, Device device) {
        return getDeviceView(layoutInflater, (D) device);
    }

    public abstract Class<? extends Device> getSupportedDeviceClass();
    protected abstract View getDeviceView(LayoutInflater layoutInflater, D device);
}
