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

import com.google.common.base.Optional;
import com.google.common.collect.Sets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;
import javax.inject.Singleton;

import li.klass.fhem.appwidget.service.AppWidgetUpdateService;
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
import li.klass.fhem.service.intent.NotificationIntentService;
import li.klass.fhem.service.intent.RoomListUpdateIntentService;
import li.klass.fhem.util.ApplicationProperties;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.sort;
import static li.klass.fhem.constants.Actions.DISMISS_EXECUTING_DIALOG;
import static li.klass.fhem.constants.Actions.REDRAW_ALL_WIDGETS;
import static li.klass.fhem.constants.BundleExtraKeys.DO_REFRESH;
import static li.klass.fhem.constants.BundleExtraKeys.UPDATE_PERIOD;
import static li.klass.fhem.domain.core.DeviceType.AT;
import static li.klass.fhem.domain.core.DeviceType.getDeviceTypeFor;
import static li.klass.fhem.util.DateFormatUtil.toReadable;

@Singleton
public class RoomListService extends AbstractService {

    private static final Logger LOG = LoggerFactory.getLogger(RoomListService.class);

    public static final String PREFERENCES_NAME = RoomListService.class.getName();

    public static final String LAST_UPDATE_PROPERTY = "LAST_UPDATE";

    public static final long NEVER_UPDATE_PERIOD = 0;
    public static final long ALWAYS_UPDATE_PERIOD = -1;

    public static final String SORT_ROOMS_DELIMITER = " ";

    private final AtomicBoolean remoteUpdateInProgress = new AtomicBoolean(false);

    private List<Intent> resendIntents = newArrayList();

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

    @Inject
    RoomListHolderService roomListHolderService;

    public void parseReceivedDeviceStateMap(String deviceName, Map<String, String> updateMap,
                                            boolean vibrateUponNotification) {

        Optional<Device> deviceOptional = getDeviceForName(deviceName);
        if (!deviceOptional.isPresent()) {
            return;
        }

        Device device = deviceOptional.get();
        deviceListParser.fillDeviceWith(device, updateMap);

        LOG.info("parseReceivedDeviceStateMap()  : updated {} with {} new values!", device.getName(), updateMap.size());

        applicationContext.startService(new Intent(Actions.NOTIFICATION_TRIGGER)
                .setClass(applicationContext, NotificationIntentService.class)
                .putExtra(BundleExtraKeys.DEVICE_NAME, deviceName)
                .putExtra(BundleExtraKeys.DEVICE, device)
                .putExtra(BundleExtraKeys.UPDATE_MAP, (Serializable) updateMap)
                .putExtra(BundleExtraKeys.VIBRATE, vibrateUponNotification));

        boolean updateWidgets = applicationProperties.getBooleanSharedPreference(PreferenceKeys.GCM_WIDGET_UPDATE, false);
        if (updateWidgets) {
            sendBroadcastWithAction(REDRAW_ALL_WIDGETS);

            applicationContext.startService(new Intent(REDRAW_ALL_WIDGETS)
                    .setClass(applicationContext, DeviceIntentService.class));
        }
    }

    /**
     * Looks for a device with a given name.
     *
     * @param deviceName name of the device
     * @return found device or null
     */
    @SuppressWarnings("unchecked")
    public <T extends Device<T>> Optional<T> getDeviceForName(String deviceName) {
        return Optional.fromNullable((T) getAllRoomsDeviceList().getDeviceFor(deviceName));
    }

    /**
     * Retrieves a {@link RoomDeviceList} containing all devices, not only the devices of a specific room.
     * The room device list will be a copy of the actual one. Thus, any modifications will have no effect!
     *
     * @return {@link RoomDeviceList} containing all devices
     */
    public RoomDeviceList getAllRoomsDeviceList() {
        RoomDeviceList originalRoomDeviceList = getRoomDeviceList();
        return new RoomDeviceList(originalRoomDeviceList);
    }

    /**
     * Loads the currently cached {@link li.klass.fhem.domain.core.RoomDeviceList}. If the cached
     * device list has not yet been loaded, it will be loaded from the cache object.
     * <p/>
     * <p>Watch out: Any modifications will be saved within the internal representation. Don't use
     * this method from client code!</p>
     *
     * @return Currently cached {@link li.klass.fhem.domain.core.RoomDeviceList}.
     */
    public RoomDeviceList getRoomDeviceList() {

        if (deviceList == null) {
            deviceList = roomListHolderService.getCachedRoomDeviceListMap();
        }
        return deviceList;
    }

    public void resetUpdateProgress() {
        LOG.debug("resetUpdateProgress()");
        remoteUpdateInProgress.set(false);
        resendIntents = newArrayList();
        sendBroadcastWithAction(DISMISS_EXECUTING_DIALOG);
    }

    /**
     * Method will check if a remote update of the current device list is required. This is
     * determined by two indicators:
     * <ul>
     * <li>After loading the cached device map, the cached map is still null. Effectively
     * this means that no devices had been cached.</li>
     * <li>The update period indicates that we have to update the device map.</li>
     * </ul>
     * <p/>
     * <p>
     * When finding out that we have to remotely update the device list, the current request
     * (as intent) is cached and an intent to {@link li.klass.fhem.service.intent.RoomListUpdateIntentService}
     * is sent. When the remote update completes, we will get an answer effectively calling
     * {#remoteUpdateFinished}. The method will resent all cached intents, resulting in
     * answers to waiting requests.
     * </p>
     * <p>
     * By caching calling intents that all want to remotely load device lists, we make
     * sure that only one remote update is concurrently executed. Also, we do not queue
     * remote requests, as they would load the same content from the server.
     * </p>
     *
     * @param intent       calling intent (that might request a remote update)
     * @param updatePeriod update period (that might indicate a necessary remote update)
     * @return {@link li.klass.fhem.service.room.RoomListService.RemoteUpdateRequired#REQUIRED} if the
     * calling intent will be cached and resend when the remote update has completed, otherwise
     * {@link li.klass.fhem.service.room.RoomListService.RemoteUpdateRequired#NOT_REQUIRED}
     */
    public RemoteUpdateRequired updateRoomDeviceListIfRequired(Intent intent, long updatePeriod) {
        if (deviceList == null) {
            deviceList = roomListHolderService.getCachedRoomDeviceListMap();
        }

        boolean requiresUpdate = shouldUpdate(updatePeriod) || deviceList == null;
        if (requiresUpdate) {
            LOG.info("updateRoomDeviceListIfRequired() - requiring update, add pending action: {}", intent.getAction());
            resendIntents.add(createResendIntent(intent));
            if (remoteUpdateInProgress.compareAndSet(false, true)) {
                applicationContext.startService(new Intent(Actions.DO_REMOTE_UPDATE)
                        .setClass(applicationContext, RoomListUpdateIntentService.class));
            }
            LOG.debug("updateRoomDeviceListIfRequired() - remote update is required");
            return RemoteUpdateRequired.REQUIRED;
        } else {
            LOG.debug("updateRoomDeviceListIfRequired() - remote update is not required");
            return RemoteUpdateRequired.NOT_REQUIRED;
        }
    }

    /**
     * Entry point for completed remote updates. See {@link #updateRoomDeviceListIfRequired} for
     * details on the process.
     */
    public void remoteUpdateFinished() {
        try {
            LOG.info("remoteUpdateFinished() - starting after actions");

            deviceList = roomListHolderService.getCachedRoomDeviceListMap();

            for (Intent resendIntent : resendIntents) {
                resend(resendIntent);
            }
            resendIntents.clear();

            LOG.info("remoteUpdateFinished() - requesting redraw of all appwidgets");

            applicationContext.sendBroadcast(new Intent(Actions.REDRAW_ALL_WIDGETS));
            applicationContext.startService(new Intent(Actions.REDRAW_ALL_WIDGETS)
                    .setClass(applicationContext, AppWidgetUpdateService.class)
                    .putExtra(BundleExtraKeys.ALLOW_REMOTE_UPDATES, false));

            LOG.info("remoteUpdateFinished() - remote update finished, device list is {}");
        } finally {
            LOG.info("remoteUpdateFinished() - finished, dismissing executing dialog");
            resetUpdateProgress();
        }
    }

    private void resend(Intent intent) {
        LOG.info("resend() : resending {}", intent.getAction());
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
            LOG.debug("shouldUpdate() : recommend update, as updatePeriod is set to ALWAYS_UPDATE");
            return true;
        }
        if (updatePeriod == NEVER_UPDATE_PERIOD) {
            LOG.debug("shouldUpdate() : recommend no update, as updatePeriod is set to NEVER_UPDATE");
            return false;
        }

        long lastUpdate = getLastUpdate();
        boolean shouldUpdate = lastUpdate + updatePeriod < System.currentTimeMillis();

        LOG.debug("shouldUpdate() : recommend {} update (lastUpdate: {}, updatePeriod: {} min)", (!shouldUpdate ? "no " : "to"), toReadable(lastUpdate), (updatePeriod / 1000 / 60));

        return shouldUpdate;
    }

    public long getLastUpdate() {
        return sharedPreferencesService.getSharedPreferences(PREFERENCES_NAME).getLong(LAST_UPDATE_PROPERTY, 0L);
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
            DeviceType type = getDeviceTypeFor(device);
            if (device.isSupported() && connectionService.mayShowInCurrentConnectionType(type) && type != AT) {
                @SuppressWarnings("unchecked")
                List<String> deviceRooms = device.getRooms();
                roomNames.addAll(deviceRooms);
            }
        }
        roomNames.removeAll(roomDeviceList.getHiddenRooms());

        FHEMWEBDevice fhemwebDevice = roomListHolderService.findFHEMWEBDevice(roomDeviceList);
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
        if (allRoomDeviceList != null) {
            for (Device device : allRoomDeviceList.getAllDevices()) {
                if (device.isInRoom(roomName)) {
                    roomDeviceList.addDevice(device);
                }
            }
            roomDeviceList.setHiddenGroups(allRoomDeviceList.getHiddenGroups());
            roomDeviceList.setHiddenRooms(allRoomDeviceList.getHiddenRooms());
        }

        return roomDeviceList;
    }

    public enum RemoteUpdateRequired {
        REQUIRED, NOT_REQUIRED
    }
}
