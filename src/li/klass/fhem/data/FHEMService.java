package li.klass.fhem.data;

import android.content.Context;
import android.util.Log;
import li.klass.fhem.AndFHEMApplication;
import li.klass.fhem.data.provider.graph.GraphEntry;
import li.klass.fhem.data.provider.graph.GraphProvider;
import li.klass.fhem.domain.Device;
import li.klass.fhem.domain.RoomDeviceList;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.*;

public class FHEMService {
    public static final FHEMService INSTANCE = new FHEMService();
    public static final String CACHE_FILENAME = "cache.obj";

    private Map<String,RoomDeviceList> deviceListMap;

    private FHEMService() {}

    public List<String> getRoomList(boolean refresh) {
        Map<String, RoomDeviceList> deviceListMap = getRoomDeviceListMap(refresh);
        ArrayList<String> roomNames = new ArrayList<String>(deviceListMap.keySet());
        for (RoomDeviceList roomDeviceList : deviceListMap.values()) {
            if (roomDeviceList.isOnlyLogDeviceRoom()) {
                roomNames.remove(roomDeviceList.getRoomName());
            }
        }
        roomNames.remove(RoomDeviceList.ALL_DEVICES_ROOM);
        return roomNames;
    }

    public RoomDeviceList deviceListForRoom(String roomName, boolean update) {
        Map<String, RoomDeviceList> deviceListMap = getRoomDeviceListMap(update);
        return deviceListMap.get(roomName);
    }

    public RoomDeviceList deviceListForAllRooms(boolean update) {
        return getRoomDeviceListMap(update).get(RoomDeviceList.ALL_DEVICES_ROOM);
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
        Map<String, RoomDeviceList> newList = DeviceListProvider.INSTANCE.listDevices();
        cacheRoomDeviceListMap(newList);
        return newList;
    }

    public void executeCommand(String command) {
        DataProviderSwitch.INSTANCE.getCurrentProvider().executeCommand(command);
    }

    @SuppressWarnings("unchecked")
    public Map<String, List<GraphEntry>> getGraphData(Device device) {
        if (device.getFileLog() == null) return null;

        Map<String, List<GraphEntry>> data = new HashMap<String, List<GraphEntry>>();
        Map columnSpecifications = device.getFileLogColumns();
        Set<String> keys = columnSpecifications.keySet();

        GraphProvider graphProvider = GraphProvider.INSTANCE;
        for (String key : keys) {
            String fileLogDeviceName = device.getFileLog().getName();
            List<GraphEntry> valueEntries = graphProvider.getCurrentGraphEntriesFor(fileLogDeviceName, (String) columnSpecifications.get(key));
            data.put(key, valueEntries);
        }

        return data;
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
            ObjectInputStream objectInputStream = new ObjectInputStream(AndFHEMApplication.getContext().openFileInput(CACHE_FILENAME));
            Map<String, RoomDeviceList> roomDeviceListMap = (Map<String, RoomDeviceList>) objectInputStream.readObject();

            if (roomDeviceListMap != null) {
                return roomDeviceListMap;
            }
        } catch (Exception e) {
            Log.d(FHEMService.class.getName(), "error occurred while de-serializing data", e);
        }
        return updateDeviceListMap();
    }
}
