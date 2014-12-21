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

import com.google.common.base.Optional;

import javax.inject.Inject;

import li.klass.fhem.AndFHEMApplication;
import li.klass.fhem.appwidget.view.WidgetType;
import li.klass.fhem.appwidget.view.widget.base.AppWidgetView;
import li.klass.fhem.appwidget.view.widget.base.DeviceListAppWidgetView;
import li.klass.fhem.appwidget.view.widget.base.EmptyRemoteViewsFactory;
import li.klass.fhem.domain.core.Device;
import li.klass.fhem.service.room.RoomListService;

import static li.klass.fhem.constants.BundleExtraKeys.APP_WIDGET_ID;
import static li.klass.fhem.constants.BundleExtraKeys.APP_WIDGET_TYPE_NAME;
import static li.klass.fhem.constants.BundleExtraKeys.DEVICE_NAME;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class AppWidgetListViewUpdateRemoteViewsService extends RemoteViewsService {

    public static final String TAG = AppWidgetListViewUpdateRemoteViewsService.class.getName();

    @Inject
    RoomListService roomListService;

    @Override
    public void onCreate() {
        super.onCreate();
        ((AndFHEMApplication) getApplication()).inject(this);
    }

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        int appWidgetId = intent.getIntExtra(APP_WIDGET_ID, -1);
        WidgetType widgetType = WidgetType.valueOf(intent.getStringExtra(APP_WIDGET_TYPE_NAME));
        String deviceName = intent.getStringExtra(DEVICE_NAME);
        Optional<Device> device = roomListService.getDeviceForName(deviceName);
        if (!device.isPresent()) {
            Log.e(TAG, "device is null, at least in the current connection");
            return null;
        }

        if (appWidgetId == -1) {
            Log.e(TAG, "no appwidget id given");
            return null;
        }

        AppWidgetView view = widgetType.widgetView;
        if (!(view instanceof DeviceListAppWidgetView)) {
            Log.e(TAG,
                    "can only handle list widget views, got " + view.getClass().getName());

            /*
            * We may not return null here, as the source code within {@link RemoteViewsService#onBind}
            * depends on a factory which is _non_ null. This is why we simply return a factory
            * handling no data at all.
            */
            return EmptyRemoteViewsFactory.INSTANCE;
        }

        DeviceListAppWidgetView listView = (DeviceListAppWidgetView) view;

        return listView.getRemoteViewsFactory(this, device.get(), appWidgetId);
    }
}
