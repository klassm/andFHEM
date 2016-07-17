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

package li.klass.fhem.domain.core;

import android.content.Context;
import android.util.Log;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import li.klass.fhem.domain.AtDevice;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.newHashSet;
import static li.klass.fhem.domain.core.DeviceType.getDeviceTypeFor;

/**
 * Class to hold devices for a certain room.
 */
public class RoomDeviceList implements Serializable {

    /**
     * Name of the room that contains _all_ devices.
     */
    public static final String ALL_DEVICES_ROOM = "ALL_DEVICES_LIST";
    /**
     * Name of the room.
     */
    private String roomName;
    /**
     * Actual devices.
     */
    private Map<String, HashSet<FhemDevice>> deviceMap = newHashMap();
    private List<String> hiddenRooms = newArrayList();

    private List<String> hiddenGroups = newArrayList();

    /**
     * Creates a new holder for a given room name.
     *
     * @param roomName room.
     */
    public RoomDeviceList(String roomName) {
        this.roomName = roomName;
    }

    public RoomDeviceList(RoomDeviceList roomDeviceList, Context context) {
        if (roomDeviceList != null) {
            this.roomName = roomDeviceList.roomName;
            for (FhemDevice device : roomDeviceList.getAllDevices()) {
                addDevice(device, context);
            }
        }
    }

    /**
     * Gets an amount of devices for a given functionality. Devices that may not be shown within
     * the current connection or devices that are not supported are not included into the return
     * value.
     *
     * @param functionality device functionality to filter.
     * @param <T>           class of the returned device list.
     * @return list of devices matching the functionality.
     */
    public <T extends FhemDevice<T>> List<T> getDevicesOfFunctionality(String functionality) {
        return getDevicesOfFunctionality(functionality, true);
    }

    /**
     * Gets devices of a certain group. At-devices are always excluded, as they are not shown
     * in any kind of normal view.
     *
     * @param group            group to filter.
     * @param respectSupported set the parameter to false to also include devices that
     *                         are not supported
     * @param <T>              class of the returned device list.
     * @return list of devices matching the group.
     */
    public <T extends FhemDevice> List<T> getDevicesOfFunctionality(String group,
                                                                    boolean respectSupported) {
        Set<T> deviceSet = getOrCreateDeviceList(group);
        List<T> deviceList = newArrayList();
        for (T device : deviceSet) {
            if (!(device instanceof AtDevice) && (!respectSupported ||
                    device.isSupported())) {
                deviceList.add(device);
            }
        }

        try {
            Collections.sort(deviceList);
        } catch (Exception e) {
            Log.e(RoomDeviceList.class.getName(), "cannot sort", e);
        }
        return deviceList;
    }

    @SuppressWarnings("unchecked")
    private <T extends FhemDevice> Set<T> getOrCreateDeviceList(String group) {
        if (!deviceMap.containsKey(group)) {
            deviceMap.put(group, new HashSet<FhemDevice>());
        }
        return (Set<T>) deviceMap.get(group);
    }

    @SuppressWarnings("unchecked")
    public <T extends FhemDevice> List<T> getDevicesOfType(DeviceType deviceType) {
        Set<FhemDevice> allDevices = getAllDevices();
        List<T> deviceList = newArrayList();
        for (FhemDevice device : newArrayList(allDevices)) {
            if (device == null || getDeviceTypeFor(device) != deviceType) {
                continue;
            }

            if (device.isSupported()) {
                deviceList.add((T) device);
            }
        }

        Collections.sort(deviceList);
        return deviceList;
    }

    public Set<FhemDevice> getAllDevices() {
        Set<FhemDevice> devices = newHashSet();
        List<HashSet<FhemDevice>> devicesCollection = newArrayList(deviceMap.values());
        for (HashSet<FhemDevice> deviceHashSet : devicesCollection) {
            devices.addAll(deviceHashSet);
        }
        return Collections.unmodifiableSet(devices);
    }

    @SuppressWarnings("unchecked")
    public <T extends FhemDevice> void removeDevice(T device, Context context) {
        List<String> groups = device.getInternalDeviceGroupOrGroupAttributes(context);
        for (String group : groups) {
            deviceMap.get(group).remove(device);
        }
    }

    @SuppressWarnings("unchecked")
    public <D extends FhemDevice> D getDeviceFor(String deviceName) {
        Set<FhemDevice> allDevices = getAllDevices();
        for (FhemDevice allDevice : allDevices) {
            if (allDevice.getName().equals(deviceName)) {
                return (D) allDevice;
            }
        }
        return null;
    }

    public boolean isEmptyOrOnlyContainsDoNotShowDevices() {
        for (Set<FhemDevice> devices : deviceMap.values()) {
            for (FhemDevice device : devices) {
                if (device.isSupported()) {
                    return false;
                }
            }
        }

        return true;
    }

    public String getRoomName() {
        return roomName;
    }

    public List<String> getHiddenGroups() {
        return hiddenGroups;
    }

    public void setHiddenGroups(List<String> hiddenGroups) {
        this.hiddenGroups = hiddenGroups;
    }

    public List<String> getHiddenRooms() {
        return hiddenRooms;
    }

    public void setHiddenRooms(List<String> hiddenRooms) {
        this.hiddenRooms = hiddenRooms;
    }

    @SuppressWarnings("unchecked")
    public <T extends FhemDevice> RoomDeviceList addDevice(T device, Context context) {
        if (device == null || !device.isSupported()) {
            return this;
        }

        List<String> groups = device.getInternalDeviceGroupOrGroupAttributes(context);
        for (String group : groups) {
            Set<FhemDevice> groupList = getOrCreateDeviceList(group);
            groupList.remove(device);
            groupList.add(device);
        }

        return this;
    }

    public RoomDeviceList addAllDevicesOf(RoomDeviceList roomDeviceList, Context context) {
        Set<FhemDevice> allDevices = roomDeviceList.getAllDevices();
        for (FhemDevice device : allDevices) {
            FhemDevice foundDevice = getDeviceFor(device.getName());
            addDevice(device, context);
            if (foundDevice != null && device.getSvgGraphDefinitions().isEmpty()) {
                device.setSvgGraphDefinitions(foundDevice.getSvgGraphDefinitions());
            }
        }
        return this;
    }
}
