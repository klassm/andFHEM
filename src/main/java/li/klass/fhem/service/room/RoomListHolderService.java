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

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Lists;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;
import javax.inject.Singleton;

import li.klass.fhem.AndFHEMApplication;
import li.klass.fhem.domain.FHEMWEBDevice;
import li.klass.fhem.domain.core.DeviceType;
import li.klass.fhem.domain.core.FhemDevice;
import li.klass.fhem.domain.core.RoomDeviceList;
import li.klass.fhem.service.connection.ConnectionService;
import li.klass.fhem.util.ApplicationProperties;
import li.klass.fhem.util.CloseableUtil;

import static com.google.common.collect.FluentIterable.from;
import static li.klass.fhem.constants.PreferenceKeys.FHEMWEB_DEVICE_NAME;

@Singleton
public class RoomListHolderService {
    private static final Logger LOG = LoggerFactory.getLogger(RoomListHolderService.class);
    public static final String CACHE_FILENAME = "cache.obj";
    public static final String DEFAULT_FHEMWEB_QUALIFIER = "andFHEM";

    @Inject
    ApplicationProperties applicationProperties;

    @Inject
    ConnectionService connectionService;

    private volatile RoomDeviceList cachedRoomList;
    private volatile boolean fileStoreNotFilled = false;

    @Inject
    public RoomListHolderService() {
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
            objectOutputStream = new ObjectOutputStream(new BufferedOutputStream(context.openFileOutput(CACHE_FILENAME, Context.MODE_PRIVATE)));
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

    private void fillHiddenRoomsAndHiddenGroups(RoomDeviceList newRoomDeviceList,
                                                FHEMWEBDevice fhemwebDevice) {
        if (newRoomDeviceList == null) return;

        newRoomDeviceList.setHiddenGroups(fhemwebDevice.getHiddenGroups());
        newRoomDeviceList.setHiddenRooms(fhemwebDevice.getHiddenRooms());
    }


    FHEMWEBDevice findFHEMWEBDevice(RoomDeviceList allRoomDeviceList, Context context) {
        List<FhemDevice> devicesOfType = allRoomDeviceList == null ?
                Lists.<FhemDevice>newArrayList() : allRoomDeviceList.getDevicesOfType(DeviceType.FHEMWEB);
        return findFHEMWEBDevice(devicesOfType, context);
    }

    FHEMWEBDevice findFHEMWEBDevice(List<FhemDevice> devices, Context context) {
        if (devices.isEmpty()) return new FHEMWEBDevice();

        String qualifier = StringUtils.stripToNull(applicationProperties.getStringSharedPreference(FHEMWEB_DEVICE_NAME, null, context));

        if (qualifier == null) {
            int port = connectionService.getPortOfSelectedConnection(context);
            Optional<FhemDevice> match = from(devices).filter(predicateFHEMWEBDeviceForPort(port)).first();
            if (match.isPresent()) {
                return (FHEMWEBDevice) match.get();
            }
        }

        qualifier = (qualifier == null ? DEFAULT_FHEMWEB_QUALIFIER : qualifier).toUpperCase(Locale.getDefault());

        Optional<FhemDevice> match = from(devices).filter(predicateFHEMWEBDeviceForQualifier(qualifier)).first();
        if (match.isPresent()) {
            return (FHEMWEBDevice) match.get();
        }
        return (FHEMWEBDevice) devices.get(0);
    }

    private Predicate<FhemDevice> predicateFHEMWEBDeviceForQualifier(final String qualifier) {
        return new Predicate<FhemDevice>() {
            @Override
            public boolean apply(FhemDevice device) {
                return device instanceof FHEMWEBDevice && device.getName() != null
                        && device.getName().toUpperCase(Locale.getDefault()).contains(qualifier);
            }
        };
    }

    private Predicate<FhemDevice> predicateFHEMWEBDeviceForPort(final int port) {
        return new Predicate<FhemDevice>() {
            @Override
            public boolean apply(FhemDevice device) {
                if (!(device instanceof FHEMWEBDevice)) return false;
                FHEMWEBDevice fhemwebDevice = (FHEMWEBDevice) device;
                return fhemwebDevice.getPort().equals(port + "");
            }
        };
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

                objectInputStream = new ObjectInputStream(new BufferedInputStream(AndFHEMApplication.getContext().openFileInput(CACHE_FILENAME)));
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
}
