package li.klass.fhem.service.room;

import li.klass.fhem.domain.core.Device;
import li.klass.fhem.domain.core.RoomDeviceList;

public class AssociatedDeviceCallback {
    private String deviceName;
    private RoomDeviceList allDevicesRoom;

    public AssociatedDeviceCallback(String deviceName, RoomDeviceList allDevicesRoom) {
        this.deviceName = deviceName;
        this.allDevicesRoom = allDevicesRoom;
    }

    public <D extends Device<D>> D getAssociatedDevice() {
        return allDevicesRoom.getDeviceFor(deviceName);
    }
}
