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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RoomDeviceList implements Serializable {

    private String roomName;
    private boolean onlyContainsDoNotShowDevices = true;

    private Map<DeviceFunctionality, HashSet<Device>> deviceMap = new HashMap<DeviceFunctionality, HashSet<Device>>();

    public static final String ALL_DEVICES_ROOM = "ALL_DEVICES_LIST";

    public RoomDeviceList(String roomName) {
        this.roomName = roomName;
    }

    public <T extends Device> List<T> getDevicesOfFunctionality(DeviceFunctionality functionality) {
        Set<T> deviceSet = getOrCreateDeviceList(functionality);
        List<T> deviceList = new ArrayList<T>();
        for (T device : deviceSet) {
            DeviceType deviceType = DeviceType.getDeviceTypeFor(device);
            if (device.isSupported() && deviceType.mayShowInCurrentConnectionType()) {
                deviceList.add(device);
            }
        }

        Collections.sort(deviceList);
        return deviceList;
    }

    public <T extends Device> List<T> getDevicesOfType(DeviceType deviceType) {
        Set<Device> allDevices = getAllDevices();
        List<T> deviceList = new ArrayList<T>();
        for (Device device : allDevices) {
            if (DeviceType.getDeviceTypeFor(device) != deviceType) {
                continue;
            }

            if (device.isSupported()) {
                deviceList.add((T) device);
            }
        }

        Collections.sort(deviceList);
        return deviceList;
    }

    public <T extends Device> void addDevice(T device) {
        if (device == null) return;

        DeviceType deviceType = DeviceType.getDeviceTypeFor(device);
        if (deviceType.mayShowInCurrentConnectionType()) {
            onlyContainsDoNotShowDevices = false;
        }

        DeviceFunctionality functionality = device.getDeviceFunctionality();
        getOrCreateDeviceList(functionality).add(device);

    }

    public <T extends Device> void removeDevice(T device) {
        HashSet<Device> functionalitySet = deviceMap.get(device.getDeviceFunctionality());
        functionalitySet.remove(device);

        for (Map.Entry<DeviceFunctionality, HashSet<Device>> functionalityEntry : deviceMap.entrySet()) {
            for (Device deviceItem : functionalityEntry.getValue()) {
                if (DeviceType.getDeviceTypeFor(deviceItem).mayShowInCurrentConnectionType()) {
                    onlyContainsDoNotShowDevices = false;
                    return;
                }
            }
        }

        onlyContainsDoNotShowDevices = true;
    }

    public Set<Device> getAllDevices() {
        Set<Device> devices = new HashSet<Device>();
        Collection<HashSet<Device>> devicesCollection = deviceMap.values();
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
        return onlyContainsDoNotShowDevices;
    }

    @SuppressWarnings("unchecked")
    private <T extends Device> Set<T> getOrCreateDeviceList(DeviceFunctionality functionality) {
        if (!deviceMap.containsKey(functionality)) {
            deviceMap.put(functionality, new HashSet<Device>());
        }
        return (Set<T>) deviceMap.get(functionality);
    }

    public String getRoomName() {
        return roomName;
    }
}
