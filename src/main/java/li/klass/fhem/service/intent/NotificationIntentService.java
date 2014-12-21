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

package li.klass.fhem.service.intent;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.os.SystemClock;
import android.util.Log;

import com.google.common.base.Joiner;

import java.lang.reflect.Field;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;

import li.klass.fhem.R;
import li.klass.fhem.activities.AndFHEMMainActivity;
import li.klass.fhem.constants.Actions;
import li.klass.fhem.constants.BundleExtraKeys;
import li.klass.fhem.constants.ResultCodes;
import li.klass.fhem.dagger.ForApplication;
import li.klass.fhem.domain.core.Device;
import li.klass.fhem.domain.genericview.ShowField;
import li.klass.fhem.fragments.core.DeviceDetailFragment;
import li.klass.fhem.util.NotificationUtil;
import li.klass.fhem.util.ReflectionUtil;

import static com.google.common.collect.Maps.newHashMap;
import static li.klass.fhem.constants.BundleExtraKeys.NOTIFICATION_UPDATES;

public class NotificationIntentService extends ConvenientIntentService {

    public static final int NO_UPDATES = 0;
    public static final int ALL_UPDATES = 1;
    public static final int STATE_UPDATES = 2;
    private static final String PREFERENCES_NAME = "deviceNotifications";

    @Inject
    @ForApplication
    Context applicationContext;

    public NotificationIntentService() {
        super(NotificationIntentService.class.getName());
    }

    @Override
    protected STATE handleIntent(Intent intent, long updatePeriod, ResultReceiver resultReceiver) {
        String deviceName = intent.getStringExtra(BundleExtraKeys.DEVICE_NAME);

        if (intent.getAction().equals(Actions.NOTIFICATION_SET_FOR_DEVICE)) {
            int updateType = intent.getIntExtra(BundleExtraKeys.NOTIFICATION_UPDATES, 0);
            setDeviceNotification(deviceName, updateType);
        } else if (intent.getAction().equals(Actions.NOTIFICATION_TRIGGER)) {
            @SuppressWarnings("unchecked")
            Map<String, String> updateMap = (Map<String, String>) intent.getSerializableExtra(BundleExtraKeys.UPDATE_MAP);
            Device<?> device = (Device<?>) intent.getSerializableExtra(BundleExtraKeys.DEVICE);
            boolean vibrate = intent.getBooleanExtra(BundleExtraKeys.VIBRATE, false);

            deviceNotification(deviceName, updateMap, device, vibrate);
        } else if (intent.getAction().equals(Actions.NOTIFICATION_GET_FOR_DEVICE)) {
            int value = getPreferences().getInt(deviceName, 0);

            Bundle result = new Bundle();
            result.putInt(NOTIFICATION_UPDATES, value);

            resultReceiver.send(ResultCodes.SUCCESS, result);
        }
        return STATE.SUCCESS;
    }

    public void rename(String deviceName, String deviceNewName) {
        SharedPreferences preferences = getPreferences();
        if (preferences.contains(deviceName)) {
            int value = preferences.getInt(deviceName, 0);
            preferences.edit().remove(deviceName).putInt(deviceNewName, value).apply();
        }
    }

    private void setDeviceNotification(String deviceName, int updateType) {
        getPreferences().edit().putInt(deviceName, updateType).commit();
    }

    private void deviceNotification(String deviceName, Map<String, String> updateMap, Device<?> device, boolean vibrate) {
        int value = getPreferences().getInt(deviceName, 0);
        if (device.triggerStateNotificationOnAttributeChange()) {
            updateMap.clear();
            updateMap.put("STATE", "updateMe");
        }

        if (isValueAllUpdates(value)) {
            generateNotification(device, updateMap, vibrate);
        } else if (isValueStateUpdates(value) && updateMap.containsKey("STATE")) {
            Map<String, String> values = newHashMap();
            values.put("STATE", updateMap.get("STATE"));
            generateNotification(device, values, vibrate);
        }
    }

    private boolean isValueAllUpdates(int value) {
        return value == ALL_UPDATES;
    }

    private boolean isValueStateUpdates(int value) {
        return value == STATE_UPDATES;
    }

    private SharedPreferences getPreferences() {
        return applicationContext.getSharedPreferences(PREFERENCES_NAME, Activity.MODE_PRIVATE);
    }

    private void generateNotification(Device device, Map<String, String> updateMap, boolean vibrate) {
        Map<String, String> notificationMap = rebuildUpdateMap(device, updateMap);
        String deviceName = device.getAliasOrName();

        Intent openIntent = new Intent(this, AndFHEMMainActivity.class);
        openIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        openIntent.putExtra(BundleExtraKeys.FRAGMENT_NAME, DeviceDetailFragment.class.getName());
        openIntent.putExtra(BundleExtraKeys.DEVICE_NAME, deviceName);
        openIntent.putExtra("unique", "foobar://" + SystemClock.elapsedRealtime());
        PendingIntent pendingIntent = PendingIntent.getActivity(this, deviceName.hashCode(), openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        String text;
        String stateKey = getString(R.string.state);
        if (notificationMap.size() == 1 && notificationMap.containsKey(stateKey)) {
            text = notificationMap.get(stateKey);
        } else {
            text = Joiner.on(",").withKeyValueSeparator(" : ").join(notificationMap);
        }

        NotificationUtil.notify(this, deviceName.hashCode(), pendingIntent, deviceName, text,
                deviceName, vibrate);
    }

    private Map<String, String> rebuildUpdateMap(Device device, Map<String, String> updateMap) {
        Map<String, String> newMap = newHashMap();

        Class<? extends Device> deviceClass = device.getClass();
        replaceFieldForClass(deviceClass, device, updateMap, newMap);

        newMap.putAll(updateMap);
        return newMap;
    }

    private void replaceFieldForClass(Class deviceClass, Device device, Map<String, String> updateMap,
                                      Map<String, String> newMap) {
        for (Field field : deviceClass.getDeclaredFields()) {
            String fieldName = field.getName().toUpperCase(Locale.getDefault());
            if (updateMap.containsKey(fieldName)) {

                try {
                    field.setAccessible(true);
                    String fieldValue = ReflectionUtil.getFieldValueAsString(device, field);
                    String name = figureOutNewName(field, fieldName);
                    newMap.put(name, fieldValue);

                    updateMap.remove(fieldName);
                } catch (Exception e) {
                    Log.e(NotificationIntentService.class.getName(), "cannot access " + field.getName(), e);
                }
            }
        }

        Class<?> superclass = deviceClass.getSuperclass();
        if (superclass != null && Device.class.isAssignableFrom(superclass) && updateMap.size() > 0) {
            replaceFieldForClass(superclass, device, updateMap, newMap);
        }
    }

    private String figureOutNewName(Field field, String fieldName) {
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
            name = getString(id);
        }
        return name;
    }
}
