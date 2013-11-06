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

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.widget.RemoteViewsService;

import li.klass.fhem.appwidget.view.WidgetType;
import li.klass.fhem.appwidget.view.widget.base.AppWidgetView;
import li.klass.fhem.appwidget.view.widget.base.ListAppWidgetView;
import li.klass.fhem.domain.core.Device;
import li.klass.fhem.service.room.RoomListService;

import static li.klass.fhem.constants.BundleExtraKeys.APP_WIDGET_ID;
import static li.klass.fhem.constants.BundleExtraKeys.APP_WIDGET_TYPE_NAME;
import static li.klass.fhem.constants.BundleExtraKeys.DEVICE_NAME;
import static li.klass.fhem.service.room.RoomListService.NEVER_UPDATE_PERIOD;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class AppWidgetListViewUpdateRemoteViewsService extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        int appWidgetId = intent.getIntExtra(APP_WIDGET_ID, -1);
        WidgetType widgetType = WidgetType.valueOf(intent.getStringExtra(APP_WIDGET_TYPE_NAME));
        String deviceName = intent.getStringExtra(DEVICE_NAME);
        Device device = RoomListService.INSTANCE.getDeviceForName(deviceName, NEVER_UPDATE_PERIOD);

        if (appWidgetId == -1) {
            Log.e(AppWidgetListViewUpdateRemoteViewsService.class.getName(),
                    "no appwidget id given");
            return null;
        }

        AppWidgetView view = widgetType.widgetView;
        if (! (view instanceof ListAppWidgetView)) {
            Log.e(AppWidgetListViewUpdateRemoteViewsService.class.getName(),
                    "can only handle list widget views, got " + view.getClass().getName());
            return null;
        }

        ListAppWidgetView listView = (ListAppWidgetView) view;

        return listView.getRemoteViewsFactory(this, device, appWidgetId);
    }
}
