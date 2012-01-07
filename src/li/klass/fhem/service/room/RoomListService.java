/*
 * AndFHEM - Open Source Android application to control a FHEM home automation
 * server.
 *
 * Copyright (c) 2011, Matthias Klass or third-party contributors as
 * indicated by the @author tags or express copyright attribution
 * statements applied by the authors.  All third-party contributions are
 * distributed under license by Red Hat Inc.
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU GENERAL PUBLIC LICENSE, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU GENERAL PUBLIC LICENSE
 * for more details.
 *
 * You should have received a copy of the GNU GENERAL PUBLIC LICENSE
 * along with this distribution; if not, write to:
 *   Free Software Foundation, Inc.
 *   51 Franklin Street, Fifth Floor
 *   Boston, MA  02110-1301  USA
 */

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

/**
 * Class containing all the functionality for managing room and device lists.
 */
public class RoomListService {

    /**
     * Currently loaded device list map.
     */
    private volatile Map<String,RoomDeviceList> deviceListMap;

    /**
     * file name of the current cache object.
     */
    public static final String CACHE_FILENAME = "cache.obj";

    public static final RoomListService INSTANCE = new RoomListService();

    private RoomListService() {}

    private interface RoomDeviceListMapListener {
        void onRoomDeviceListRefresh(Map<String,RoomDeviceList> deviceListMap);
    }

    /**
     * Retrieves a list of all room names.
     * @param context context in which the action was started.
     * @param refresh should the underlying {@link RoomDeviceList} be refreshed by asking FHEM for new values?
     * @param listener listener to notify when the room list has been retrieved
     */
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

    /**
     * Gets or creates a new device list for a given room.
     * @param context context in which the action was started.
     * @param roomName room name used for searching
     * @param update should the underlying {@link RoomDeviceList} be refreshed by asking FHEM for new values?
     * @param listener listener to notify when the room list has been retrieved
     */
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

    /**
     * Retrieves a {@link RoomDeviceList} containing all devices, not only the devices of a specific room.
     * @param context context in which the action was started.
     * @param update should the underlying {@link RoomDeviceList} be refreshed by asking FHEM for new values?
     * @param listener listener to notify when the room device list has been retrieved.
     */
    public void getAllRoomsDeviceList(Context context, boolean update, final RoomDeviceListListener listener) {
        getRoomDeviceListMap(context, update, new RoomDeviceListMapListener() {
            @Override
            public void onRoomDeviceListRefresh(Map<String, RoomDeviceList> deviceListMap) {
                listener.onRoomListRefresh(deviceListMap.get(RoomDeviceList.ALL_DEVICES_ROOM));
            }
        });
    }

    /**
     * Retrieves the {@link RoomDeviceList} for a specific room name.
     * @param context context context in which the action was started.
     * @param roomName room name used for searching.
     * @param update should the underlying {@link RoomDeviceList} be refreshed by asking FHEM for new values?
     * @param listener listener to notify when the room device list has been retrieved.
     */
    public void getRoomDeviceList(Context context, final String roomName, boolean update, final RoomDeviceListListener listener) {
        getRoomDeviceListMap(context, update, new RoomDeviceListMapListener() {
            @Override
            public void onRoomDeviceListRefresh(Map<String, RoomDeviceList> deviceListMap) {
                listener.onRoomListRefresh(deviceListMap.get(roomName));
            }
        });
    }

    /**
     * Removes the {@link RoomDeviceList} being associated to the given room name.
     * @param context context context in which the action was started.
     * @param roomName room name used for searching the room
     */
    public void removeDeviceListForRoom(Context context, final String roomName) {
        getRoomDeviceListMap(context, false, new RoomDeviceListMapListener() {
            @Override
            public void onRoomDeviceListRefresh(Map<String, RoomDeviceList> deviceListMap) {
                deviceListMap.remove(roomName);
            }
        });
    }

    /**
     * Switch method deciding whether a FHEM has to be contacted, the cached list can be used or the map already has
     * been loaded to the deviceListMap attribute.
     * @param context context context context in which the action was started.
     * @param update update should the underlying {@link RoomDeviceList} be refreshed by asking FHEM for new values?
     * @param listener listener listener to notify when the room device list map has been retrieved.
     */
    private void getRoomDeviceListMap(Context context, boolean update, RoomDeviceListMapListener listener) {
        if (update) {
            updateDeviceListMap(context, listener);
        } else if (deviceListMap == null) {
            loadStoredDataFromFile(context, listener);
        } else {
            listener.onRoomDeviceListRefresh(deviceListMap);
        }
    }

    /**
     * Loads the most current room device list map from FHEM and saves it to the cache.
     * @param context context context context in which the action was started.
     * @param listener listener listener to notify when the room device list map has been retrieved.
     */
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
                storeDeviceListMap();
            }

            @Override
            protected int getExecuteDialogMessage() {
                return R.string.updating;
            }
        }.executeTask();
    }

    /**
     * Stores the currently loaded room device list map to the cache file.
     */
    public void storeDeviceListMap() {
        Context context = AndFHEMApplication.getContext();
        try {
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(context.openFileOutput(CACHE_FILENAME, Context.MODE_PRIVATE));
            objectOutputStream.writeObject(deviceListMap);
        } catch (Exception e) {
            Log.e(CommandExecutionService.class.getName(), "error occurred while serializing data", e);
        }
    }

    /**
     * Loads the currently cached room device list map data from the file storage.
     * @param context context context context in which the action was started.
     * @param listener listener listener to notify when the room device list map has been retrieved.
     */
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
