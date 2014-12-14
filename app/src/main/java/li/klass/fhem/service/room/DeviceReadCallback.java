package li.klass.fhem.service.room;

import java.util.Map;

import li.klass.fhem.domain.core.Device;
import li.klass.fhem.domain.core.RoomDeviceList;

public abstract class DeviceReadCallback<D extends Device<D>> extends AllDevicesReadCallback {
    private String deviceName;

    public DeviceReadCallback(String callbackDevice) {
        this.deviceName = callbackDevice;
    }

    @SuppressWarnings("unchecked")
    public void devicesRead(Map<String, Device> allDevices) {
        onCallbackDeviceRead((D) allDevices.get(deviceName));
    }

    public abstract void onCallbackDeviceRead(D callbackDevice);
}
