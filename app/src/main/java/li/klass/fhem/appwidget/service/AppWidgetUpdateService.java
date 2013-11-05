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

import li.klass.fhem.appwidget.AppWidgetDataHolder;
import li.klass.fhem.constants.Actions;
import li.klass.fhem.constants.BundleExtraKeys;

public class AppWidgetUpdateService extends IntentService {
    public AppWidgetUpdateService() {
        super(AppWidgetUpdateService.class.getName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent.getAction() == null || ! intent.getAction().equalsIgnoreCase(Actions.REDRAW_WIDGET)) {
            return;
        }

        if (! intent.hasExtra(BundleExtraKeys.APP_WIDGET_ID) ||
                ! intent.hasExtra(BundleExtraKeys.ALLOW_REMOTE_UPDATES)) {
            return;
        }

        int widgetId = intent.getIntExtra(BundleExtraKeys.APP_WIDGET_ID, -1);
        boolean allowRemoteUpdates = intent.getBooleanExtra(BundleExtraKeys.ALLOW_REMOTE_UPDATES, false);

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);

        AppWidgetDataHolder.INSTANCE.updateWidgetInCurrentThread(appWidgetManager, this,
                widgetId, allowRemoteUpdates);
    }
}
