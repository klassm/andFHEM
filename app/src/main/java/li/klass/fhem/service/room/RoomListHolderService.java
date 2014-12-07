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

import com.google.common.collect.Lists;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;
import javax.inject.Singleton;

import li.klass.fhem.AndFHEMApplication;
import li.klass.fhem.domain.FHEMWEBDevice;
import li.klass.fhem.domain.core.Device;
import li.klass.fhem.domain.core.DeviceType;
import li.klass.fhem.domain.core.RoomDeviceList;
import li.klass.fhem.util.ApplicationProperties;
import li.klass.fhem.util.CloseableUtil;

import static li.klass.fhem.constants.PreferenceKeys.DEVICE_NAME;

@Singleton
public class RoomListHolderService {
    private static final Logger LOG = LoggerFactory.getLogger(RoomListHolderService.class);
    public static final String CACHE_FILENAME = "cache.obj";
    public static final String DEFAULT_FHEMWEB_QUALIFIER = "andFHEM";

    @Inject
    ApplicationProperties applicationProperties;

    public synchronized void storeDeviceListMap(RoomDeviceList roomDeviceList) {
        if (roomDeviceList == null || roomDeviceList.isEmptyOrOnlyContainsDoNotShowDevices()) {
            LOG.info("storeDeviceListMap() : won't store device list, as empty");
            return;
        }
        fillHiddenRoomsAndHiddenGroups(roomDeviceList, findFHEMWEBDevice(roomDeviceList));

        LOG.info("storeDeviceListMap() : storing device list to cache");
        Context context = AndFHEMApplication.getContext();
        ObjectOutputStream objectOutputStream = null;
        try {
            objectOutputStream = new ObjectOutputStream(context.openFileOutput(CACHE_FILENAME, Context.MODE_PRIVATE));
            objectOutputStream.writeObject(roomDeviceList);
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


    FHEMWEBDevice findFHEMWEBDevice(RoomDeviceList allRoomDeviceList) {
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

    /**
     * Loads the currently cached room device list map data from the file storage.
     *
     * @return cached room device list map
     */
    @SuppressWarnings("unchecked")
    RoomDeviceList getCachedRoomDeviceListMap() {
        ObjectInputStream objectInputStream = null;
        try {
            LOG.info("getCachedRoomDeviceListMap() : fetching device list from cache");
            long startLoad = System.currentTimeMillis();

            objectInputStream = new ObjectInputStream(AndFHEMApplication.getContext().openFileInput(CACHE_FILENAME));
            RoomDeviceList roomDeviceListMap = (RoomDeviceList) objectInputStream.readObject();
            LOG.info("getCachedRoomDeviceListMap() : loading device list from cache completed after {} ms",
                    (System.currentTimeMillis() - startLoad));

            if (roomDeviceListMap != null && roomDeviceListMap.isEmptyOrOnlyContainsDoNotShowDevices()) {
                return null;
            } else {
                return roomDeviceListMap;
            }
        } catch (Exception e) {
            LOG.info("getCachedRoomDeviceListMap() : error occurred while de-serializing data", e);
            return null;
        } finally {
            CloseableUtil.close(objectInputStream);
        }
    }
}
