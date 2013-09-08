/*
 * AndFHEM - Open Source Android application to control a FHEM home automation
 * server.
 *
 * Copyright (c) 2012, Matthias Klass or third-party contributors as
 * indicated by the @author tags or express copyright attribution
 * statements applied by the authors.  All third-party contributions are
 * distributed under license by Red Hat Inc.
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU GENERAL PUBLICLICENSE, as published by the Free Software Foundation.
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
 */

package li.klass.fhem.appwidget;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.util.Log;
import android.widget.RemoteViews;
import li.klass.fhem.appwidget.view.WidgetType;
import li.klass.fhem.appwidget.view.widget.AppWidgetView;
import li.klass.fhem.constants.Actions;
import li.klass.fhem.constants.BundleExtraKeys;
import li.klass.fhem.constants.ResultCodes;
import li.klass.fhem.domain.core.Device;
import li.klass.fhem.service.room.RoomListService;
import li.klass.fhem.util.ApplicationProperties;
import li.klass.fhem.util.NetworkState;

import java.util.Set;

import static li.klass.fhem.util.SharedPreferencesUtil.getSharedPreferences;
import static li.klass.fhem.util.SharedPreferencesUtil.getSharedPreferencesEditor;

public class AppWidgetDataHolder {
    public static final AppWidgetDataHolder INSTANCE = new AppWidgetDataHolder();
    private static final String preferenceName = AppWidgetDataHolder.class.getName();
    public static final String WIDGET_UPDATE_INTERVAL_PREFERENCES_KEY_WLAN = "WIDGET_UPDATE_INTERVAL_WLAN";
    public static final String WIDGET_UPDATE_INTERVAL_PREFERENCES_KEY_MOBILE = "WIDGET_UPDATE_INTERVAL_MOBILE";
    public static final String TAG = AppWidgetDataHolder.class.getName();
    private String SAVE_SEPARATOR = "#";

    private AppWidgetDataHolder() {
    }

    public void updateAllWidgets(final Context context, final boolean allowRemoteUpdate) {
        Log.d(AppWidgetDataHolder.class.getName(), "update all widgets!");
        new AsyncTask<String, String, String>() {
            @Override
            protected String doInBackground(String... voids) {
                Set<String> appWidgetIds = getSharedPreferences(preferenceName).getAll().keySet();
                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);

                for (String appWidgetId : appWidgetIds) {
                    updateWidgetInCurrentThread(appWidgetManager, context, Integer.parseInt(appWidgetId), allowRemoteUpdate);
                }
                return null;
            }
        }.doInBackground("");
    }

    public void updateWidget(final AppWidgetManager appWidgetManager, final Context context, final int appWidgetId,
                             final boolean allowRemoteUpdate) {
        new AsyncTask<String, String, String>() {
            @Override
            protected String doInBackground(String... voids) {
                updateWidgetInCurrentThread(appWidgetManager, context, Integer.parseInt(String.valueOf(appWidgetId)), allowRemoteUpdate);
                return null;
            }
        }.doInBackground("");
    }

    private void updateWidgetInCurrentThread(final AppWidgetManager appWidgetManager, final Context context,
                                             final int appWidgetId, final boolean allowRemoteUpdate) {
        final WidgetConfiguration widgetConfiguration = getWidgetConfiguration(appWidgetId);
        AppWidgetProviderInfo widgetInfo = appWidgetManager.getAppWidgetInfo(appWidgetId);
        if (widgetInfo == null) {
            Log.d(AppWidgetDataHolder.class.getName(), "cannot find widget for id " + appWidgetId);
            deleteWidget(context, appWidgetId);
            return;
        }
        if (widgetConfiguration == null) return;

        long updateInterval = getConnectionDependentUpdateInterval(context);

        final AppWidgetView widgetView = widgetConfiguration.widgetType.widgetView;

        boolean doRemoteWidgetUpdates = ApplicationProperties.INSTANCE.getBooleanSharedPreference("prefWidgetRemoteUpdate", true);

        long updatePeriod = doRemoteWidgetUpdates && allowRemoteUpdate ? updateInterval : RoomListService.NEVER_UPDATE_PERIOD;
        scheduleUpdateIntent(context, widgetConfiguration, false, updateInterval);

        Intent deviceIntent = new Intent(Actions.GET_DEVICE_FOR_NAME);
        deviceIntent.putExtra(BundleExtraKeys.DEVICE_NAME, widgetConfiguration.deviceName);
        deviceIntent.putExtra(BundleExtraKeys.UPDATE_PERIOD, updatePeriod);
        deviceIntent.putExtra(BundleExtraKeys.RESULT_RECEIVER, new ResultReceiver(new Handler()) {
            @Override
            protected void onReceiveResult(int resultCode, Bundle resultData) {
                if (resultCode == ResultCodes.SUCCESS) {
                    Device device = (Device) resultData.get(BundleExtraKeys.DEVICE);
                    if (device == null) {
                        Log.d(TAG, "cannot find device " + widgetConfiguration.deviceName);
                        return;
                    }

                    RemoteViews content = widgetView.createView(context, device, widgetConfiguration);

                    try {
                        appWidgetManager.updateAppWidget(appWidgetId, content);
//                        saveWidgetConfigurationToPreferences(context, widgetConfiguration.updatedWithCurrentUpdateTime());
                    } catch (Exception e) {
                        Log.e(TAG, "something strange happened during appwidget update", e);
                    }
                }
            }
        });
        context.startService(deviceIntent);
    }

    public void deleteWidget(Context context, int appWidgetId) {
        SharedPreferences.Editor editor = getSharedPreferencesEditor(preferenceName);
        editor.remove(String.valueOf(appWidgetId));

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        cancelUpdating(context, appWidgetId, alarmManager);
    }

    private void cancelUpdating(Context context, int appWidgetId, AlarmManager alarmManager) {
        PendingIntent updatePendingIntent = updatePendingIndentForWidgetId(context, appWidgetId);
        alarmManager.cancel(updatePendingIntent);
    }

    public void saveWidgetConfigurationToPreferences(WidgetConfiguration widgetConfiguration) {
        SharedPreferences.Editor edit = getSharedPreferencesEditor(preferenceName);
        String value = widgetConfiguration.toSaveString();
        edit.putString(String.valueOf(widgetConfiguration.widgetId), value);
        edit.commit();
    }

    private void scheduleUpdateIntent(Context context, WidgetConfiguration widgetConfiguration, boolean updateNow, long widgetUpdateInterval) {
        if (widgetUpdateInterval > 0) {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            PendingIntent pendingIntent = updatePendingIndentForWidgetId(context, widgetConfiguration.widgetId);
            long now = System.currentTimeMillis();
            long firstRun = updateNow ? now : now + widgetUpdateInterval;

            cancelUpdating(context, widgetConfiguration.widgetId, alarmManager);
            alarmManager.setRepeating(AlarmManager.RTC, firstRun, widgetUpdateInterval, pendingIntent);
        }
    }

    private PendingIntent updatePendingIndentForWidgetId(Context context, int widgetId) {
        Intent updateIntent = new Intent(Actions.WIDGET_UPDATE);
        updateIntent.putExtra(BundleExtraKeys.APP_WIDGET_ID, widgetId);

        return PendingIntent.getBroadcast(context, widgetId * (-1),
                updateIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private long getConnectionDependentUpdateInterval(Context context) {
        if (!NetworkState.isConnected(context)) return RoomListService.NEVER_UPDATE_PERIOD;
        if (NetworkState.isConnectedMobile(context)) {
            return getWidgetUpdateIntervalFor(WIDGET_UPDATE_INTERVAL_PREFERENCES_KEY_MOBILE);
        }
        return getWidgetUpdateIntervalFor(WIDGET_UPDATE_INTERVAL_PREFERENCES_KEY_WLAN);
    }

    private WidgetConfiguration getWidgetConfiguration(int widgetId) {
        SharedPreferences sharedPreferences = getSharedPreferences(preferenceName);
        String value = sharedPreferences.getString(String.valueOf(widgetId), null);
        if (value == null) return null;

        return WidgetConfiguration.fromSaveString(value);
    }

    private int getWidgetUpdateIntervalFor(String key) {
        String value = ApplicationProperties.INSTANCE.getStringSharedPreference(key, "3600");
        int intValue = Integer.parseInt(value);
        return intValue * 1000;
    }
}
