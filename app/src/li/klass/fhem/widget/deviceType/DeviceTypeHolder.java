/*
 * AndFHEM - Open Source Android application to control a FHEM home automation
 *  server.
 *
 *  Copyright (c) 2012, Matthias Klass or third-party contributors as
 *  indicated by the @author tags or express copyright attribution
 *  statements applied by the authors.  All third-party contributions are
 *  distributed under license by Red Hat Inc.
 *
 *  This copyrighted material is made available to anyone wishing to use, modify,
 *  copy, or redistribute it subject to the terms and conditions of the GNU GENERAL PUBLICLICENSE, as published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *  or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU GENERAL PUBLIC LICENSE
 *  for more details.
 *
 *  You should have received a copy of the GNU GENERAL PUBLIC LICENSE
 *  along with this distribution; if not, write to:
 *    Free Software Foundation, Inc.
 *    51 Franklin Street, Fifth Floor
 */

package li.klass.fhem.widget.deviceType;

import android.util.Log;
import li.klass.fhem.constants.PreferenceKeys;
import li.klass.fhem.domain.core.DeviceType;
import li.klass.fhem.exception.SerializationException;
import li.klass.fhem.util.ApplicationProperties;
import org.apache.pig.impl.util.ObjectSerializer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DeviceTypeHolder {

    private volatile boolean isLoaded = false;
    private List<DeviceType> invisibleDeviceTypes;
    private List<DeviceType> visibleDeviceTypes;

    private synchronized void load() {
        if (isLoaded) return;

        visibleDeviceTypes = loadVisibleDeviceTypes();
        invisibleDeviceTypes = loadInvisibleDeviceTypes();

        isLoaded = true;
    }

    public List<DeviceType> getVisibleDeviceTypes() {
        if (! isLoaded) load();

        List<DeviceType> allDevices = new ArrayList<DeviceType>(Arrays.asList(DeviceType.values()));
        allDevices.removeAll(invisibleDeviceTypes);
        allDevices.removeAll(visibleDeviceTypes);

        ArrayList<DeviceType> result = new ArrayList<DeviceType>(visibleDeviceTypes);
        result.addAll(allDevices);

        return result;
    }

    public List<DeviceType> getInvisibleDeviceTypes() {
        if (! isLoaded) load();

        return invisibleDeviceTypes;
    }

    private List<DeviceType> loadVisibleDeviceTypes() {
        String persistedValue = ApplicationProperties.INSTANCE.getStringSharedPreference(PreferenceKeys.DEVICE_TYPE_ORDER_VISIBLE, null);
        return parsePersistedValue(persistedValue, DeviceType.values());
    }

    private List<DeviceType> loadInvisibleDeviceTypes() {
        String persistedValue = ApplicationProperties.INSTANCE.getStringSharedPreference(PreferenceKeys.DEVICE_TYPE_ORDER_INVISIBLE, null);
        return parsePersistedValue(persistedValue, new DeviceType[0]);
    }

    private List<DeviceType> parsePersistedValue(String persistedValue, DeviceType[] defaultValue) {
        try {
        if (persistedValue != null && ! "".equals(persistedValue)) {
            return Arrays.asList((DeviceType[]) ObjectSerializer.deserialize(persistedValue));
        }
        } catch (SerializationException e) {
            Log.e(DeviceTypeHolder.class.getName(), "error during deserialisation", e);
        }
        return Arrays.asList(defaultValue);
    }
}
