package li.klass.fhem.service.room;

import android.content.Context;
import android.util.Log;
import li.klass.fhem.AndFHEMApplication;
import li.klass.fhem.R;
import li.klass.fhem.domain.RoomDeviceList;
import li.klass.fhem.service.CommandExecutionService;
import li.klass.fhem.service.ExecuteOnSuccess;
import li.klass.fhem.service.UpdateDialogAsyncTask;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Map;

public class RoomListService {
    public static final RoomListService INSTANCE = new RoomListService();

    private volatile Map<String,RoomDeviceList> deviceListMap;
    public static final String CACHE_FILENAME = "cache.obj";

    private RoomListService() {
    }

    private interface RoomDeviceListMapListener {
        void onRoomDeviceListRefresh(Map<String,RoomDeviceList> deviceListMap);
    }

    public void getRoomList(Context context, boolean refresh, final RoomListListener listener) {
        getRoomDeviceListMap(context, refresh, new RoomDeviceListMapListener() {
            @Override
            public void onRoomDeviceListRefresh(Map<String, RoomDeviceList> deviceListMap) {
                ArrayList<String> roomNames = new ArrayList<String>(deviceListMap.keySet());
                for (RoomDeviceList roomDeviceList : deviceListMap.values()) {
                    if (roomDeviceList.isOnlyLogDeviceRoom()) {
                        roomNames.remove(roomDeviceList.getRoomName());
                    }
                }
                roomNames.remove(RoomDeviceList.ALL_DEVICES_ROOM);
                listener.onRoomListRefresh(roomNames);
            }
        });
    }
    
    public void getOrCreateRoomDeviceList(Context context, final String roomName, boolean update,
                                                    final RoomDeviceListListener listener) {

        getRoomDeviceListMap(context, update, new RoomDeviceListMapListener() {
            @Override
            public void onRoomDeviceListRefresh(Map<String, RoomDeviceList> deviceListMap) {
                RoomDeviceList roomDeviceList = deviceListMap.get(roomName);

                if (roomDeviceList == null) {
                    roomDeviceList = new RoomDeviceList(roomName);
                    deviceListMap.put(roomName, roomDeviceList);
                }
                listener.onRoomListRefresh(roomDeviceList);
            }
        });
    }

    public void getAllRoomsDeviceList(Context context, boolean update, final RoomDeviceListListener listener) {
        getRoomDeviceListMap(context, update, new RoomDeviceListMapListener() {
            @Override
            public void onRoomDeviceListRefresh(Map<String, RoomDeviceList> deviceListMap) {
                listener.onRoomListRefresh(deviceListMap.get(RoomDeviceList.ALL_DEVICES_ROOM));
            }
        });
    }


    public void getRoomDeviceList(Context context, final String roomName, boolean update, final RoomDeviceListListener listener) {
        getRoomDeviceListMap(context, update, new RoomDeviceListMapListener() {
            @Override
            public void onRoomDeviceListRefresh(Map<String, RoomDeviceList> deviceListMap) {
                listener.onRoomListRefresh(deviceListMap.get(roomName));
            }
        });
    }

    public void removeDeviceListForRoom(Context context, final String roomName) {
        getRoomDeviceListMap(context, false, new RoomDeviceListMapListener() {
            @Override
            public void onRoomDeviceListRefresh(Map<String, RoomDeviceList> deviceListMap) {
                deviceListMap.remove(roomName);
            }
        });
    }

    private void getRoomDeviceListMap(Context context, boolean update, RoomDeviceListMapListener listener) {
        if (update) {
            updateDeviceListMap(context, listener);
        } else if (deviceListMap == null) {
            loadStoredDataFromFile(context, listener);
        } else {
            listener.onRoomDeviceListRefresh(deviceListMap);
        }
    }

    public void storeDeviceListMap() {
        cacheRoomDeviceListMap(deviceListMap);
    }

    private void updateDeviceListMap(Context context, final RoomDeviceListMapListener listener) {
        ExecuteOnSuccess executeOnSuccess = new ExecuteOnSuccess() {
            @Override
            public void onSuccess() {
                listener.onRoomDeviceListRefresh(deviceListMap);
            }
        };

        new UpdateDialogAsyncTask(context, executeOnSuccess) {
            @Override
            protected void executeCommand() {
                deviceListMap = DeviceListParser.INSTANCE.listDevices();
                cacheRoomDeviceListMap(deviceListMap);
            }

            @Override
            protected int getExecuteDialogMessage() {
                return R.string.updating;
            }
        }.executeTask();
    }


    private void cacheRoomDeviceListMap(Map<String, RoomDeviceList> content) {
        Context context = AndFHEMApplication.getContext();
        try {
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(context.openFileOutput(CACHE_FILENAME, Context.MODE_PRIVATE));
            objectOutputStream.writeObject(content);
        } catch (Exception e) {
            Log.e(CommandExecutionService.class.getName(), "error occurred while serializing data", e);
        }
    }

    @SuppressWarnings("unchecked")
    private void loadStoredDataFromFile(Context context, RoomDeviceListMapListener listener) {
        try {
            ObjectInputStream objectInputStream = new ObjectInputStream(AndFHEMApplication.getContext().openFileInput(CACHE_FILENAME));
            Map<String, RoomDeviceList> roomDeviceListMap = (Map<String, RoomDeviceList>) objectInputStream.readObject();

            if (roomDeviceListMap != null) {
                this.deviceListMap = roomDeviceListMap;
                listener.onRoomDeviceListRefresh(roomDeviceListMap);
                return;
            }
        } catch (Exception e) {
            Log.d(CommandExecutionService.class.getName(), "error occurred while de-serializing data", e);
        }
        updateDeviceListMap(context, listener);
    }
}
