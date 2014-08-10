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
import android.appwidget.AppWidgetHost;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.RemoteViews;

import com.google.common.base.Optional;

import java.util.Map;
import java.util.Set;

import li.klass.fhem.appwidget.service.AppWidgetUpdateService;
import li.klass.fhem.appwidget.view.widget.base.AppWidgetView;
import li.klass.fhem.constants.Actions;
import li.klass.fhem.constants.BundleExtraKeys;
import li.klass.fhem.util.ApplicationProperties;
import li.klass.fhem.util.NetworkState;
import li.klass.fhem.util.SharedPreferencesUtil;

import static li.klass.fhem.constants.PreferenceKeys.ALLOW_REMOTE_UPDATE;
import static li.klass.fhem.service.room.RoomListService.NEVER_UPDATE_PERIOD;
import static li.klass.fhem.util.SharedPreferencesUtil.SHARED_PREFERENCES_UTIL;

public class AppWidgetDataHolder {
    public static final String WIDGET_UPDATE_INTERVAL_PREFERENCES_KEY_WLAN = "WIDGET_UPDATE_INTERVAL_WLAN";

    public static final String WIDGET_UPDATE_INTERVAL_PREFERENCES_KEY_MOBILE = "WIDGET_UPDATE_INTERVAL_MOBILE";
    public static final AppWidgetDataHolder INSTANCE = new AppWidgetDataHolder();
    static final String SAVE_PREFERENCE_NAME = AppWidgetDataHolder.class.getName();
    private static final String TAG = AppWidgetDataHolder.class.getName();
    private SharedPreferencesUtil sharedPreferencesUtil = SHARED_PREFERENCES_UTIL;
    private ApplicationProperties applicationProperties = ApplicationProperties.INSTANCE;

    AppWidgetDataHolder() {
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
        intent.setClass(context, AppWidgetUpdateService.class);
        intent.putExtra(BundleExtraKeys.APP_WIDGET_ID, appWidgetId);
        intent.putExtra(BundleExtraKeys.ALLOW_REMOTE_UPDATES, allowRemoteUpdate);
        context.startService(intent);
    }

    public void updateWidgetInCurrentThread(final AppWidgetManager appWidgetManager, final Context context,
                                            final int appWidgetId, final boolean allowRemoteUpdate) {
        Optional<WidgetConfiguration> widgetConfigurationOptional = getWidgetConfiguration(appWidgetId);

        if (widgetConfigurationOptional.isPresent()) {
            WidgetConfiguration configuration = widgetConfigurationOptional.get();

            final AppWidgetView widgetView = getAppWidgetView(configuration);

            long updateInterval = getConnectionDependentUpdateInterval(context);
            scheduleUpdateIntent(context, configuration, false, updateInterval);

            boolean doRemoteWidgetUpdates = applicationProperties.getBooleanSharedPreference(ALLOW_REMOTE_UPDATE, true);
            long viewCreateUpdateInterval = doRemoteWidgetUpdates && allowRemoteUpdate ? updateInterval : NEVER_UPDATE_PERIOD;
            RemoteViews content = widgetView.createView(context, configuration, viewCreateUpdateInterval);

            try {
                appWidgetManager.updateAppWidget(appWidgetId, content);
            } catch (Exception e) {
                Log.e(TAG, "something strange happened during appwidget update", e);
            }
        } else {
            deleteWidget(context, appWidgetId);
        }
    }

    AppWidgetView getAppWidgetView(WidgetConfiguration configuration) {
        return configuration.widgetType.widgetView;
    }

    public void deleteWidget(Context context, int appWidgetId) {
        Log.d(AppWidgetDataHolder.class.getName(), String.format("deleting widget for id %d", appWidgetId));

        SharedPreferences preferences = getSavedPreferences();
        String key = String.valueOf(appWidgetId);
        if (preferences.contains(key)) {
            preferences.edit().remove(key).apply();

            AppWidgetHost host = getAppWidgetHost(context);
            host.deleteAppWidgetId(appWidgetId);
        }

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        cancelUpdating(context, appWidgetId, alarmManager);
    }

    AppWidgetHost getAppWidgetHost(Context context) {
        return new AppWidgetHost(context, 0);
    }

    private void cancelUpdating(Context context, int appWidgetId, AlarmManager alarmManager) {
        PendingIntent updatePendingIntent = updatePendingIndentForWidgetId(context, appWidgetId);
        alarmManager.cancel(updatePendingIntent);
    }

    private void scheduleUpdateIntent(Context context, WidgetConfiguration widgetConfiguration,
                                      boolean updateImmediately, long widgetUpdateInterval) {
        if (widgetUpdateInterval > 0) {
            Log.d(TAG, String.format("scheduling widget update %s => %s ", widgetConfiguration.toString(), (widgetUpdateInterval / 1000) + "s"));

            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            PendingIntent pendingIntent = updatePendingIndentForWidgetId(context, widgetConfiguration.widgetId);
            long now = System.currentTimeMillis();
            long firstRun = updateImmediately ? now : now + widgetUpdateInterval;

            cancelUpdating(context, widgetConfiguration.widgetId, alarmManager);
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, firstRun, widgetUpdateInterval, pendingIntent);
        }
    }

    private PendingIntent updatePendingIndentForWidgetId(Context context, int widgetId) {
        Intent updateIntent = new Intent(Actions.REDRAW_WIDGET);
        updateIntent.putExtra(BundleExtraKeys.APP_WIDGET_ID, widgetId);
        updateIntent.putExtra(BundleExtraKeys.ALLOW_REMOTE_UPDATES, true);

        return PendingIntent.getService(context, widgetId * (-1),
                updateIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    long getConnectionDependentUpdateInterval(Context context) {
        long updateInterval;

        if (!NetworkState.isConnected(context)) {
            updateInterval = NEVER_UPDATE_PERIOD;
        } else  if (NetworkState.isConnectedMobile(context)) {
            updateInterval = getWidgetUpdateIntervalFor(WIDGET_UPDATE_INTERVAL_PREFERENCES_KEY_MOBILE);
        } else {
            updateInterval = getWidgetUpdateIntervalFor(WIDGET_UPDATE_INTERVAL_PREFERENCES_KEY_WLAN);
        }

        return updateInterval;
    }

    Optional<WidgetConfiguration> getWidgetConfiguration(int widgetId) {
        SharedPreferences sharedPreferences = getSavedPreferences();
        String value = sharedPreferences.getString(String.valueOf(widgetId), null);

        if (value == null) {
            return Optional.absent();
        } else {
            WidgetConfiguration configuration = WidgetConfiguration.fromSaveString(value);
            if (configuration.isOld) {
                Log.e(TAG, "updated widget " + configuration);
                saveWidgetConfigurationToPreferences(configuration);
            }
            return Optional.fromNullable(configuration);
        }
    }

    public void saveWidgetConfigurationToPreferences(WidgetConfiguration widgetConfiguration) {
        SharedPreferences.Editor edit = sharedPreferencesUtil.getSharedPreferencesEditor(SAVE_PREFERENCE_NAME);
        String value = widgetConfiguration.toSaveString();
        edit.putString(String.valueOf(widgetConfiguration.widgetId), value);
        edit.apply();
    }

    Set<String> getAllAppWidgetIds() {
        SharedPreferences sharedPreferences = getSavedPreferences();
        Map<String,?> allEntries = sharedPreferences.getAll();

        assert allEntries != null;

        return allEntries.keySet();
    }

    SharedPreferences getSavedPreferences() {
        return sharedPreferencesUtil.getSharedPreferences(SAVE_PREFERENCE_NAME);
    }

    private int getWidgetUpdateIntervalFor(String key) {
        String value = ApplicationProperties.INSTANCE.getStringSharedPreference(key, "3600");
        int intValue = Integer.parseInt(value);
        return intValue * 1000;
    }
}
