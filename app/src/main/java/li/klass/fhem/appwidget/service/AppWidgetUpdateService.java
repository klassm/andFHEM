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

package li.klass.fhem.appwidget.service;

import android.app.IntentService;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

import li.klass.fhem.AndFHEMApplication;
import li.klass.fhem.R;
import li.klass.fhem.appwidget.AppWidgetDataHolder;
import li.klass.fhem.appwidget.WidgetConfiguration;
import li.klass.fhem.appwidget.view.widget.base.AppWidgetView;
import li.klass.fhem.constants.Actions;
import li.klass.fhem.constants.BundleExtraKeys;
import li.klass.fhem.service.intent.RoomListIntentService;
import li.klass.fhem.util.ApplicationProperties;
import li.klass.fhem.util.FhemResultReceiver;

import static com.google.common.base.Preconditions.checkArgument;
import static li.klass.fhem.constants.Actions.REDRAW_ALL_WIDGETS;
import static li.klass.fhem.constants.Actions.REDRAW_WIDGET;
import static li.klass.fhem.constants.Actions.REMOTE_UPDATE_FINISHED;
import static li.klass.fhem.constants.Actions.WIDGET_REQUEST_UPDATE;
import static li.klass.fhem.constants.PreferenceKeys.ALLOW_REMOTE_UPDATE;
import static li.klass.fhem.service.room.RoomListService.NEVER_UPDATE_PERIOD;

public class AppWidgetUpdateService extends IntentService {

    public static final Logger LOG = LoggerFactory.getLogger(AppWidgetUpdateService.class);

    @Inject
    AppWidgetDataHolder appWidgetDataHolder;

    @Inject
    ApplicationProperties applicationProperties;


    public AppWidgetUpdateService() {
        super(AppWidgetUpdateService.class.getName());
    }

    @Override
    public void onCreate() {
        super.onCreate();
        ((AndFHEMApplication) getApplication()).inject(this);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String action = intent.getAction();
        boolean allowRemoteUpdates = intent.getBooleanExtra(BundleExtraKeys.ALLOW_REMOTE_UPDATES, false);

        if (REDRAW_WIDGET.equals(action)) {
            handleRedrawWidget(intent, allowRemoteUpdates);
        } else if (REDRAW_ALL_WIDGETS.equals(action)) {
            LOG.info("onHandleIntent() - updating all widgets (received REDRAW_ALL_WIDGETS)");
            appWidgetDataHolder.updateAllWidgets(this, allowRemoteUpdates);
        } else if (WIDGET_REQUEST_UPDATE.equals(action)) {
            new Handler(getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(AppWidgetUpdateService.this, R.string.widget_remote_update_started, Toast.LENGTH_LONG).show();
                }
            });
            Intent updateIntent = new Intent(Actions.DO_UPDATE);
            updateIntent.putExtra(BundleExtraKeys.DO_REFRESH, true);
            sendBroadcast(updateIntent);
        } else if (REMOTE_UPDATE_FINISHED.equals(action)) {
            updateWidgetAfterDeviceListReload(intent.getIntExtra(BundleExtraKeys.APP_WIDGET_ID, -1));
        }
    }

    private void handleRedrawWidget(Intent intent, boolean allowRemoteUpdates) {
        if (!intent.hasExtra(BundleExtraKeys.APP_WIDGET_ID)) {
            return;
        }

        int widgetId = intent.getIntExtra(BundleExtraKeys.APP_WIDGET_ID, -1);
        LOG.debug("handleRedrawWidget() - updating widget-id {}, remote update is {}", widgetId, allowRemoteUpdates);

        updateWidget(this, widgetId, allowRemoteUpdates);
    }

    public void updateWidget(final IntentService intentService,
                             final int appWidgetId, final boolean allowRemoteUpdate) {
        Optional<WidgetConfiguration> widgetConfigurationOptional = appWidgetDataHolder.getWidgetConfiguration(appWidgetId);

        if (!widgetConfigurationOptional.isPresent()) {
            appWidgetDataHolder.deleteWidget(intentService, appWidgetId);
            return;
        }

        final WidgetConfiguration configuration = widgetConfigurationOptional.get();

        final long updateInterval = appWidgetDataHolder.getConnectionDependentUpdateInterval(intentService);

        boolean doRemoteWidgetUpdates = applicationProperties.getBooleanSharedPreference(ALLOW_REMOTE_UPDATE, true);
        final long viewCreateUpdateInterval = doRemoteWidgetUpdates && allowRemoteUpdate ? updateInterval : NEVER_UPDATE_PERIOD;

        appWidgetDataHolder.scheduleUpdateIntent(intentService, configuration, false, updateInterval);

        startService(new Intent(Actions.UPDATE_IF_REQUIRED)
                .putExtra(BundleExtraKeys.UPDATE_PERIOD, viewCreateUpdateInterval)
                .putExtra(BundleExtraKeys.SENDER, AppWidgetUpdateService.class)
                .setClass(this, RoomListIntentService.class)
                .putExtra(BundleExtraKeys.APP_WIDGET_ID, appWidgetId));
    }

    private void updateWidgetAfterDeviceListReload(int appWidgetId) {

        Optional<WidgetConfiguration> optional = appWidgetDataHolder.getWidgetConfiguration(appWidgetId);
        checkArgument(optional.isPresent());

        final WidgetConfiguration configuration = optional.get();

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);

        final AppWidgetView widgetView = appWidgetDataHolder.getAppWidgetView(configuration);
        widgetView.attach(getApplication());

        RemoteViews content = widgetView.createView(this, configuration);

        try {
            appWidgetManager.updateAppWidget(appWidgetId, content);
        } catch (Exception e) {
            LOG.error("updateWidgetAfterDeviceListReload() - something strange happened during appwidget update", e);
        }
    }

}
