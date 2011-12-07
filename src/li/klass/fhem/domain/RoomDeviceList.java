package li.klass.fhem.domain;

import android.util.Log;

import java.util.*;

public class RoomDeviceList {
    public static final String FS_20 = "FS20";
    public static final String KS_300 = "KS300";

    private String roomName;
    private Map<String, ArrayList<? extends Device>> deviceMap = new HashMap<String, ArrayList<? extends Device>>();

    public RoomDeviceList(String roomName) {
        this.roomName = roomName;
    }

    public List<FS20Device> getFs20Devices() {
        return getOrCreateDeviceList(FS_20);
    }


    public List<KS300Device> getKS300Devices() {
        return getOrCreateDeviceList(KS_300);
    }

    public void addFS20Device(FS20Device fs20Device) {
        getFs20Devices().add(fs20Device);
    }

    public void addKS300Device(KS300Device ks300Device) {
        Log.e(RoomDeviceList.class.getName(), ks300Device.toString());
        getKS300Devices().add(ks300Device);
        Log.e(RoomDeviceList.class.getName(), getKS300Devices().get(0).toString());
    }

    @SuppressWarnings("unchecked")
    public <T extends Device> List<T> getDevicesForType(String type) {
        if (deviceMap.containsKey(type)) {
            return (List<T>) deviceMap.get(type);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private <T extends Device> List<T> getOrCreateDeviceList(String typeName) {
        if (! deviceMap.containsKey(typeName)) {
            deviceMap.put(typeName, new ArrayList<Device>());
        }
        return (List<T>) deviceMap.get(typeName);
    }

    public String getRoomName() {
        return roomName;
    }
}
