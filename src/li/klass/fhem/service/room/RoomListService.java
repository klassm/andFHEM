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
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;
import li.klass.fhem.AndFHEMApplication;
import li.klass.fhem.R;
import li.klass.fhem.constants.Actions;
import li.klass.fhem.domain.Device;
import li.klass.fhem.domain.RoomDeviceList;
import li.klass.fhem.exception.DeviceListParseException;
import li.klass.fhem.exception.HostConnectionException;
import li.klass.fhem.service.CommandExecutionService;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Map;

public class RoomListService {
    public static final RoomListService INSTANCE = new RoomListService();

    /**
     * Currently loaded device list map.
     */
    private volatile Map<String,RoomDeviceList> deviceListMap;

    /**
     * file name of the current cache object.
     */
    public static final String CACHE_FILENAME = "cache.obj";

    private RoomListService() {
    }

    /**
     * Looks for a device with a given name.
     * @param deviceName name of the device
     * @param refresh refresh device list
     * @return found device or null
     */
    public Device getDeviceForName(String deviceName, boolean refresh) {
        return getAllRoomsDeviceList(refresh).getDeviceFor(deviceName);
    }

    /**
     * Retrieves a list of all room names.
     * @param refresh should the underlying {@link RoomDeviceList} be refreshed by asking FHEM for new values?
     * @return list of all room names
     */
    public ArrayList<String> getRoomNameList(boolean refresh) {
        Map<String, RoomDeviceList> map = getRoomDeviceListMap(refresh);
        ArrayList<String> roomNames = new ArrayList<String>(map.keySet());
        for (RoomDeviceList roomDeviceList : map.values()) {
            if (roomDeviceList.isOnlyLogDeviceRoom()) {
                roomNames.remove(roomDeviceList.getRoomName());
            } else if (roomDeviceList.getRoomName().equals(RoomDeviceList.ALL_DEVICES_ROOM)) {
                roomNames.remove(roomDeviceList.getRoomName());
            }
        }
        return roomNames;
    }

    /**
     * Gets or creates a new device list for a given room.
     * @param roomName room name used for searching
     * @param refresh should the underlying {@link RoomDeviceList} be refreshed by asking FHEM for new values?
     * @return {@link RoomDeviceList} for a room
     */
    public RoomDeviceList getOrCreateRoomDeviceList(final String roomName, boolean refresh) {
        Map<String, RoomDeviceList> map = getRoomDeviceListMap(refresh);
        RoomDeviceList roomDeviceList = map.get(roomName);
        if (roomDeviceList == null) {
            roomDeviceList = new RoomDeviceList(roomName);
            map.put(roomName, roomDeviceList);
        }

        return roomDeviceList;
    }

    /**
     * Retrieves a {@link RoomDeviceList} containing all devices, not only the devices of a specific room.
     * @param refresh should the underlying {@link RoomDeviceList} be refreshed by asking FHEM for new values?
     * @return {@link RoomDeviceList} containing all devices
     */
    public RoomDeviceList getAllRoomsDeviceList(boolean refresh) {
        return getRoomDeviceListMap(refresh).get(RoomDeviceList.ALL_DEVICES_ROOM);
    }

    /**
     * Retrieves the {@link RoomDeviceList} for a specific room name.
     * @param roomName room name used for searching.
     * @param refresh should the underlying {@link RoomDeviceList} be refreshed by asking FHEM for new values?
     * @return found {@link RoomDeviceList} or null
     */
    public RoomDeviceList getDeviceListForRoom(String roomName, boolean refresh) {
        return getRoomDeviceListMap(refresh).get(roomName);
    }

    /**
     * Removes the {@link RoomDeviceList} being associated to the given room name.
     * @param roomName room name used for searching the room
     */
    public void removeDeviceListForRoom(String roomName) {
        getRoomDeviceListMap(false).remove(roomName);
    }

    /**
     * Switch method deciding whether a FHEM has to be contacted, the cached list can be used or the map already has
     * been loaded to the deviceListMap attribute.
     * @param refresh refresh should the underlying {@link RoomDeviceList} be refreshed by asking FHEM for new values?
     * @return current room device list map
     */
    private Map<String, RoomDeviceList> getRoomDeviceListMap(boolean refresh) {
        if (! refresh && deviceListMap == null) {
            deviceListMap = getCachedRoomDeviceListMap();
        }
        
        Context context = AndFHEMApplication.getContext();

        if (refresh || deviceListMap == null) {
            sendBroadcastWithAction(Actions.SHOW_UPDATING_DIALOG);
            try {
                deviceListMap = getRemoteRoomDeviceListMap();
            }  catch (HostConnectionException e) {
                Toast.makeText(context, R.string.updateErrorHostConnection, Toast.LENGTH_LONG).show();
                Log.e(RoomListService.class.getName(), "error occurred", e);
            } catch (DeviceListParseException e) {
                Toast.makeText(context, R.string.updateErrorDeviceListParse, Toast.LENGTH_LONG).show();
                Log.e(RoomListService.class.getName(), "error occurred", e);
            } finally {
                sendBroadcastWithAction(Actions.DISMISS_UPDATING_DIALOG);
            }
        }

        return deviceListMap;
    }

    /**
     * Loads the most current room device list map from FHEM and saves it to the cache.
     * @return remotely loaded room device list map
     */
    private Map<String, RoomDeviceList> getRemoteRoomDeviceListMap() {
        return DeviceListParser.INSTANCE.listDevices();
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
     * @return cached room device list map
     */
    @SuppressWarnings("unchecked")
    private Map<String, RoomDeviceList> getCachedRoomDeviceListMap() {
        try {
            ObjectInputStream objectInputStream = new ObjectInputStream(AndFHEMApplication.getContext().openFileInput(CACHE_FILENAME));
            return (Map<String, RoomDeviceList>) objectInputStream.readObject();
        } catch (Exception e) {
            Log.d(CommandExecutionService.class.getName(), "error occurred while de-serializing data", e);
            return null;
        }
    }

    /**
     * Sends a broadcast message containing a specified action. Context is the application context.
     * @param action action to use for sending the broadcast intent.
     */
    private void sendBroadcastWithAction(String action) {
        Intent broadcastIntent = new Intent(action);
        AndFHEMApplication.getContext().sendBroadcast(broadcastIntent);
    }
}
