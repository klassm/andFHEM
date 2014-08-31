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

package li.klass.fhem.widget.deviceFunctionality;

import android.content.Context;
import android.util.Log;

import org.apache.pig.impl.util.ObjectSerializer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import li.klass.fhem.AndFHEMApplication;
import li.klass.fhem.domain.core.DeviceFunctionality;
import li.klass.fhem.exception.SerializationException;
import li.klass.fhem.util.ApplicationProperties;

import static com.google.common.base.Preconditions.checkNotNull;
import static li.klass.fhem.constants.PreferenceKeys.DEVICE_FUNCTIONALITY_ORDER_VISIBLE;
import static li.klass.fhem.constants.PreferenceKeys.DEVICE_TYPE_FUNCTIONALITY_ORDER_INVISIBLE;

public class DeviceGroupHolder {

    public static final String TAG = DeviceGroupHolder.class.getName();
    private final ApplicationProperties applicationProperties;

    private volatile boolean isLoaded = false;
    private List<DeviceFunctionality> invisible;
    private List<DeviceFunctionality> visible;
    private ArrayList<DeviceFunctionality> available;

    public DeviceGroupHolder(ApplicationProperties applicationProperties) {
        checkNotNull(applicationProperties);
        this.applicationProperties = applicationProperties;
    }

    public List<DeviceFunctionality> getVisible() {
        if (!isLoaded) load();

        List<DeviceFunctionality> all = getAvailable();
        all.removeAll(invisible);
        all.removeAll(visible);

        ArrayList<DeviceFunctionality> result = new ArrayList<DeviceFunctionality>(visible);
        result.addAll(all);

        return result;
    }

    private synchronized void load() {
        if (isLoaded) return;

        available = getAvailable();

        visible = loadVisibleDeviceTypes();
        invisible = loadInvisibleDeviceTypes();

        isLoaded = true;
    }

    private ArrayList<DeviceFunctionality> getAvailable() {
        final Context context = AndFHEMApplication.getContext();

        ArrayList<DeviceFunctionality> functionalityList = new ArrayList<DeviceFunctionality>(
                Arrays.asList(DeviceFunctionality.values()));
        Collections.sort(functionalityList, new Comparator<DeviceFunctionality>() {
            @Override
            public int compare(DeviceFunctionality me, DeviceFunctionality other) {
                return me.getCaptionText(context).compareTo(other.getCaptionText(context));
            }
        });

        return functionalityList;
    }

    private List<DeviceFunctionality> loadVisibleDeviceTypes() {
        String persistedValue = applicationProperties
                .getStringSharedPreference(DEVICE_FUNCTIONALITY_ORDER_VISIBLE, null);

        return parsePersistedValue(persistedValue, available);
    }

    private List<DeviceFunctionality> loadInvisibleDeviceTypes() {
        String persistedValue = applicationProperties
                .getStringSharedPreference(DEVICE_TYPE_FUNCTIONALITY_ORDER_INVISIBLE, null);
        return parsePersistedValue(persistedValue, new ArrayList<DeviceFunctionality>());
    }

    private List<DeviceFunctionality> parsePersistedValue(String persistedValue,
                                                          ArrayList<DeviceFunctionality> defaultValue) {
        try {
            if (persistedValue != null && !"".equals(persistedValue)) {
                return Arrays.asList((DeviceFunctionality[]) ObjectSerializer.deserialize(persistedValue));
            }
        } catch (SerializationException e) {
            Log.e(TAG, "error during deserialisation", e);
        }
        return defaultValue;
    }

    public List<DeviceFunctionality> getInvisible() {
        if (!isLoaded) load();

        return invisible;
    }
}
