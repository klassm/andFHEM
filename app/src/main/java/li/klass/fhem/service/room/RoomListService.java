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

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;
import javax.inject.Singleton;

import li.klass.fhem.AndFHEMApplication;
import li.klass.fhem.constants.Actions;
import li.klass.fhem.constants.BundleExtraKeys;
import li.klass.fhem.constants.PreferenceKeys;
import li.klass.fhem.dagger.ForApplication;
import li.klass.fhem.domain.FHEMWEBDevice;
import li.klass.fhem.domain.core.Device;
import li.klass.fhem.domain.core.DeviceType;
import li.klass.fhem.domain.core.RoomDeviceList;
import li.klass.fhem.service.AbstractService;
import li.klass.fhem.service.SharedPreferencesService;
import li.klass.fhem.service.connection.ConnectionService;
import li.klass.fhem.service.intent.DeviceIntentService;
import li.klass.fhem.util.ApplicationProperties;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.sort;
import static li.klass.fhem.constants.Actions.DISMISS_EXECUTING_DIALOG;
import static li.klass.fhem.constants.Actions.REDRAW_ALL_WIDGETS;
import static li.klass.fhem.constants.BundleExtraKeys.DO_REFRESH;
import static li.klass.fhem.constants.BundleExtraKeys.UPDATE_PERIOD;
import static li.klass.fhem.constants.PreferenceKeys.DEVICE_NAME;
import static li.klass.fhem.domain.core.DeviceType.getDeviceTypeFor;
import static li.klass.fhem.util.DateFormatUtil.toReadable;

@Singleton
public class RoomListService extends AbstractService {

    public static final String TAG = RoomListService.class.getName();

    public static final String PREFERENCES_NAME = TAG;

    public static final String CACHE_FILENAME = "cache.obj";

    public static final String LAST_UPDATE_PROPERTY = "LAST_UPDATE";

    public static final long NEVER_UPDATE_PERIOD = 0;

    public static final long ALWAYS_UPDATE_PERIOD = -1;
    public static final String SORT_ROOMS_DELIMITER = " ";
    public static final String DEFAULT_FHEMWEB_QUALIFIER = "andFHEM";

    private final AtomicBoolean remoteUpdateInProgress = new AtomicBoolean(false);

    private List<Intent> resendIntents = Lists.newArrayList();

    volatile RoomDeviceList deviceList;

    @Inject
    ConnectionService connectionService;

    @Inject
    DeviceListParser deviceListParser;

    @Inject
    ApplicationProperties applicationProperties;

    @Inject
    @ForApplication
    Context applicationContext;

    @Inject
    SharedPreferencesService sharedPreferencesService;

    public void parseReceivedDeviceStateMap(String deviceName, Map<String, String> updateMap,
                                            boolean vibrateUponNotification) {

        Device device = getDeviceForName(deviceName);
        if (device == null) return;

        deviceListParser.fillDeviceWith(device, updateMap);

        Log.i(TAG, "parseReceivedDeviceStateMap()  : updated " + device.getName() + " with " + updateMap.size() + " new values!");

        Intent intent = new Intent(Actions.NOTIFICATION_TRIGGER);
        intent.putExtra(BundleExtraKeys.DEVICE_NAME, deviceName);
        intent.putExtra(BundleExtraKeys.DEVICE, device);
        intent.putExtra(BundleExtraKeys.UPDATE_MAP, (Serializable) updateMap);
        intent.putExtra(BundleExtraKeys.VIBRATE, vibrateUponNotification);
        applicationContext.startService(intent);

        boolean updateWidgets = applicationProperties.getBooleanSharedPreference(PreferenceKeys.GCM_WIDGET_UPDATE, false);
        if (updateWidgets) {
            sendBroadcastWithAction(REDRAW_ALL_WIDGETS);

            Intent redrawAllWidgetsServiceIntent = new Intent(REDRAW_ALL_WIDGETS);
            redrawAllWidgetsServiceIntent.setClass(applicationContext, DeviceIntentService.class);
            applicationContext.startService(redrawAllWidgetsServiceIntent);
        }
    }

    /**
     * Looks for a device with a given name.
     *
     * @param deviceName name of the device
     * @return found device or null
     */
    public Device getDeviceForName(String deviceName) {
        return getAllRoomsDeviceList().getDeviceFor(deviceName);
    }

    /**
     * Retrieves a {@link RoomDeviceList} containing all devices, not only the devices of a specific room.
     *
     * @return {@link RoomDeviceList} containing all devices
     */
    public RoomDeviceList getAllRoomsDeviceList() {
        if (deviceList == null) {
            deviceList = getCachedRoomDeviceListMap();
        }
        return new RoomDeviceList(getRoomDeviceList());
    }

    /**
     * Switch method deciding whether a FHEM has to be contacted, the cached list can be used or
     * the map already has been loaded to the deviceList attribute.
     * Note that if a remote update is currently in progress, the calling thread will be locked
     * until the current operation has completed. This is especially important, as we are
     * called by a thread pool in {@link li.klass.fhem.service.intent.RoomListIntentService}.
     *
     * @return current room device list map
     */
    private RoomDeviceList getRoomDeviceList() {
        return deviceList;
    }

    public RemoteUpdateRequired updateRoomDeviceListIfRequired(Intent intent, long updatePeriod) {
        if (deviceList == null) {
            deviceList = getCachedRoomDeviceListMap();
        }

        boolean requiresUpdate = shouldUpdate(updatePeriod) || deviceList == null;
        if (requiresUpdate) {
            resendIntents.add(createResendIntent(intent));
            if (remoteUpdateInProgress.compareAndSet(false, true)) {
                applicationContext.startService(new Intent(Actions.DO_REMOTE_UPDATE));
            }

            return RemoteUpdateRequired.REQUIRED;
        } else {
            return RemoteUpdateRequired.NOT_REQUIRED;
        }
    }

    public void remoteUpdateFinished(Intent intent) {
        try {
            RoomDeviceList newDeviceList = (RoomDeviceList) intent.getSerializableExtra(BundleExtraKeys.DEVICE_LIST);
            setLastUpdateToNow();
            fillHiddenRoomsAndHiddenGroups(newDeviceList, findFHEMWEBDevice(newDeviceList));
            if (newDeviceList != null) {
                deviceList = newDeviceList;
            }

            if (!deviceList.isEmptyOrOnlyContainsDoNotShowDevices()) {
                storeDeviceListMap();
            }

            for (Intent resendIntent : resendIntents) {
                resend(resendIntent);
            }
            resendIntents.clear();
            Log.i(TAG, "remote update finished, device list is " + deviceList);
        } finally {
            remoteUpdateInProgress.set(false);
            sendBroadcastWithAction(DISMISS_EXECUTING_DIALOG);
        }
    }

    private void resend(Intent intent) {
        Log.i(TAG, "resend() : resending " + intent.getAction());
        applicationContext.startService(createResendIntent(intent));
    }

    private Intent createResendIntent(Intent intent) {
        Intent resendIntent = new Intent(intent);
        resendIntent.removeExtra(DO_REFRESH);
        resendIntent.removeExtra(UPDATE_PERIOD);

        resendIntent.putExtra(UPDATE_PERIOD, NEVER_UPDATE_PERIOD);

        return resendIntent;
    }

    private boolean shouldUpdate(long updatePeriod) {
        if (updatePeriod == ALWAYS_UPDATE_PERIOD) {
            Log.d(TAG, "shouldUpdate() : recommend update, as updatePeriod is set to ALWAYS_UPDATE");
            return true;
        }
        if (updatePeriod == NEVER_UPDATE_PERIOD) {
            Log.d(TAG, "shouldUpdate() : recommend no update, as updatePeriod is set to NEVER_UPDATE");
            return false;
        }

        long lastUpdate = getLastUpdate();
        boolean shouldUpdate = lastUpdate + updatePeriod < System.currentTimeMillis();

        Log.d(TAG, "shouldUpdate() : recommend " + (!shouldUpdate ? "no " : "") + "update (lastUpdate: " + toReadable(lastUpdate) +
                ", updatePeriod: " + (updatePeriod / 1000 / 60) + " min)");

        return shouldUpdate;
    }

    /**
     * Loads the currently cached room device list map data from the file storage.
     *
     * @return cached room device list map
     */
    @SuppressWarnings("unchecked")
    private RoomDeviceList getCachedRoomDeviceListMap() {
        try {
            Log.i(TAG, "getCachedRoomDeviceListMap() : fetching device list from cache");
            long startLoad = System.currentTimeMillis();

            ObjectInputStream objectInputStream = new ObjectInputStream(AndFHEMApplication.getContext().openFileInput(CACHE_FILENAME));
            RoomDeviceList roomDeviceListMap = (RoomDeviceList) objectInputStream.readObject();
            Log.i(TAG, "getCachedRoomDeviceListMap() : loading device list from cache completed after "
                    + (System.currentTimeMillis() - startLoad) + "ms");

            return roomDeviceListMap;
        } catch (Exception e) {
            Log.d(TAG, "getCachedRoomDeviceListMap() : error occurred while de-serializing data", e);
            return new RoomDeviceList(RoomDeviceList.ALL_DEVICES_ROOM);
        }
    }

    private void fillHiddenRoomsAndHiddenGroups(RoomDeviceList newRoomDeviceList,
                                                FHEMWEBDevice fhemwebDevice) {
        if (newRoomDeviceList == null) return;

        newRoomDeviceList.setHiddenGroups(fhemwebDevice.getHiddenGroups());
        newRoomDeviceList.setHiddenRooms(fhemwebDevice.getHiddenRooms());
    }

    private FHEMWEBDevice findFHEMWEBDevice(RoomDeviceList allRoomDeviceList) {
        List<Device> devicesOfType = allRoomDeviceList == null ?
                Lists.<Device>newArrayList() : allRoomDeviceList.getDevicesOfType(DeviceType.FHEMWEB);
        return findFHEMWEBDevice(devicesOfType);
    }

    FHEMWEBDevice findFHEMWEBDevice(List<Device> devices) {
        String qualifier = applicationProperties.getStringSharedPreference(DEVICE_NAME, DEFAULT_FHEMWEB_QUALIFIER).toUpperCase(Locale.getDefault());
        if (!devices.isEmpty()) {
            FHEMWEBDevice foundDevice = null;
            for (Device device : devices) {
                if (device.getName() != null && device.getName().toUpperCase(Locale.getDefault()).contains(qualifier)) {
                    foundDevice = (FHEMWEBDevice) device;
                    break;
                }
            }
            if (foundDevice != null) {
                return foundDevice;
            } else {
                return (FHEMWEBDevice) devices.get(0);
            }
        } else {
            return new FHEMWEBDevice();
        }
    }

    public long getLastUpdate() {
        return sharedPreferencesService.getSharedPreferences(PREFERENCES_NAME).getLong(LAST_UPDATE_PROPERTY, 0L);
    }

    private void setLastUpdateToNow() {
        sharedPreferencesService.getSharedPreferencesEditor(PREFERENCES_NAME).putLong(LAST_UPDATE_PROPERTY, System.currentTimeMillis()).commit();
    }

    public ArrayList<String> getAvailableDeviceNames() {
        ArrayList<String> deviceNames = newArrayList();
        RoomDeviceList allRoomsDeviceList = getAllRoomsDeviceList();

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
     * @return list of all room names
     */
    public ArrayList<String> getRoomNameList() {
        RoomDeviceList roomDeviceList = getRoomDeviceList();
        if (roomDeviceList == null) return newArrayList();

        Set<String> roomNames = Sets.newHashSet();
        for (Device device : roomDeviceList.getAllDevices()) {
            if (device.isSupported() && connectionService.mayShowInCurrentConnectionType(getDeviceTypeFor(device))) {
                @SuppressWarnings("unchecked")
                List<String> deviceRooms = device.getRooms();
                roomNames.addAll(deviceRooms);
            }
        }
        roomNames.removeAll(roomDeviceList.getHiddenRooms());

        FHEMWEBDevice fhemwebDevice = findFHEMWEBDevice(roomDeviceList);
        return sortRooms(roomNames, fhemwebDevice);
    }

    ArrayList<String> sortRooms(Set<String> roomNames, FHEMWEBDevice fhemwebDevice) {
        final List<String> sortRooms = newArrayList();
        if (fhemwebDevice != null && fhemwebDevice.getSortRooms() != null) {
            sortRooms.addAll(Arrays.asList(fhemwebDevice.getSortRooms().split(SORT_ROOMS_DELIMITER)));
        }
        ArrayList<String> roomNamesCopy = newArrayList(roomNames);
        sort(roomNamesCopy, sortRoomsComparator(sortRooms));
        return roomNamesCopy;
    }

    private Comparator<String> sortRoomsComparator(final List<String> sortRooms) {
        return new Comparator<String>() {
            @Override
            public int compare(String lhs, String rhs) {
                int lhsIndex = sortRooms.indexOf(lhs);
                int rhsIndex = sortRooms.indexOf(rhs);

                if (lhsIndex == rhsIndex && lhsIndex == -1) {
                    // both not in sort list, compare based on names
                    return lhs.compareTo(rhs);
                } else if (lhsIndex != rhsIndex && lhsIndex != -1 && rhsIndex != -1) {
                    // both in sort list, compare indexes
                    return ((Integer) lhsIndex).compareTo(rhsIndex);
                } else if (lhsIndex == -1) {
                    // lhs not in sort list, rhs in sort list
                    return 1;
                } else {
                    // rhs not in sort list, lhs in sort list
                    return -1;
                }
            }
        };
    }

    /**
     * Retrieves the {@link RoomDeviceList} for a specific room name.
     *
     * @param roomName room name used for searching.
     * @return found {@link RoomDeviceList} or null
     */
    public RoomDeviceList getDeviceListForRoom(String roomName) {
        RoomDeviceList roomDeviceList = new RoomDeviceList(roomName);

        RoomDeviceList allRoomDeviceList = getRoomDeviceList();
        for (Device device : allRoomDeviceList.getAllDevices()) {
            if (device.isInRoom(roomName)) {
                roomDeviceList.addDevice(device);
            }
        }
        roomDeviceList.setHiddenGroups(allRoomDeviceList.getHiddenGroups());
        roomDeviceList.setHiddenRooms(allRoomDeviceList.getHiddenRooms());

        return roomDeviceList;
    }

    /**
     * Stores the currently loaded room device list map to the cache file.
     */
    public synchronized void storeDeviceListMap() {
        Log.i(TAG, "storeDeviceListMap() : storing device list to cache");
        Context context = AndFHEMApplication.getContext();
        try {
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(context.openFileOutput(CACHE_FILENAME, Context.MODE_PRIVATE));
            objectOutputStream.writeObject(deviceList);
        } catch (Exception e) {
            Log.e(TAG, "storeDeviceListMap() : error occurred while serializing data", e);
        }
    }

    public enum RemoteUpdateRequired {
        REQUIRED, NOT_REQUIRED
    }
}
