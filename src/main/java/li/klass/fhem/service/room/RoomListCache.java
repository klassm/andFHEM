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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import li.klass.fhem.AndFHEMApplication;
import li.klass.fhem.domain.FHEMWEBDevice;
import li.klass.fhem.domain.core.RoomDeviceList;
import li.klass.fhem.service.connection.ConnectionService;
import li.klass.fhem.util.ApplicationProperties;
import li.klass.fhem.util.CloseableUtil;
import li.klass.fhem.util.preferences.SharedPreferencesService;

import static li.klass.fhem.service.room.RoomListService.LAST_UPDATE_PROPERTY;
import static li.klass.fhem.service.room.RoomListService.PREFERENCES_NAME;

public class RoomListCache {
    private static final Logger LOG = LoggerFactory.getLogger(RoomListCache.class);
    public static final String DEFAULT_FHEMWEB_QUALIFIER = "andFHEM";
    private final SharedPreferencesService sharedPreferencesService;

    private String connectionId;
    private volatile RoomDeviceList cachedRoomList;
    private volatile boolean fileStoreNotFilled = false;

    ApplicationProperties applicationProperties;
    ConnectionService connectionService;

    public RoomListCache(String connectionId, ApplicationProperties applicationProperties, ConnectionService connectionService, SharedPreferencesService sharedPreferencesService) {
        this.connectionId = connectionId;
        this.applicationProperties = applicationProperties;
        this.connectionService = connectionService;
        this.sharedPreferencesService = sharedPreferencesService;
    }

    public synchronized boolean storeDeviceListMap(RoomDeviceList roomDeviceList, Context context) {
        if (roomDeviceList == null) {
            LOG.info("storeDeviceListMap() : won't store device list, as empty");
            return false;
        }
        storeDeviceListMapInternal(roomDeviceList, context);
        return true;
    }

    public synchronized void clearRoomDeviceList(Context context) {
        storeDeviceListMapInternal(new RoomDeviceList(RoomDeviceList.ALL_DEVICES_ROOM), context);
    }

    private void storeDeviceListMapInternal(RoomDeviceList roomDeviceList, Context context) {
        fillHiddenRoomsAndHiddenGroups(roomDeviceList, findFHEMWEBDevice(roomDeviceList, context));
        cachedRoomList = roomDeviceList;
        LOG.info("storeDeviceListMap() : storing device list to cache");
        long startLoad = System.currentTimeMillis();
        ObjectOutputStream objectOutputStream = null;
        try {
            objectOutputStream = new ObjectOutputStream(new BufferedOutputStream(context.openFileOutput(getFileName(), Context.MODE_PRIVATE)));
            objectOutputStream.writeObject(roomDeviceList);
            fileStoreNotFilled = false;
            LOG.info("storeDeviceListMap() : storing device list to cache completed after {} ms",
                    (System.currentTimeMillis() - startLoad));
        } catch (Exception e) {
            LOG.error("storeDeviceListMap() : error occurred while writing data to disk", e);
        } finally {
            CloseableUtil.close(objectOutputStream);
        }
    }

    private String getFileName() {
        return connectionId + ".serverCache";
    }

    private void fillHiddenRoomsAndHiddenGroups(RoomDeviceList newRoomDeviceList,
                                                FHEMWEBDevice fhemwebDevice) {
        if (newRoomDeviceList == null) return;

        newRoomDeviceList.setHiddenGroups(fhemwebDevice.getHiddenGroups());
        newRoomDeviceList.setHiddenRooms(fhemwebDevice.getHiddenRooms());
    }


    private FHEMWEBDevice findFHEMWEBDevice(RoomDeviceList allRoomDeviceList, Context context) {
        return new FHEMWebDeviceInRoomDeviceListSupplier(applicationProperties, connectionService, allRoomDeviceList, context).get();
    }

    /**
     * Loads the currently cached room device list map data from the file storage.
     *
     * @return cached room device list map
     */
    @SuppressWarnings("unchecked")
    public RoomDeviceList getCachedRoomDeviceListMap() {
        if (cachedRoomList != null || fileStoreNotFilled) {
            return cachedRoomList;
        }
        synchronized (this) {
            if (cachedRoomList != null || fileStoreNotFilled) {
                return cachedRoomList;
            }

            ObjectInputStream objectInputStream = null;
            try {
                LOG.info("getCachedRoomDeviceListMap() : fetching device list from cache");
                long startLoad = System.currentTimeMillis();

                objectInputStream = new ObjectInputStream(new BufferedInputStream(AndFHEMApplication.getContext().openFileInput(getFileName())));
                cachedRoomList = (RoomDeviceList) objectInputStream.readObject();
                LOG.info("getCachedRoomDeviceListMap() : loading device list from cache completed after {} ms",
                        (System.currentTimeMillis() - startLoad));
                if (cachedRoomList != null && cachedRoomList.isEmptyOrOnlyContainsDoNotShowDevices()) {
                    cachedRoomList = null;
                    fileStoreNotFilled = true;
                }
            } catch (Exception e) {
                LOG.info("getCachedRoomDeviceListMap() : error occurred while de-serializing data", e);
                fileStoreNotFilled = true;
                return null;
            } finally {
                CloseableUtil.close(objectInputStream);
            }
        }

        return cachedRoomList;
    }

    public long getLastUpdate(Context context) {
        return sharedPreferencesService.getPreferences(PREFERENCES_NAME, context).getLong(LAST_UPDATE_PROPERTY + "_" + connectionId, 0L);
    }
}
