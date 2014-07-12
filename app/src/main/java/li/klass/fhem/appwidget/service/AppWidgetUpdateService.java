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
import android.util.Log;
import android.widget.Toast;

import li.klass.fhem.AndFHEMApplication;
import li.klass.fhem.R;
import li.klass.fhem.appwidget.AppWidgetDataHolder;
import li.klass.fhem.constants.Actions;
import li.klass.fhem.constants.BundleExtraKeys;

import static li.klass.fhem.constants.Actions.DEVICE_LIST_REMOTE_NOTIFY;
import static li.klass.fhem.constants.Actions.REDRAW_WIDGET;
import static li.klass.fhem.constants.Actions.WIDGET_REQUEST_UPDATE;

public class AppWidgetUpdateService extends IntentService {

    public static final String TAG = AppWidgetUpdateService.class.getName();

    public AppWidgetUpdateService() {
        super(AppWidgetUpdateService.class.getName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String action = intent.getAction();
        boolean allowRemoteUpdates = intent.getBooleanExtra(BundleExtraKeys.ALLOW_REMOTE_UPDATES, false);

        if (REDRAW_WIDGET.equals(action)) {
            handleRedrawWidget(intent, allowRemoteUpdates);
        } else if (DEVICE_LIST_REMOTE_NOTIFY.equals(action)) {
            Log.i(TAG, "updating all widgets (received DEVICE_LIST_REMOTE_NOTIFY)");
            AppWidgetDataHolder.INSTANCE.updateAllWidgets(this, allowRemoteUpdates);
        } else if (WIDGET_REQUEST_UPDATE.equals(action)) {
            new Handler(getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(AndFHEMApplication.getContext(), R.string.widget_remote_update_started, Toast.LENGTH_LONG).show();
                }
            });
            Intent updateIntent = new Intent(Actions.DO_UPDATE);
            updateIntent.putExtra(BundleExtraKeys.DO_REFRESH, true);
            sendBroadcast(updateIntent);
        }
    }

    private void handleRedrawWidget(Intent intent, boolean allowRemoteUpdates) {
        if (! intent.hasExtra(BundleExtraKeys.APP_WIDGET_ID)) {
            return;
        }

        int widgetId = intent.getIntExtra(BundleExtraKeys.APP_WIDGET_ID, -1);
        Log.d(TAG, "updating widget " + widgetId + ", remote update is " + allowRemoteUpdates);

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);

        AppWidgetDataHolder.INSTANCE.updateWidgetInCurrentThread(appWidgetManager, this,
                widgetId, allowRemoteUpdates);
    }
}
