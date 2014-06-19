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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.collect.Lists.newArrayList;
import static li.klass.fhem.domain.core.DeviceType.getDeviceTypeFor;

/**
 * Class to hold devices for a certain room.
 */
public class RoomDeviceList implements Serializable, Cloneable {

    /**
     * Name of the room.
     */
    private String roomName;

    /**
     * Actual devices.
     */
    private Map<String, HashSet<Device>> deviceMap = new HashMap<String, HashSet<Device>>();

    /**
     * Name of the room that contains _all_ devices.
     */
    public static final String ALL_DEVICES_ROOM = "ALL_DEVICES_LIST";

    private List<String> hiddenRooms = newArrayList();

    private List<String> hiddenGroups = newArrayList();

    /**
     * Creates a new holder for a given room name.
     * @param roomName room.
     */
    public RoomDeviceList(String roomName) {
        this.roomName = roomName;
    }

    /**
     * Gets an amount of devices for a given functionality. Devices that may not be shown within
     * the current connection or devices that are not supported are not included into the return
     * value.
     *
     * @param functionality device functionality to filter.
     * @param <T> class of the returned device list.
     * @return list of devices matching the functionality.
     */
    public <T extends Device<T>> List<T> getDevicesOfFunctionality(String functionality) {
        return getDevicesOfFunctionality(functionality, true);
    }

    /**
     * Gets devices of a certain group.
     *
     * @param group group to filter.
     * @param respectMayShowAndSupported set the parameter to false to also include devices that
     *                                   may not be shown within the current connection.
     * @param <T> class of the returned device list.
     * @return list of devices matching the group.
     */
    public <T extends Device> List<T> getDevicesOfFunctionality(String group,
                                                                boolean respectMayShowAndSupported) {
        Set<T> deviceSet = getOrCreateDeviceList(group);
        List<T> deviceList = new ArrayList<T>();
        for (T device : deviceSet) {
            DeviceType deviceType = getDeviceTypeFor(device);
            if (! respectMayShowAndSupported ||
                    (device.isSupported() && deviceType.mayShowInCurrentConnectionType())) {
                deviceList.add(device);
            }
        }

        Collections.sort(deviceList);
        return deviceList;
    }

    @SuppressWarnings("unchecked")
    public <T extends Device> List<T> getDevicesOfType(DeviceType deviceType) {
        Set<Device> allDevices = getAllDevices();
        List<T> deviceList = new ArrayList<T>();
        for (Device device : allDevices) {
            if (getDeviceTypeFor(device) != deviceType) {
                continue;
            }

            if (device.isSupported()) {
                deviceList.add((T) device);
            }
        }

        Collections.sort(deviceList);
        return deviceList;
    }

    @SuppressWarnings("unchecked")
    public <T extends Device> void addDevice(T device) {
        if (device == null) return;
        if (! device.isSupported()) return;

        List<String> groups = device.getInternalDeviceGroupOrGroupAttributes();
        for (String group : groups) {
            getOrCreateDeviceList(group).add(device);
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends Device> void removeDevice(T device) {
        List<String> groups = device.getInternalDeviceGroupOrGroupAttributes();
        for (String group : groups) {
            deviceMap.get(group).remove(device);
        }
    }

    public Set<Device> getAllDevices() {
        Set<Device> devices = new HashSet<Device>();
        List<HashSet<Device>> devicesCollection = newArrayList(deviceMap.values());
        for (HashSet<Device> deviceHashSet : devicesCollection) {
            devices.addAll(deviceHashSet);
        }
        return Collections.unmodifiableSet(devices);
    }

    @SuppressWarnings("unchecked")
    public <D extends Device> D getDeviceFor(String deviceName) {
        Set<Device> allDevices = getAllDevices();
        for (Device allDevice : allDevices) {
            if (allDevice.getName().equals(deviceName)) {
                return (D) allDevice;
            }
        }
        return null;
    }

    public boolean isEmptyOrOnlyContainsDoNotShowDevices() {
        for (HashSet<Device> devices : deviceMap.values()) {
            for (Device device : devices) {
                if (getDeviceTypeFor(device).mayShowInCurrentConnectionType()
                        && device.isSupported()) {
                    return false;
                }
            }
        }

        return true;
    }

    @SuppressWarnings("unchecked")
    private <T extends Device> Set<T> getOrCreateDeviceList(String group) {
        if (!deviceMap.containsKey(group)) {
            deviceMap.put(group, new HashSet<Device>());
        }
        return (Set<T>) deviceMap.get(group);
    }

    public String getRoomName() {
        return roomName;
    }

    public List<String> getHiddenGroups() {
        return hiddenGroups;
    }

    public List<String> getHiddenRooms() {
        return hiddenRooms;
    }

    public void setHiddenRooms(List<String> hiddenRooms) {
        this.hiddenRooms = hiddenRooms;
    }

    public void setHiddenGroups(List<String> hiddenGroups) {
        this.hiddenGroups = hiddenGroups;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        RoomDeviceList roomDeviceList = (RoomDeviceList) super.clone();
        roomDeviceList.roomName = roomName;
        for (Device device : getAllDevices()) {
            roomDeviceList.addDevice(device);
        }

        return roomDeviceList;
    }
}
