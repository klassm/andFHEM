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

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import li.klass.fhem.constants.Actions;
import li.klass.fhem.constants.BundleExtraKeys;

public abstract class AndFHEMAppWidgetProvider extends AppWidgetProvider {

    public static final String TAG = AndFHEMAppWidgetProvider.class.getName();

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        for (int appWidgetId : appWidgetIds) {
            AppWidgetDataHolder.INSTANCE.updateWidget(appWidgetManager, context, appWidgetId, true);
        }
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
        for (int appWidgetId : appWidgetIds) {
            AppWidgetDataHolder.INSTANCE.deleteWidget(context, appWidgetId);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (intent.getAction().equals(Actions.WIDGET_UPDATE)) {
            int appWidgetId = intent.getIntExtra(BundleExtraKeys.APP_WIDGET_ID, -1);
            Log.d(TAG, "update widget " + appWidgetId);

            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            if (appWidgetId != -1) {
                onUpdate(context, appWidgetManager, new int[] {appWidgetId});
            }
        } else if (intent.getAction().equals(Actions.DEVICE_LIST_REMOTE_NOTIFY)) {
            Log.i(TAG, "updating all widgets (received DEVICE_LIST_REMOTE_NOTIFY");
            AppWidgetDataHolder.INSTANCE.updateAllWidgets(context, false);
        }
    }
}
