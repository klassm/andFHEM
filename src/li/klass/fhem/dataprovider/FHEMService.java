package li.klass.fhem.dataprovider;

import android.content.Context;
import android.util.Log;
import li.klass.fhem.AndFHEMApplication;
import li.klass.fhem.domain.RoomDeviceList;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FHEMService {
    public static final FHEMService INSTANCE = new FHEMService();
    public static final String CACHE_FILENAME = "cache.obj";

    private TelnetFHEM telnetFHEM = TelnetFHEM.INSTANCE;
    private Map<String,RoomDeviceList> deviceListMap;

    private FHEMService() {}

    public List<String> getRoomList(boolean refresh) {
        Map<String, RoomDeviceList> deviceListMap = getRoomDeviceListMap(refresh);
        return new ArrayList<String>(deviceListMap.keySet());
    }


    public RoomDeviceList deviceListForRoom(String roomName, boolean update) {
        Map<String, RoomDeviceList> deviceListMap = getRoomDeviceListMap(update);
        return deviceListMap.get(roomName);
    }

    public RoomDeviceList deviceListForAllRooms(boolean update) {
        Map<String, RoomDeviceList> roomDeviceListMap = getRoomDeviceListMap(update);
        RoomDeviceList deviceList = new RoomDeviceList("ALL_ROOMS");

        for (RoomDeviceList roomDeviceList : roomDeviceListMap.values()) {
            deviceList.addRoomDeviceList(roomDeviceList);
        }

        return deviceList;
    }

    private Map<String, RoomDeviceList> getRoomDeviceListMap(boolean update) {
        if (update) {
            deviceListMap = updateDeviceListMap();
        } else if (deviceListMap == null) {
            deviceListMap = getStoredDataFromFile();
        }
        return deviceListMap;
    }

    public void storeDeviceListMap() {
        cacheRoomDeviceListMap(deviceListMap);
    }

    private Map<String, RoomDeviceList> updateDeviceListMap() {
        Map<String, RoomDeviceList> newList = FHEMDeviceListProvider.INSTANCE.listDevices();
        cacheRoomDeviceListMap(newList);
        return newList;
    }

    public void executeCommand(String command) {
        telnetFHEM.executeCommand(command);
    }

    private void cacheRoomDeviceListMap(Map<String, RoomDeviceList> content) {
        Context context = AndFHEMApplication.getContext();
        try {
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(context.openFileOutput(CACHE_FILENAME, Context.MODE_PRIVATE));
            objectOutputStream.writeObject(content);
        } catch (Exception e) {
            Log.e(FHEMService.class.getName(), "error occurred while serializing data", e);
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, RoomDeviceList> getStoredDataFromFile() {
        try {
            Context context = AndFHEMApplication.getContext();
            ObjectInputStream objectInputStream = new ObjectInputStream(context.openFileInput(CACHE_FILENAME));
            return (Map<String, RoomDeviceList>) objectInputStream.readObject();
        } catch (Exception e) {
            Log.e(FHEMService.class.getName(), "error occurred while de-serializing mensa data", e);
        }
        return updateDeviceListMap();
    }
}
