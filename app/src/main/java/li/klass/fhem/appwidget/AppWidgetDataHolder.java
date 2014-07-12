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
import android.util.Log;
import android.widget.RemoteViews;

import java.util.Map;
import java.util.Set;

import li.klass.fhem.appwidget.view.widget.base.AppWidgetView;
import li.klass.fhem.appwidget.view.widget.base.DeviceAppWidgetView;
import li.klass.fhem.constants.Actions;
import li.klass.fhem.constants.BundleExtraKeys;
import li.klass.fhem.service.room.RoomListService;
import li.klass.fhem.util.ApplicationProperties;
import li.klass.fhem.util.NetworkState;

import static li.klass.fhem.util.SharedPreferencesUtil.getSharedPreferences;
import static li.klass.fhem.util.SharedPreferencesUtil.getSharedPreferencesEditor;

public class AppWidgetDataHolder {
    public static final AppWidgetDataHolder INSTANCE = new AppWidgetDataHolder();
    private static final String preferenceName = AppWidgetDataHolder.class.getName();
    public static final String WIDGET_UPDATE_INTERVAL_PREFERENCES_KEY_WLAN = "WIDGET_UPDATE_INTERVAL_WLAN";
    public static final String WIDGET_UPDATE_INTERVAL_PREFERENCES_KEY_MOBILE = "WIDGET_UPDATE_INTERVAL_MOBILE";
    public static final String TAG = AppWidgetDataHolder.class.getName();

    private AppWidgetDataHolder() {
    }

    public void updateAllWidgets(final Context context, final boolean allowRemoteUpdate) {
        Set<String> appWidgetIds = getAllAppWidgetIds();
        for (String appWidgetId : appWidgetIds) {
            updateWidget( context, Integer.parseInt(appWidgetId), allowRemoteUpdate);
        }
    }

    public void updateWidget(final Context context, final int appWidgetId,
                             final boolean allowRemoteUpdate) {
        Intent intent = new Intent(Actions.REDRAW_WIDGET);
        intent.putExtra(BundleExtraKeys.APP_WIDGET_ID, appWidgetId);
        intent.putExtra(BundleExtraKeys.ALLOW_REMOTE_UPDATES, allowRemoteUpdate);
        context.startService(intent);
    }

    public void updateWidgetInCurrentThread(final AppWidgetManager appWidgetManager, final Context context,
                                            final int appWidgetId, final boolean allowRemoteUpdate) {
        final WidgetConfiguration widgetConfiguration = getWidgetConfiguration(appWidgetId);
        AppWidgetProviderInfo widgetInfo = appWidgetManager.getAppWidgetInfo(appWidgetId);
        if (widgetInfo == null) {
            Log.d(AppWidgetDataHolder.class.getName(), "cannot not find widget for id " + appWidgetId);
            deleteWidget(context, appWidgetId);
            return;
        }
        if (widgetConfiguration == null) return;

        long updateInterval = getConnectionDependentUpdateInterval(context);

        final AppWidgetView widgetView = widgetConfiguration.widgetType.widgetView;

        boolean doRemoteWidgetUpdates = ApplicationProperties.INSTANCE.getBooleanSharedPreference("prefWidgetRemoteUpdate", true);

        long updatePeriod = doRemoteWidgetUpdates && allowRemoteUpdate ? updateInterval : RoomListService.NEVER_UPDATE_PERIOD;
        Log.d(TAG, "remote widget pref: " + doRemoteWidgetUpdates + ", allow remote update: " + allowRemoteUpdate +  " => update period " + updatePeriod);
        scheduleUpdateIntent(context, widgetConfiguration, false, updateInterval);

        RemoteViews content = widgetView.createView(context, widgetConfiguration, updatePeriod);

        try {
            appWidgetManager.updateAppWidget(appWidgetId, content);
        } catch (Exception e) {
            Log.e(TAG, "something strange happened during appwidget update", e);
        }
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
        Intent updateIntent = new Intent(Actions.REDRAW_WIDGET);
        updateIntent.putExtra(BundleExtraKeys.APP_WIDGET_ID, widgetId);
        updateIntent.putExtra(BundleExtraKeys.ALLOW_REMOTE_UPDATES, true);

        return PendingIntent.getService(context, widgetId * (-1),
                updateIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private long getConnectionDependentUpdateInterval(Context context) {
        long updateInterval;

        if (!NetworkState.isConnected(context)) {
            updateInterval = RoomListService.NEVER_UPDATE_PERIOD;
        } else  if (NetworkState.isConnectedMobile(context)) {
            updateInterval = getWidgetUpdateIntervalFor(WIDGET_UPDATE_INTERVAL_PREFERENCES_KEY_MOBILE);
        } else {
            updateInterval = getWidgetUpdateIntervalFor(WIDGET_UPDATE_INTERVAL_PREFERENCES_KEY_WLAN);
        }

        return updateInterval;
    }

    private WidgetConfiguration getWidgetConfiguration(int widgetId) {
        SharedPreferences sharedPreferences = getSharedPreferences(preferenceName);
        String value = sharedPreferences.getString(String.valueOf(widgetId), null);
        if (value == null) return null;

        WidgetConfiguration configuration = WidgetConfiguration.fromSaveString(value);
        if (configuration.isOld) {
            Log.e(TAG, "updated widget " + configuration);
            saveWidgetConfigurationToPreferences(configuration);
        }
        return configuration;
    }

    private Set<String> getAllAppWidgetIds() {
        SharedPreferences sharedPreferences = getSharedPreferences(preferenceName);
        Map<String,?> allEntries = sharedPreferences.getAll();

        assert allEntries != null;

        return allEntries.keySet();
    }

    private int getWidgetUpdateIntervalFor(String key) {
        String value = ApplicationProperties.INSTANCE.getStringSharedPreference(key, "3600");
        int intValue = Integer.parseInt(value);
        return intValue * 1000;
    }
}
