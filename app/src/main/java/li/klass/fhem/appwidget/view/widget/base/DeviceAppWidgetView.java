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

package li.klass.fhem.appwidget.view.widget.base;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.widget.RemoteViews;

import com.google.common.base.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

import li.klass.fhem.R;
import li.klass.fhem.activities.AndFHEMMainActivity;
import li.klass.fhem.appwidget.WidgetConfiguration;
import li.klass.fhem.appwidget.WidgetConfigurationCreatedCallback;
import li.klass.fhem.appwidget.annotation.SupportsWidget;
import li.klass.fhem.appwidget.view.WidgetType;
import li.klass.fhem.constants.Actions;
import li.klass.fhem.constants.BundleExtraKeys;
import li.klass.fhem.constants.ResultCodes;
import li.klass.fhem.dagger.ForApplication;
import li.klass.fhem.domain.core.Device;
import li.klass.fhem.fragments.FragmentType;
import li.klass.fhem.service.room.RoomListService;
import li.klass.fhem.util.FhemResultReceiver;

public abstract class DeviceAppWidgetView extends AppWidgetView {

    public static final String TAG = DeviceAppWidgetView.class.getName();

    @Inject
    RoomListService roomListService;

    @Inject
    @ForApplication
    Context applicationContext;

    public static final Logger LOG = LoggerFactory.getLogger(DeviceAppWidgetView.class);

    public boolean supports(Device<?> device) {
        if (!device.getClass().isAnnotationPresent(SupportsWidget.class)) return false;

        if (!device.supportsWidget(this.getClass())) {
            return false;
        }

        SupportsWidget annotation = device.getClass().getAnnotation(SupportsWidget.class);
        Class<? extends DeviceAppWidgetView>[] supportedWidgetViews = annotation.value();
        for (Class<? extends DeviceAppWidgetView> supportedWidgetView : supportedWidgetViews) {
            if (supportedWidgetView.equals(this.getClass())) return true;
        }

        return false;
    }

    @Override
    public RemoteViews createView(Context context, WidgetConfiguration widgetConfiguration) {
        RemoteViews views = super.createView(context, widgetConfiguration);
        Log.i(TAG, "creating appwidget view for " + widgetConfiguration);

        if (shouldSetDeviceName()) {
            String deviceName = deviceNameFrom(widgetConfiguration);

            Device device = getDeviceFor(deviceName);
            if (device == null) return null;

            views.setTextViewText(R.id.deviceName, device.getWidgetName());
        }

        return views;
    }

    private String deviceNameFrom(WidgetConfiguration widgetConfiguration) {
        return widgetConfiguration.payload.get(0);
    }

    public boolean shouldSetDeviceName() {
        return true;
    }

    private Device getDeviceFor(String deviceName) {
        return roomListService.getDeviceForName(deviceName).orNull();
    }

    protected void openDeviceDetailPageWhenClicking(int viewId, RemoteViews view, Device device, WidgetConfiguration widgetConfiguration) {
        openDeviceDetailPageWhenClicking(viewId, view, device, widgetConfiguration.widgetId);
    }

    protected void openDeviceDetailPageWhenClicking(int viewId, RemoteViews view, Device device, int widgetId) {
        PendingIntent pendingIntent = createOpenDeviceDetailPagePendingIntent(device, widgetId);

        view.setOnClickPendingIntent(viewId, pendingIntent);
    }

    protected PendingIntent createOpenDeviceDetailPagePendingIntent(Device device, int widgetId) {
        Intent openIntent = createOpenDeviceDetailPageIntent(device, applicationContext);
        return PendingIntent.getActivity(applicationContext, widgetId, openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

    protected Intent createOpenDeviceDetailPageIntent(Device device, Context context) {
        Intent openIntent = new Intent(context, AndFHEMMainActivity.class);
        openIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        openIntent.putExtra(BundleExtraKeys.FRAGMENT, FragmentType.DEVICE_DETAIL);
        openIntent.putExtra(BundleExtraKeys.DEVICE_NAME, device.getName());
        openIntent.putExtra("unique", "foobar://" + SystemClock.elapsedRealtime());
        return openIntent;
    }

    @Override
    public void createWidgetConfiguration(Context context, WidgetType widgetType, int appWidgetId,
                                          WidgetConfigurationCreatedCallback callback, String... payload) {
        Optional<Device> device = roomListService.getDeviceForName(payload[0]);
        if (device.isPresent()) {
            createDeviceWidgetConfiguration(context, widgetType, appWidgetId, device.get(), callback);
        } else {
            Log.i(TAG, "cannot find device for " + payload[0]);
        }
    }

    protected void createDeviceWidgetConfiguration(Context context, WidgetType widgetType, int appWidgetId,
                                                   Device device, WidgetConfigurationCreatedCallback callback) {
        callback.widgetConfigurationCreated(new WidgetConfiguration(appWidgetId, widgetType, device.getName()));
    }

    protected void fillWidgetView(Context context, RemoteViews view,
                                  WidgetConfiguration widgetConfiguration) {
        Device<?> device = getDeviceFor(deviceNameFrom(widgetConfiguration));
        if (device != null) {
            view.setTextViewText(R.id.deviceName, device.getWidgetName());
            fillWidgetView(context, view, device, widgetConfiguration);
        } else {
            Log.i(TAG, "cannot find device for " + deviceNameFrom(widgetConfiguration));
        }
    }

    protected abstract void fillWidgetView(Context context, RemoteViews view, Device<?> device,
                                           WidgetConfiguration widgetConfiguration);
}
