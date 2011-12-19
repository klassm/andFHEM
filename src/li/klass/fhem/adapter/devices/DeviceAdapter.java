package li.klass.fhem.adapter.devices;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import li.klass.fhem.R;
import li.klass.fhem.domain.Device;

public abstract class DeviceAdapter<D extends Device> {
    public boolean supports(Class<? extends Device> deviceClass) {
        return getSupportedDeviceClass().isAssignableFrom(deviceClass);
    }

    @SuppressWarnings("unchecked")
    public View getView(LayoutInflater layoutInflater, View convertView, Device device) {
        if (convertView != null) {
            TextView deviceName = (TextView) convertView.findViewById(R.id.deviceName);
            if (deviceName != null && deviceName.getText().equals(device.getName())) {
                return convertView;
            }
        }
        return getDeviceView(layoutInflater, (D) device);
    }

    @SuppressWarnings("unchecked")
    public View getDetailView(LayoutInflater layoutInflater, Device device) {
        if (supportsDetailView()) {
            return getDeviceDetailView(layoutInflater, (D) device);
        }
        return null;
    }

    public abstract int getDetailViewLayout();
    public abstract boolean supportsDetailView();
    public abstract Class<? extends Activity> getDetailActivityClass();
    protected abstract View getDeviceDetailView(LayoutInflater layoutInflater, D device);

    public abstract Class<? extends Device> getSupportedDeviceClass();
    protected abstract View getDeviceView(LayoutInflater layoutInflater, D device);
    
}
