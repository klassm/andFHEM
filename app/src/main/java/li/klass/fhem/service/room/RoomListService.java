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
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

import li.klass.fhem.AndFHEMApplication;
import li.klass.fhem.R;
import li.klass.fhem.constants.Actions;
import li.klass.fhem.constants.BundleExtraKeys;
import li.klass.fhem.constants.PreferenceKeys;
import li.klass.fhem.domain.core.Device;
import li.klass.fhem.domain.core.RoomDeviceList;
import li.klass.fhem.exception.AndFHEMException;
import li.klass.fhem.exception.CommandExecutionException;
import li.klass.fhem.service.AbstractService;
import li.klass.fhem.service.CommandExecutionService;
import li.klass.fhem.util.ApplicationProperties;

import static li.klass.fhem.constants.Actions.DEVICE_LIST_REMOTE_NOTIFY;
import static li.klass.fhem.util.SharedPreferencesUtil.getSharedPreferences;
import static li.klass.fhem.util.SharedPreferencesUtil.getSharedPreferencesEditor;

public class RoomListService extends AbstractService {

    public static final RoomListService INSTANCE = new RoomListService();
    public static final String TAG = RoomListService.class.getName();

    /**
     * Currently loaded device list map.
     */
    private volatile RoomDeviceList deviceList;

    /**
     * file name of the current cache object.
     */
    public static final String CACHE_FILENAME = "cache.obj";

    public static final String PREFERENCES_NAME = TAG;
    public static final String LAST_UPDATE_PROPERTY = "LAST_UPDATE";

    public static final long NEVER_UPDATE_PERIOD = 0;
    public static final long ALWAYS_UPDATE_PERIOD = -1;

    private final ReentrantLock updateLock = new ReentrantLock();

    private final AtomicBoolean currentlyUpdating = new AtomicBoolean(false);

    private RoomListService() {
    }

    public void parseReceivedDeviceStateMap(String deviceName, Map<String, String> updateMap,
                                            boolean vibrateUponNotification) {

        Device device = getDeviceForName(deviceName, NEVER_UPDATE_PERIOD);
        if (device == null) return;

        DeviceListParser.INSTANCE.fillDeviceWith(device, updateMap);

        Log.i(TAG, "updated " + device.getName() + " with " + updateMap.size() + " new values!");

        Intent intent = new Intent(Actions.NOTIFICATION_TRIGGER);
        intent.putExtra(BundleExtraKeys.DEVICE_NAME, deviceName);
        intent.putExtra(BundleExtraKeys.DEVICE, device);
        intent.putExtra(BundleExtraKeys.UPDATE_MAP, (Serializable) updateMap);
        intent.putExtra(BundleExtraKeys.VIBRATE, vibrateUponNotification);
        AndFHEMApplication.getContext().startService(intent);

        boolean updateWidgets = ApplicationProperties.INSTANCE.getBooleanSharedPreference(PreferenceKeys.GCM_WIDGET_UPDATE, false);
        if (updateWidgets) {
            sendBroadcastWithAction(DEVICE_LIST_REMOTE_NOTIFY);
            getContext().startService(new Intent(DEVICE_LIST_REMOTE_NOTIFY));
        }
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

    public ArrayList<String> getAvailableDeviceNames(long updatePeriod) {
        ArrayList<String> deviceNames = new ArrayList<String>();
        RoomDeviceList allRoomsDeviceList = getAllRoomsDeviceList(updatePeriod);

        for (Device device : allRoomsDeviceList.getAllDevices()) {
            deviceNames.add(device.getName() + "|" +
                    emptyOrValue(device.getAlias()) + "|" +
                    emptyOrValue(device.getWidgetName()));
        }

        return deviceNames;
    }

    private String emptyOrValue(String value) {
        if (value == null) return "";
        return value;
    }

    /**
     * Retrieves a list of all room names.
     *
     * @param updatePeriod -1 if the underlying list should always be updated, otherwise do update if the last update is
     *                     longer ago than the given period
     * @return list of all room names
     */
    public ArrayList<String> getRoomNameList(long updatePeriod) {
        RoomDeviceList roomDeviceList = getRoomDeviceList(updatePeriod);
        if (roomDeviceList == null) return new ArrayList<String>();

        Set<String> roomNames = Sets.newHashSet();
        for (Device device : roomDeviceList.getAllDevices()) {
            @SuppressWarnings("unchecked")
            List<String> rooms = device.getRooms();
            roomNames.addAll(rooms);
        }

        return Lists.newArrayList(roomNames);
    }

    /**
     * Retrieves a {@link RoomDeviceList} containing all devices, not only the devices of a specific room.
     *
     * @param updatePeriod -1 if the underlying list should always be updated, otherwise do update if the last update is
     *                     longer ago than the given period
     * @return {@link RoomDeviceList} containing all devices
     */
    public RoomDeviceList getAllRoomsDeviceList(long updatePeriod) {
        return getRoomDeviceList(updatePeriod);
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
        RoomDeviceList roomDeviceList = new RoomDeviceList(roomName);

        for (Device device : getRoomDeviceList(updatePeriod).getAllDevices()) {
            if (device.isInRoom(roomName)) {
                roomDeviceList.addDevice(device);
            }
        }

        return roomDeviceList;
    }

    /**
     * Switch method deciding whether a FHEM has to be contacted, the cached list can be used or
     * the map already has been loaded to the deviceList attribute.
     * Note that if a remote update is currently in progress, the calling thread will be locked
     * until the current operation has completed. This is especially important, as we are
     * called by a thread pool in {@link li.klass.fhem.service.intent.RoomListIntentService}.
     *
     * @param updatePeriod -1 if the underlying list should always be updated, otherwise do update
     *                     if the last update is longer ago than the given period
     * @return current room device list map
     */
    private RoomDeviceList getRoomDeviceList(long updatePeriod) {
        boolean refresh = shouldUpdate(updatePeriod);

        if (!refresh && deviceList == null) {
            deviceList = getCachedRoomDeviceListMap();
        }

        if (refresh || deviceList == null) {

            try {
                if (! currentlyUpdating.compareAndSet(false, true)) {
                    awaitUpdateCompletion();

                    return deviceList;
                }

                // In any case, make sure that only thread updates at the same time!
                updateLock.lock();

                new AsyncTask<Void, Void, Void>() {

                    @Override
                    protected Void doInBackground(Void... voids) {
                        sendBroadcastWithAction(Actions.SHOW_UPDATING_DIALOG, null);
                        try {
                            deviceList = getRemoteRoomDeviceListMap();
                        } finally {
                            currentlyUpdating.set(false);
                            sendBroadcastWithAction(Actions.DISMISS_UPDATING_DIALOG, null);
                            sendBroadcastWithAction(DEVICE_LIST_REMOTE_NOTIFY, null);
                            getContext().startService(new Intent(DEVICE_LIST_REMOTE_NOTIFY));

                            updateLock.unlock();
                            synchronized (currentlyUpdating) {
                                currentlyUpdating.notifyAll();
                            }
                        }
                        return null;
                    }
                }.doInBackground();

            } catch (AndFHEMException e) {
                int errorStringId = e.getErrorMessageStringId();
                sendErrorMessage(errorStringId);

                Log.e(TAG, "error occurred while fetching the remote device list", e);
            } catch (Exception e) {
                sendErrorMessage(R.string.error_update);
                Log.e(TAG, "unknown exception occurred while fetching the remote device list", e);
            }
        }

        if (deviceList == null) {
            Log.e(TAG, "deviceList is still null, returning an empty hashMap");
            deviceList = new RoomDeviceList(RoomDeviceList.ALL_DEVICES_ROOM);
        }

        return deviceList;
    }

    private void awaitUpdateCompletion() throws InterruptedException {
        while (currentlyUpdating.get() && deviceList == null) {
            Log.i(TAG, "Update in progress, still got null device list, waiting ...");
            synchronized (currentlyUpdating) {
                currentlyUpdating.wait();
            }
        }
    }

    private void sendErrorMessage(int errorStringId) {
        Bundle bundle = new Bundle();
        bundle.putInt(BundleExtraKeys.STRING_ID, errorStringId);
        sendBroadcastWithAction(Actions.SHOW_TOAST, bundle);
    }

    /**
     * Execute a "xmllist" request on the configured FHEM server. The result is parsed
     * by {@link li.klass.fhem.service.room.DeviceListParser} and returned.
     *
     * If the device list parser encounters an exception, null is returned. To make sure
     * that we do not discard any old device list data upon encountering an error, we return
     * the old device list map in this case.
     *
     * @return device list data.
     */
    private synchronized RoomDeviceList getRemoteRoomDeviceListMap() {
        Log.i(TAG, "fetching device list from remote");

        String result = null;
        try {
            result = CommandExecutionService.INSTANCE.executeSafely("xmllist");

        } catch (CommandExecutionException e) {
            Log.i(TAG, "error during command execution", e);
        }
        if (result == null) return deviceList;

        DeviceListParser parser = DeviceListParser.INSTANCE;
        RoomDeviceList newDeviceList = parser.parseAndWrapExceptions(result);

        if (newDeviceList != null) {
            setLastUpdateToNow();
            return newDeviceList;
        }

        return deviceList;
    }

    /**
     * Stores the currently loaded room device list map to the cache file.
     */
    public synchronized void storeDeviceListMap() {
        Log.i(TAG, "storing device list to cache");
        Context context = AndFHEMApplication.getContext();
        try {
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(context.openFileOutput(CACHE_FILENAME, Context.MODE_PRIVATE));
            objectOutputStream.writeObject(deviceList);
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
    private RoomDeviceList getCachedRoomDeviceListMap() {
        try {
            Log.i(TAG, "fetching device list from cache");
            long startLoad = System.currentTimeMillis();

            ObjectInputStream objectInputStream = new ObjectInputStream(AndFHEMApplication.getContext().openFileInput(CACHE_FILENAME));
            RoomDeviceList roomDeviceListMap = (RoomDeviceList) objectInputStream.readObject();
            Log.i(TAG, "loading device list from cache completed after "
                    + (System.currentTimeMillis() - startLoad) + "ms");

            return roomDeviceListMap;
        } catch (Exception e) {
            Log.d(TAG, "error occurred while de-serializing data", e);
            return null;
        }
    }

    public long getLastUpdate() {
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
