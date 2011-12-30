package li.klass.fhem.adapter.devices;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import li.klass.fhem.R;
import li.klass.fhem.domain.Device;

public abstract class DeviceAdapter<D extends Device> {


    public static final String INTENT_DEVICE_NAME = "deviceName";
    public static final String INTENT_ROOM = "room";

    public boolean supports(Class<? extends Device> deviceClass) {
        return getSupportedDeviceClass().isAssignableFrom(deviceClass);
    }

    @SuppressWarnings("unchecked")
    public View getView(LayoutInflater layoutInflater, View convertView, Device device) {
        if (convertView != null) {
            TextView deviceName = (TextView) convertView.findViewById(R.id.deviceName);
            if (deviceName != null && deviceName.getText().equals(device.getAliasOrName())) {
                return convertView;
            }
        }
        return getDeviceView(layoutInflater, (D) device);
    }

    @SuppressWarnings("unchecked")
    public View getDetailView(Context context, LayoutInflater layoutInflater, Device device) {
        if (supportsDetailView()) {
            return getDeviceDetailView(context, layoutInflater, (D) device);
        }
        return null;
    }

    public void gotoDetailView(Context context, Device device) {
        if (! supportsDetailView()) {
            return;
        }

        Intent intent = new Intent();
        intent.putExtras(new Bundle());
        intent.putExtra(INTENT_DEVICE_NAME, device.getName());
        intent.putExtra(INTENT_ROOM, device.getRoom());

        intent = onFillDeviceDetailIntent(context, device, intent);
        if (intent != null) {
            context.startActivity(intent);
        }
    }


    public abstract int getDetailViewLayout();
    public abstract boolean supportsDetailView();
    protected abstract View getDeviceDetailView(Context context, LayoutInflater layoutInflater, D device);
    protected abstract Intent onFillDeviceDetailIntent(Context context, Device device, Intent intent);

    public abstract Class<? extends Device> getSupportedDeviceClass();
    protected abstract View getDeviceView(LayoutInflater layoutInflater, D device);

}
