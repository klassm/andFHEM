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
import android.os.Bundle;
import android.util.Log;
import li.klass.fhem.AndFHEMApplication;
import li.klass.fhem.R;
import li.klass.fhem.constants.Actions;
import li.klass.fhem.constants.BundleExtraKeys;
import li.klass.fhem.domain.core.Device;
import li.klass.fhem.domain.core.RoomDeviceList;
import li.klass.fhem.exception.AndFHEMException;
import li.klass.fhem.service.AbstractService;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import static li.klass.fhem.util.SharedPreferencesUtil.getSharedPreferences;
import static li.klass.fhem.util.SharedPreferencesUtil.getSharedPreferencesEditor;

public class RoomListService extends AbstractService {

    public static final RoomListService INSTANCE = new RoomListService();
    public static final String TAG = RoomListService.class.getName();

    /**
     * Currently loaded device list map.
     */
    private volatile Map<String, RoomDeviceList> deviceListMap;

    /**
     * file name of the current cache object.
     */
    public static final String CACHE_FILENAME = "cache.obj";

    public static final String PREFERENCES_NAME = TAG;
    public static final String LAST_UPDATE_PROPERTY = "LAST_UPDATE";

    public static final long NEVER_UPDATE_PERIOD = 0;
    public static final long ALWAYS_UPDATE_PERIOD = -1;

    private final ReentrantLock reentrantLock = new ReentrantLock();

    private final AtomicBoolean currentlyUpdating = new AtomicBoolean(false);

    private RoomListService() {
    }

    public void parseReceivedDeviceStateMap(String deviceName, Map<String, String> updateMap) {
        Device device = getDeviceForName(deviceName, NEVER_UPDATE_PERIOD);
        if (device == null) return;

        DeviceListParser.INSTANCE.fillDeviceWith(device, updateMap);
        Log.i(TAG, "updated " + device.getName() + " with " + updateMap.size() + " new values!");
    }

    /**
     * Looks for a device with a given name.
     *
     * @param deviceName   name of the device
     * @param updatePeriod -1 if the underlying list should always be updated, otherwise do update if the last update is
     *                     longer ago than the given period
     * @return found device or null
     */
    public Device getDeviceForName(String deviceName, long updatePeriod) {
        return getAllRoomsDeviceList(updatePeriod).getDeviceFor(deviceName);
    }

    /**
     * Retrieves a list of all room names.
     *
     * @param updatePeriod -1 if the underlying list should always be updated, otherwise do update if the last update is
     *                     longer ago than the given period
     * @return list of all room names
     */
    public ArrayList<String> getRoomNameList(long updatePeriod) {
        Map<String, RoomDeviceList> map = getRoomDeviceListMap(updatePeriod);
        if (map == null) return new ArrayList<String>();

        ArrayList<String> roomNames = new ArrayList<String>(map.keySet());
        for (RoomDeviceList roomDeviceList : map.values()) {
            if (roomDeviceList.isEmptyOrOnlyContainsDoNotShowDevices()) {
                Log.i(TAG, "removing " + roomDeviceList.getRoomName() + ", as it only contains do not show devices!");
                roomNames.remove(roomDeviceList.getRoomName());
            } else if (roomDeviceList.getRoomName().equals(RoomDeviceList.ALL_DEVICES_ROOM)) {
                roomNames.remove(roomDeviceList.getRoomName());
            }
        }
        return roomNames;
    }

    /**
     * Gets or creates a new device list for a given room.
     *
     * @param roomName     room name used for searching
     * @param updatePeriod -1 if the underlying list should always be updated, otherwise do update if the last update is
     *                     longer ago than the given period
     * @return {@link RoomDeviceList} for a room
     */
    public synchronized RoomDeviceList getOrCreateRoomDeviceList(final String roomName, long updatePeriod) {
        Map<String, RoomDeviceList> map = getRoomDeviceListMap(updatePeriod);
        RoomDeviceList roomDeviceList = map.get(roomName);
        if (roomDeviceList == null) {
            roomDeviceList = new RoomDeviceList(roomName);
            map.put(roomName, roomDeviceList);
        }

        return roomDeviceList;
    }

    /**
     * Retrieves a {@link RoomDeviceList} containing all devices, not only the devices of a specific room.
     *
     * @param updatePeriod -1 if the underlying list should always be updated, otherwise do update if the last update is
     *                     longer ago than the given period
     * @return {@link RoomDeviceList} containing all devices
     */
    public RoomDeviceList getAllRoomsDeviceList(long updatePeriod) {
        Map<String, RoomDeviceList> roomDeviceListMap = getRoomDeviceListMap(updatePeriod);
        RoomDeviceList allRoomsDeviceList = new RoomDeviceList(RoomDeviceList.ALL_DEVICES_ROOM);
        for (String room : roomDeviceListMap.keySet()) {
            for (Device device : roomDeviceListMap.get(room).getAllDevices()) {
                allRoomsDeviceList.addDevice(device);
            }
        }
        return allRoomsDeviceList;
    }

    /**
     * Retrieves the {@link RoomDeviceList} for a specific room name.
     *
     * @param roomName     room name used for searching.
     * @param updatePeriod -1 if the underlying list should always be updated, otherwise do update if the last update is
     *                     longer ago than the given period
     * @return found {@link RoomDeviceList} or null
     */
    public RoomDeviceList getDeviceListForRoom(String roomName, long updatePeriod) {
        return getRoomDeviceListMap(updatePeriod).get(roomName);
    }

    /**
     * Removes the {@link RoomDeviceList} being associated to the given room name.
     *
     * @param roomName room name used for searching the room
     */
    public synchronized void removeDeviceListForRoom(String roomName) {
        getRoomDeviceListMap(NEVER_UPDATE_PERIOD).remove(roomName);
    }

    /**
     * Switch method deciding whether a FHEM has to be contacted, the cached list can be used or the map already has
     * been loaded to the deviceListMap attribute.
     *
     * @param updatePeriod -1 if the underlying list should always be updated, otherwise do update if the last update is
     *                     longer ago than the given period
     * @return current room device list map
     */
    private Map<String, RoomDeviceList> getRoomDeviceListMap(long updatePeriod) {
        boolean refresh = shouldUpdate(updatePeriod);

        if (!refresh && deviceListMap == null) {
            deviceListMap = getCachedRoomDeviceListMap();
        }

        if (refresh || deviceListMap == null) {

            try {
                if (currentlyUpdating.compareAndSet(false, true)) {
                    reentrantLock.lock();

                    sendBroadcastWithAction(Actions.SHOW_UPDATING_DIALOG, null);
                    try {
                        deviceListMap = getRemoteRoomDeviceListMap();
                    } finally {
                        currentlyUpdating.set(false);
                        sendBroadcastWithAction(Actions.DISMISS_UPDATING_DIALOG, null);
                        sendBroadcastWithAction(Actions.DEVICE_LIST_REMOTE_NOTIFY, null);

                        reentrantLock.unlock();
                    }
                } else {
                    while (currentlyUpdating.get() && deviceListMap == null) {
                        try {
                            Log.i(TAG, "Update in progress, still got null device list, waiting ...");
                            Thread.sleep(1000);
                        } catch (Exception ignored) {
                        }
                    }
                    Log.i(TAG, "should update, but update is currently in progress. Returning cached roomDeviceList");
                    return deviceListMap;
                }
            } catch (AndFHEMException e) {
                int errorStringId = e.getErrorMessageStringId();
                sendErrorMessage(errorStringId);

                Log.e(TAG, "error occurred while fetching the remote device list", e);
            } catch (Exception e) {
                sendErrorMessage(R.string.updateError);
                Log.e(TAG, "unknown exception occurred while fetching the remote device list", e);
            }
        }

        if (deviceListMap == null) {
            Log.e(TAG, "deviceListMap is still null, returning an empty hashMap");
            deviceListMap = new HashMap<String, RoomDeviceList>();
        }

        return deviceListMap;
    }

    private void sendErrorMessage(int errorStringId) {
        Bundle bundle = new Bundle();
        bundle.putInt(BundleExtraKeys.TOAST_STRING_ID, errorStringId);
        sendBroadcastWithAction(Actions.SHOW_TOAST, bundle);
    }

    /**
     * Loads the most current room device list map from FHEM. Only one request can be active simultaneously. This is
     * why a callable is used. If a request is in progress, the next request just waits for the same result.
     * @return remotely loaded room device list map
     */
    /**
     * Loads the most current room device list map from FHEM and saves it to the cache.
     *
     * @return remotely loaded room device list map
     */
    private synchronized Map<String, RoomDeviceList> getRemoteRoomDeviceListMap() {
        Log.i(TAG, "fetching device list from remote");
        Map<String, RoomDeviceList> result = DeviceListParser.INSTANCE.listDevices();
        setLastUpdateToNow();
        return result;
    }

    /**
     * Stores the currently loaded room device list map to the cache file.
     */
    public synchronized void storeDeviceListMap() {
        Log.i(TAG, "storing device list to cache");
        Context context = AndFHEMApplication.getContext();
        try {
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(context.openFileOutput(CACHE_FILENAME, Context.MODE_PRIVATE));
            objectOutputStream.writeObject(deviceListMap);
        } catch (Exception e) {
            Log.e(TAG, "error occurred while serializing data", e);
        }
    }

    /**
     * Loads the currently cached room device list map data from the file storage.
     *
     * @return cached room device list map
     */
    @SuppressWarnings("unchecked")
    private Map<String, RoomDeviceList> getCachedRoomDeviceListMap() {
        try {
            Log.i(TAG, "fetching device list from cache");
            long startLoad = System.currentTimeMillis();

            ObjectInputStream objectInputStream = new ObjectInputStream(AndFHEMApplication.getContext().openFileInput(CACHE_FILENAME));
            Map<String, RoomDeviceList> roomDeviceListMap = (Map<String, RoomDeviceList>) objectInputStream.readObject();
            Log.i(TAG, "loading device list from cache completed after "
                    + (System.currentTimeMillis() - startLoad) + "ms");

            return roomDeviceListMap;
        } catch (Exception e) {
            Log.d(TAG, "error occurred while de-serializing data", e);
            return null;
        }
    }

    private long getLastUpdate() {
        return getSharedPreferences(PREFERENCES_NAME).getLong(LAST_UPDATE_PROPERTY, 0L);
    }

    private void setLastUpdateToNow() {
        getSharedPreferencesEditor(PREFERENCES_NAME).putLong(LAST_UPDATE_PROPERTY, System.currentTimeMillis()).commit();
    }

    private boolean shouldUpdate(long updatePeriod) {
        if (updatePeriod == ALWAYS_UPDATE_PERIOD) {
            Log.i(TAG, "recommend update, as updatePeriod is set to ALWAYS_UPDATE");
            return true;
        }
        if (updatePeriod == NEVER_UPDATE_PERIOD) {
            Log.i(TAG, "recommend no update, as updatePeriod is set to NEVER_UPDATE");
            return false;
        }

        long lastUpdate = getLastUpdate();
        boolean shouldUpdate = lastUpdate + updatePeriod < System.currentTimeMillis();
        Log.i(TAG, "recommend " + (!shouldUpdate ? "no " : "") + "update (lastUpdate: " + lastUpdate +
                ", updatePeriod: " + updatePeriod + ")");
        return shouldUpdate;
    }
}
