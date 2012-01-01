package li.klass.fhem.domain;

import java.io.Serializable;
import java.util.*;

public class RoomDeviceList implements Serializable {

    private String roomName;
    private boolean containsOnlyLogDevices = true;

    private Map<DeviceType, HashSet<Device>> deviceMap = new HashMap<DeviceType, HashSet<Device>>();
    
    public static final String ALL_DEVICES_ROOM = "ALL_DEVICES_LIST";

    public RoomDeviceList(String roomName) {
        this.roomName = roomName;
    }

    public <T extends Device> Collection<T> getDevicesOfType(DeviceType type) {
        Set<T> deviceSet = getOrCreateDeviceList(type);
        List<T> deviceList = new ArrayList<T>(deviceSet);
        Collections.sort(deviceList);
        return deviceList;
    }

    public <T extends Device> void addDevice(T device) {
        if (device == null) return;
        getOrCreateDeviceList(DeviceType.getDeviceTypeFor(device)).add(device);
        
        if (! (device instanceof FileLog)) {
            containsOnlyLogDevices = false;
        }
    }
    
    public <T extends Device> void removeDevice(T device) {
        deviceMap.get(DeviceType.getDeviceTypeFor(device)).remove(device);
    }
    
    public Set<Device> getAllDevices() {
        Set<Device> devices = new HashSet<Device>();
        Collection<HashSet<Device>> devicesCollection = deviceMap.values();
        for (HashSet<Device> deviceHashSet : devicesCollection) {
            devices.addAll(deviceHashSet);
        }
        return Collections.unmodifiableSet(devices);
    }

    @SuppressWarnings("unchecked")
    public <D extends Device> D getDeviceFor(String deviceName) {
        Set<Device> allDevices = getAllDevices();
        for (Device allDevice : allDevices) {
            if (allDevice.getName().equals(deviceName)) {
                return (D) allDevice;
            }
        }
        return null;
    }

    public boolean isOnlyLogDeviceRoom() {
        return containsOnlyLogDevices;
    }

    @SuppressWarnings("unchecked")
    private <T extends Device> Set<T> getOrCreateDeviceList(DeviceType deviceType) {
        if (! deviceMap.containsKey(deviceType)) {
            deviceMap.put(deviceType, new HashSet<Device>());
        }
        return (Set<T>) deviceMap.get(deviceType);
    }

    public String getRoomName() {
        return roomName;
    }
}
