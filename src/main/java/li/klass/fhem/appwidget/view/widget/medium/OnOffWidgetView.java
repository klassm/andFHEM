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

import javax.inject.Inject;

import li.klass.fhem.R;
import li.klass.fhem.adapter.devices.hook.DeviceHookProvider;
import li.klass.fhem.adapter.devices.toggle.OnOffBehavior;
import li.klass.fhem.appwidget.WidgetConfiguration;
import li.klass.fhem.appwidget.view.widget.base.DeviceAppWidgetView;
import li.klass.fhem.constants.Actions;
import li.klass.fhem.constants.BundleExtraKeys;
import li.klass.fhem.dagger.ApplicationComponent;
import li.klass.fhem.domain.core.FhemDevice;
import li.klass.fhem.domain.core.ToggleableDevice;
import li.klass.fhem.service.intent.DeviceIntentService;

public class OnOffWidgetView extends DeviceAppWidgetView {
    @Inject
    OnOffBehavior onOffBehavior;

    @Inject
    DeviceHookProvider deviceHookProvider;

    @Override
    public int getWidgetName() {
        return R.string.widget_onOff;
    }

    @Override
    protected int getContentView() {
        return R.layout.appwidget_on_off;
    }

    @Override
    protected void fillWidgetView(Context context, RemoteViews view, FhemDevice<?> device, WidgetConfiguration widgetConfiguration) {
        boolean isOn = onOffBehavior.isOn(device);

        String onStateName = deviceHookProvider.getOnStateName(device);
        String offStateName = deviceHookProvider.getOffStateName(device);

        view.setTextViewText(R.id.widgetOnButton, device.getEventMapStateFor(onStateName));
        view.setTextViewText(R.id.widgetOffButton, device.getEventMapStateFor(offStateName));

        int backgroundColor = isOn ? R.color.android_green : android.R.color.white;
        view.setInt(R.id.widgetOnButton, "setBackgroundColor", context.getResources().getColor(backgroundColor));

        Intent onIntent = new Intent(Actions.DEVICE_SET_STATE)
                .setClass(context, DeviceIntentService.class)
                .putExtra(BundleExtraKeys.DEVICE_NAME, device.getName())
                .putExtra(BundleExtraKeys.DEVICE_TARGET_STATE, onStateName)
                .putExtra(BundleExtraKeys.CONNECTION_ID, widgetConfiguration.connectionId.orNull());
        PendingIntent onPendingIntent = PendingIntent.getService(context, widgetConfiguration.widgetId,
                onIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        view.setOnClickPendingIntent(R.id.widgetOnButton, onPendingIntent);

        Intent offIntent = new Intent(Actions.DEVICE_SET_STATE)
                .setClass(context, DeviceIntentService.class)
                .putExtra(BundleExtraKeys.DEVICE_NAME, device.getName())
                .putExtra(BundleExtraKeys.DEVICE_TARGET_STATE, offStateName)
                .putExtra(BundleExtraKeys.CONNECTION_ID, widgetConfiguration.connectionId.orNull());
        PendingIntent offPendingIntent = PendingIntent.getService(context, -1 * widgetConfiguration.widgetId,
                offIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        view.setOnClickPendingIntent(R.id.widgetOffButton, offPendingIntent);

        openDeviceDetailPageWhenClicking(R.id.deviceName, view, device, widgetConfiguration, context);
    }

    @Override
    public boolean supports(FhemDevice<?> device, Context context) {
        return device instanceof ToggleableDevice;
    }

    @Override
    protected void inject(ApplicationComponent applicationComponent) {
        applicationComponent.inject(this);
    }
}
