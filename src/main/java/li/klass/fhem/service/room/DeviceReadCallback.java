package li.klass.fhem.service.room;

import java.util.Map;

import li.klass.fhem.domain.core.FhemDevice;

public abstract class DeviceReadCallback<D extends FhemDevice> extends AllDevicesReadCallback {
    private String deviceName;

    public DeviceReadCallback(String callbackDevice) {
        this.deviceName = callbackDevice;
    }

    @SuppressWarnings("unchecked")
    public void devicesRead(Map<String, FhemDevice> allDevices) {
        onCallbackDeviceRead((D) allDevices.get(deviceName));
    }

    public abstract void onCallbackDeviceRead(D callbackDevice);
}
