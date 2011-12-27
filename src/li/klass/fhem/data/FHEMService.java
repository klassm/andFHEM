package li.klass.fhem.data;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;
import li.klass.fhem.AndFHEMApplication;
import li.klass.fhem.R;
import li.klass.fhem.data.provider.graph.GraphEntry;
import li.klass.fhem.data.provider.graph.GraphProvider;
import li.klass.fhem.domain.Device;
import li.klass.fhem.domain.RoomDeviceList;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public boolean renameDevice(Context context, Device device, String newName) {
        if (FHEMService.INSTANCE.executeSafely(context, "rename " + device.getName() + " " + newName)) {
            device.setName(newName);
            return true;
        }
        return false;
    }

    public boolean deleteDevice(Context context, Device device) {
        if (FHEMService.INSTANCE.executeSafely(context, "delete " + device.getName())) {
            deviceListForAllRooms(false).removeDevice(device);
            deviceListForRoom(device.getRoom(), false).removeDevice(device);
            return true;
        }
        return false;
    }

    public void executeCommand(String command) {
        DataProviderSwitch.INSTANCE.getCurrentProvider().executeCommand(command);
    }

    public boolean executeSafely(Context context, String command) {
        try {
            executeCommand(command);
            return true;
        } catch (Exception e) {
            Toast.makeText(context, R.string.executeError, Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    public Map<String, List<GraphEntry>> getGraphData(Device device, List<String> columnSpecifications) {
        if (device.getFileLog() == null) return null;

        Map<String, List<GraphEntry>> data = new HashMap<String, List<GraphEntry>>();

        GraphProvider graphProvider = GraphProvider.INSTANCE;
        for (String columnSpec : columnSpecifications) {
            String fileLogDeviceName = device.getFileLog().getName();
            List<GraphEntry> valueEntries = graphProvider.getCurrentGraphEntriesFor(fileLogDeviceName, columnSpec);
            data.put(columnSpec, valueEntries);
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
