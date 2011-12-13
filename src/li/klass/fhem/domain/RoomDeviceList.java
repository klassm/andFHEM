package li.klass.fhem.domain;

import android.content.Context;
import android.preference.PreferenceManager;
import li.klass.fhem.AndFHEMApplication;

import java.io.Serializable;
import java.util.*;

public class RoomDeviceList implements Serializable {

    private String roomName;
    private Map<DeviceType, HashSet<Device>> deviceMap = new HashMap<DeviceType, HashSet<Device>>();

    public RoomDeviceList(String roomName) {
        this.roomName = roomName;
    }

    public <T extends Device> Collection<T> getDevicesOfType(DeviceType type) {
        Set<T> deviceSet = getOrCreateDeviceList(type);
        List<T> deviceList = new ArrayList<T>(deviceSet);
        Collections.sort(deviceList);
        return deviceList;
    }

    public <T extends Device> void addDevice(DeviceType type, T device) {
        if (device == null) return;
        getOrCreateDeviceList(type).add(device);
    }

    public void addRoomDeviceList(RoomDeviceList roomDeviceList) {
        Context context = AndFHEMApplication.getContext();
        boolean showHiddenDevices = PreferenceManager.getDefaultSharedPreferences(context).getBoolean("prefShowHiddenDevices", false);

        Map<DeviceType, HashSet<Device>> inputMap = roomDeviceList.deviceMap;

        for (DeviceType deviceType : inputMap.keySet()) {
            Set<Device> inputValue = inputMap.get(deviceType);
            for (Device device : inputValue) {
                boolean isHiddenDevice = device.isHiddenDevice() && ! showHiddenDevices;
                Set<Device> devices = getOrCreateDeviceList(deviceType);
                if (! devices.contains(device) && ! isHiddenDevice) {
                    devices.add(device);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private <T extends Device> Set<T> getOrCreateDeviceList(DeviceType deviceType) {
        if (! deviceMap.containsKey(deviceType)) {
            deviceMap.put(deviceType, new HashSet<Device>());
        }
        return (Set<T>) deviceMap.get(deviceType);
    }
}
