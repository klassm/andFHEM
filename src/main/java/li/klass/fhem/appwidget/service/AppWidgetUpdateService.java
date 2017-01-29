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
import android.os.Handler;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.google.common.base.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

import li.klass.fhem.AndFHEMApplication;
import li.klass.fhem.R;
import li.klass.fhem.appwidget.AppWidgetDataHolder;
import li.klass.fhem.appwidget.WidgetConfiguration;
import li.klass.fhem.appwidget.view.widget.base.AppWidgetView;
import li.klass.fhem.appwidget.view.widget.base.DeviceAppWidgetView;
import li.klass.fhem.constants.Actions;
import li.klass.fhem.service.intent.RoomListIntentService;
import li.klass.fhem.util.ApplicationProperties;

import static com.google.common.base.Preconditions.checkArgument;
import static li.klass.fhem.constants.Actions.REDRAW_ALL_WIDGETS;
import static li.klass.fhem.constants.Actions.REDRAW_WIDGET;
import static li.klass.fhem.constants.Actions.REMOTE_UPDATE_FINISHED;
import static li.klass.fhem.constants.Actions.WIDGET_REQUEST_UPDATE;
import static li.klass.fhem.constants.BundleExtraKeys.ALLOW_REMOTE_UPDATES;
import static li.klass.fhem.constants.BundleExtraKeys.APP_WIDGET_ID;
import static li.klass.fhem.constants.BundleExtraKeys.CONNECTION_ID;
import static li.klass.fhem.constants.BundleExtraKeys.DEVICE_NAME;
import static li.klass.fhem.constants.BundleExtraKeys.DO_REFRESH;
import static li.klass.fhem.constants.BundleExtraKeys.SENDER;
import static li.klass.fhem.constants.BundleExtraKeys.UPDATE_PERIOD;
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
        ((AndFHEMApplication) getApplication()).getDaggerComponent().inject(this);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String action = intent.getAction();
        boolean allowRemoteUpdates = intent.getBooleanExtra(ALLOW_REMOTE_UPDATES, false);

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
            updateIntent.putExtra(DO_REFRESH, true);
            sendBroadcast(updateIntent);
        } else if (REMOTE_UPDATE_FINISHED.equals(action)) {
            updateWidgetAfterDeviceListReload(intent.getIntExtra(APP_WIDGET_ID, -1));
        }
    }

    private void handleRedrawWidget(Intent intent, boolean allowRemoteUpdates) {
        if (!intent.hasExtra(APP_WIDGET_ID)) {
            return;
        }

        int widgetId = intent.getIntExtra(APP_WIDGET_ID, -1);
        LOG.debug("handleRedrawWidget() - updating widget-id {}, remote update is {}", widgetId, allowRemoteUpdates);

        updateWidget(this, widgetId, allowRemoteUpdates);
    }

    public void updateWidget(final IntentService intentService,
                             final int appWidgetId, final boolean allowRemoteUpdate) {
        Optional<WidgetConfiguration> widgetConfigurationOptional = appWidgetDataHolder.getWidgetConfiguration(appWidgetId, this);

        if (!widgetConfigurationOptional.isPresent()) {
            appWidgetDataHolder.deleteWidget(intentService, appWidgetId);
            LOG.info("updateWidget - widget with widget-id {} has been deleted", appWidgetId);
            return;
        }

        final WidgetConfiguration configuration = widgetConfigurationOptional.get();

        final long updateInterval = appWidgetDataHolder.getConnectionDependentUpdateInterval(intentService);

        boolean doRemoteWidgetUpdates = applicationProperties.getBooleanSharedPreference(ALLOW_REMOTE_UPDATE, true, this);
        final long viewCreateUpdateInterval = doRemoteWidgetUpdates && allowRemoteUpdate ? updateInterval : NEVER_UPDATE_PERIOD;

        appWidgetDataHolder.scheduleUpdateIntent(intentService, configuration, false, updateInterval);

        LOG.info("updateWidget - request widget update for widget-id {}, interval is {}, update interval is {}ms", appWidgetId, viewCreateUpdateInterval, updateInterval);

        Intent intent = new Intent(Actions.UPDATE_IF_REQUIRED)
                .putExtra(UPDATE_PERIOD, viewCreateUpdateInterval)
                .putExtra(SENDER, AppWidgetUpdateService.class)
                .setClass(this, RoomListIntentService.class)
                .putExtra(CONNECTION_ID, configuration.connectionId.orNull())
                .putExtra(APP_WIDGET_ID, appWidgetId);
        if (configuration.widgetType.widgetView instanceof DeviceAppWidgetView) {
            String deviceName = ((DeviceAppWidgetView) configuration.widgetType.widgetView).deviceNameFrom(configuration);
            intent.putExtra(DEVICE_NAME, deviceName);
        }
        startService(intent);
    }

    private void updateWidgetAfterDeviceListReload(int appWidgetId) {

        Optional<WidgetConfiguration> optional = appWidgetDataHolder.getWidgetConfiguration(appWidgetId, this);
        checkArgument(optional.isPresent());

        final WidgetConfiguration configuration = optional.get();

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);

        final AppWidgetView widgetView = appWidgetDataHolder.getAppWidgetView(configuration);
        RemoteViews content = widgetView.createView(this, configuration);

        try {
            appWidgetManager.updateAppWidget(appWidgetId, content);
        } catch (Exception e) {
            LOG.error("updateWidgetAfterDeviceListReload() - something strange happened during appwidget update", e);
        }
    }

}
