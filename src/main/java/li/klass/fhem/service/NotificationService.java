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

package li.klass.fhem.service;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemClock;

import com.google.common.base.Joiner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import li.klass.fhem.R;
import li.klass.fhem.activities.AndFHEMMainActivity;
import li.klass.fhem.constants.BundleExtraKeys;
import li.klass.fhem.domain.core.FhemDevice;
import li.klass.fhem.domain.genericview.ShowField;
import li.klass.fhem.fragments.FragmentType;
import li.klass.fhem.util.NotificationUtil;
import li.klass.fhem.util.ReflectionUtil;

import static com.google.common.collect.Maps.newHashMap;

@Singleton
public class NotificationService {
    public static final int NO_UPDATES = 0;
    public static final int ALL_UPDATES = 1;
    public static final int STATE_UPDATES = 2;

    public static final String PREFERENCES_NAME = "deviceNotifications";

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationService.class);

    @Inject
    public NotificationService() {
    }

    public void rename(String deviceName, String deviceNewName, Context context) {
        SharedPreferences preferences = getPreferences(context);
        if (preferences.contains(deviceName)) {
            int value = preferences.getInt(deviceName, 0);
            preferences.edit().remove(deviceName).putInt(deviceNewName, value).apply();
        }
    }

    public void setDeviceNotification(String deviceName, int updateType, Context context) {
        getPreferences(context).edit().putInt(deviceName, updateType).apply();
    }

    public void deviceNotification(String deviceName, Map<String, String> updateMap, FhemDevice device, boolean vibrate, Context context) {
        int value = getPreferences(context).getInt(deviceName, 0);
        if (device.triggerStateNotificationOnAttributeChange()) {
            updateMap.clear();
            updateMap.put("STATE", "updateMe");
        }

        if (isValueAllUpdates(value)) {
            generateNotification(device, updateMap, vibrate, context);
        } else if (isValueStateUpdates(value) && updateMap.containsKey("STATE")) {
            Map<String, String> values = newHashMap();
            values.put("STATE", updateMap.get("STATE"));
            generateNotification(device, values, vibrate, context);
        }
    }

    public int forDevice(Context context, String deviceName) {
        return getPreferences(context).getInt(deviceName, 0);
    }

    private boolean isValueAllUpdates(int value) {
        return value == ALL_UPDATES;
    }

    private boolean isValueStateUpdates(int value) {
        return value == STATE_UPDATES;
    }


    private void generateNotification(FhemDevice device, Map<String, String> updateMap, boolean vibrate, Context context) {
        Map<String, String> notificationMap = rebuildUpdateMap(device, updateMap, context);
        String deviceName = device.getName();

        Intent openIntent = new Intent(context, AndFHEMMainActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                .putExtra(BundleExtraKeys.FRAGMENT, FragmentType.DEVICE_DETAIL)
                .putExtra(BundleExtraKeys.DEVICE_NAME, deviceName)
                .putExtra("unique", "foobar://" + SystemClock.elapsedRealtime());
        PendingIntent pendingIntent = PendingIntent.getActivity(context, deviceName.hashCode(), openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        String text;
        String stateKey = context.getString(R.string.state);
        if (notificationMap.size() == 1 && notificationMap.containsKey(stateKey)) {
            text = notificationMap.get(stateKey);
        } else {
            text = Joiner.on(",").withKeyValueSeparator(" : ").join(notificationMap);
        }

        NotificationUtil.notify(context, deviceName.hashCode(), pendingIntent, deviceName, text,
                deviceName, vibrate);
    }

    private Map<String, String> rebuildUpdateMap(FhemDevice device, Map<String, String> updateMap, Context context) {
        Map<String, String> newMap = newHashMap();

        Class<? extends FhemDevice> deviceClass = device.getClass();
        replaceFieldForClass(deviceClass, device, updateMap, newMap, context);

        newMap.putAll(updateMap);
        return newMap;
    }


    private void replaceFieldForClass(Class deviceClass, FhemDevice device, Map<String, String> updateMap,
                                      Map<String, String> newMap, Context context) {
        for (Field field : deviceClass.getDeclaredFields()) {
            String fieldName = field.getName().toUpperCase(Locale.getDefault());
            if (updateMap.containsKey(fieldName)) {

                try {
                    field.setAccessible(true);
                    String fieldValue = ReflectionUtil.getFieldValueAsString(device, field);
                    String name = figureOutNewName(field, fieldName, context);
                    newMap.put(name, fieldValue);

                    updateMap.remove(fieldName);
                } catch (Exception e) {
                    LOGGER.error("replaceFieldForClass() - cannot access " + field.getName(), e);
                }
            }
        }

        Class<?> superclass = deviceClass.getSuperclass();
        if (superclass != null && FhemDevice.class.isAssignableFrom(superclass) && updateMap.size() > 0) {
            replaceFieldForClass(superclass, device, updateMap, newMap, context);
        }
    }

    private String figureOutNewName(Field field, String fieldName, Context context) {
        ShowField annotation = field.getAnnotation(ShowField.class);
        int id = -1;
        if (annotation == null && fieldName.equals("STATE")) {
            id = R.string.state;
        } else if (annotation != null) {
            id = annotation.description().getId();
        }

        String name;
        if (id == -1) {
            name = fieldName;
        } else {
            name = context.getString(id);
        }
        return name;
    }

    private SharedPreferences getPreferences(Context context) {
        return context.getSharedPreferences(PREFERENCES_NAME, Activity.MODE_PRIVATE);
    }
}
