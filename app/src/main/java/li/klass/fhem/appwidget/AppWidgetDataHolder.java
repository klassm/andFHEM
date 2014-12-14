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

package li.klass.fhem.appwidget;

import android.app.AlarmManager;
import android.app.IntentService;
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

import javax.inject.Inject;

import li.klass.fhem.appwidget.service.AppWidgetUpdateService;
import li.klass.fhem.appwidget.view.widget.base.AppWidgetView;
import li.klass.fhem.constants.Actions;
import li.klass.fhem.constants.BundleExtraKeys;
import li.klass.fhem.service.SharedPreferencesService;
import li.klass.fhem.util.ApplicationProperties;
import li.klass.fhem.util.NetworkState;

import static java.lang.Integer.parseInt;
import static li.klass.fhem.constants.PreferenceKeys.ALLOW_REMOTE_UPDATE;
import static li.klass.fhem.service.room.RoomListService.NEVER_UPDATE_PERIOD;

public class AppWidgetDataHolder {
    public static final String WIDGET_UPDATE_INTERVAL_PREFERENCES_KEY_WLAN = "WIDGET_UPDATE_INTERVAL_WLAN";
    public static final String WIDGET_UPDATE_INTERVAL_PREFERENCES_KEY_MOBILE = "WIDGET_UPDATE_INTERVAL_MOBILE";
    static final String SAVE_PREFERENCE_NAME = AppWidgetDataHolder.class.getName();
    private static final String TAG = AppWidgetDataHolder.class.getName();

    @Inject
    ApplicationProperties applicationProperties;

    @Inject
    SharedPreferencesService sharedPreferencesService;

    public void updateAllWidgets(final Context context, final boolean allowRemoteUpdate) {
        Set<String> appWidgetIds = getAllAppWidgetIds();
        for (String appWidgetId : appWidgetIds) {
            context.startService(getRedrawWidgetIntent(context, parseInt(appWidgetId), allowRemoteUpdate));
        }
    }

    Set<String> getAllAppWidgetIds() {
        SharedPreferences sharedPreferences = getSavedPreferences();
        Map<String, ?> allEntries = sharedPreferences.getAll();

        assert allEntries != null;

        return allEntries.keySet();
    }

    private Intent getRedrawWidgetIntent(Context context, int appWidgetId, boolean allowRemoteUpdate) {
        return new Intent(Actions.REDRAW_WIDGET)
                .setClass(context, AppWidgetUpdateService.class)
                .putExtra(BundleExtraKeys.APP_WIDGET_ID, appWidgetId)
                .putExtra(BundleExtraKeys.ALLOW_REMOTE_UPDATES, allowRemoteUpdate);
    }

    SharedPreferences getSavedPreferences() {
        return sharedPreferencesService.getSharedPreferences(SAVE_PREFERENCE_NAME);
    }


    public AppWidgetView getAppWidgetView(WidgetConfiguration configuration) {
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

    public void scheduleUpdateIntent(Context context, WidgetConfiguration widgetConfiguration,
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
        Intent updateIntent = getRedrawWidgetIntent(context, widgetId, true);

        return PendingIntent.getService(context, widgetId * (-1),
                updateIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public long getConnectionDependentUpdateInterval(Context context) {
        long updateInterval;

        if (!NetworkState.isConnected(context)) {
            updateInterval = NEVER_UPDATE_PERIOD;
        } else if (NetworkState.isConnectedMobile(context)) {
            updateInterval = getWidgetUpdateIntervalFor(WIDGET_UPDATE_INTERVAL_PREFERENCES_KEY_MOBILE);
        } else {
            updateInterval = getWidgetUpdateIntervalFor(WIDGET_UPDATE_INTERVAL_PREFERENCES_KEY_WLAN);
        }

        return updateInterval;
    }

    public Optional<WidgetConfiguration> getWidgetConfiguration(int widgetId) {
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
        SharedPreferences.Editor edit = sharedPreferencesService.getSharedPreferencesEditor(SAVE_PREFERENCE_NAME);
        String value = widgetConfiguration.toSaveString();
        edit.putString(String.valueOf(widgetConfiguration.widgetId), value);
        edit.apply();
    }

    private int getWidgetUpdateIntervalFor(String key) {
        String value = applicationProperties.getStringSharedPreference(key, "3600");
        int intValue = parseInt(value);
        return intValue * 1000;
    }
}
