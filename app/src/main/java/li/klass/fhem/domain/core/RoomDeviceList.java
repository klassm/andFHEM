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

    private Map<DeviceType, HashSet<Device>> deviceMap = new HashMap<DeviceType, HashSet<Device>>();

    public static final String ALL_DEVICES_ROOM = "ALL_DEVICES_LIST";

    public RoomDeviceList(String roomName) {
        this.roomName = roomName;
    }

    public <T extends Device> List<T> getDevicesOfType(DeviceType type) {
        Set<T> deviceSet = getOrCreateDeviceList(type);
        List<T> deviceList = new ArrayList<T>();
        for (T device : deviceSet) {
            if (device.isSupported()) {
                deviceList.add(device);
            }
        }

        Collections.sort(deviceList);
        return deviceList;
    }

    public <T extends Device> void addDevice(T device) {
        if (device == null) return;
        DeviceType deviceType = DeviceType.getDeviceTypeFor(device);
        getOrCreateDeviceList(deviceType).add(device);

        if (deviceType.mayShowInCurrentConnectionType()) {
            onlyContainsDoNotShowDevices = false;
        }
    }

    public <T extends Device> void removeDevice(T device) {
        deviceMap.get(DeviceType.getDeviceTypeFor(device)).remove(device);

        for (DeviceType deviceType : deviceMap.keySet()) {
            if (! deviceType.mayShowInCurrentConnectionType()) continue;
            if (deviceMap.get(deviceType).size() > 0) return;
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
    private <T extends Device> Set<T> getOrCreateDeviceList(DeviceType deviceType) {
        if (!deviceMap.containsKey(deviceType)) {
            deviceMap.put(deviceType, new HashSet<Device>());
        }
        return (Set<T>) deviceMap.get(deviceType);
    }

    public String getRoomName() {
        return roomName;
    }
}
