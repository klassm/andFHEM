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

package li.klass.fhem.appwidget.view.widget.medium;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import li.klass.fhem.R;
import li.klass.fhem.appwidget.WidgetConfiguration;
import li.klass.fhem.appwidget.view.widget.base.DeviceAppWidgetView;
import li.klass.fhem.constants.Actions;
import li.klass.fhem.constants.BundleExtraKeys;
import li.klass.fhem.domain.core.Device;
import li.klass.fhem.domain.core.ToggleableDevice;
import li.klass.fhem.service.intent.DeviceIntentService;

public class OnOffWidgetView extends DeviceAppWidgetView {
    @Override
    public int getWidgetName() {
        return R.string.widget_onOff;
    }

    @Override
    protected int getContentView() {
        return R.layout.appwidget_on_off;
    }

    @Override
    protected void fillWidgetView(Context context, RemoteViews view, Device<?> device, WidgetConfiguration widgetConfiguration) {
        ToggleableDevice<?> toggleable = (ToggleableDevice) device;

        boolean isOn = toggleable.isOnRespectingInvertHook();

        view.setTextViewText(R.id.widgetOnButton, toggleable.getEventMapStateFor(toggleable.getOnStateName()));
        view.setTextViewText(R.id.widgetOffButton, toggleable.getEventMapStateFor(toggleable.getOffStateName()));

        int backgroundColor = isOn ? R.color.android_green : android.R.color.white;
        view.setInt(R.id.widgetOnButton, "setBackgroundColor", context.getResources().getColor(backgroundColor));

        Intent onIntent = new Intent(Actions.DEVICE_SET_STATE);
        onIntent.setClass(context, DeviceIntentService.class);
        onIntent.putExtra(BundleExtraKeys.DEVICE_NAME, toggleable.getName());
        onIntent.putExtra(BundleExtraKeys.DEVICE_TARGET_STATE, toggleable.getOnStateName());
        PendingIntent onPendingIntent = PendingIntent.getService(context, widgetConfiguration.widgetId,
                onIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        view.setOnClickPendingIntent(R.id.widgetOnButton, onPendingIntent);

        Intent offIntent = new Intent(Actions.DEVICE_SET_STATE);
        offIntent.setClass(context, DeviceIntentService.class);
        offIntent.putExtra(BundleExtraKeys.DEVICE_NAME, toggleable.getName());
        offIntent.putExtra(BundleExtraKeys.DEVICE_TARGET_STATE, toggleable.getOffStateName());
        PendingIntent offPendingIntent = PendingIntent.getService(context, -1 * widgetConfiguration.widgetId,
                offIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        view.setOnClickPendingIntent(R.id.widgetOffButton, offPendingIntent);

        openDeviceDetailPageWhenClicking(R.id.deviceName, view, device, widgetConfiguration);
    }

    @Override
    public boolean supports(Device<?> device) {
        return device instanceof ToggleableDevice;
    }
}
