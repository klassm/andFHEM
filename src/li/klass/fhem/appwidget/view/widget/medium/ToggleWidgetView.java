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

package li.klass.fhem.appwidget.view.widget.medium;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.RemoteViews;
import li.klass.fhem.R;
import li.klass.fhem.appwidget.WidgetConfiguration;
import li.klass.fhem.appwidget.view.widget.AppWidgetView;
import li.klass.fhem.constants.Actions;
import li.klass.fhem.constants.BundleExtraKeys;
import li.klass.fhem.domain.Device;
import li.klass.fhem.domain.ToggleableDevice;

public class ToggleWidgetView extends AppWidgetView {
    @Override
    public int getWidgetName() {
        return R.string.widget_toggle;
    }

    @Override
    protected int getContentView() {
        return R.layout.appwidget_toggle;
    }

    @Override
    protected void fillWidgetView(Context context, RemoteViews view, Device<?> device, WidgetConfiguration widgetConfiguration) {
        ToggleableDevice toggleable = (ToggleableDevice) device;
        if (toggleable.isOn()) {
            view.setViewVisibility(R.id.toggleOff, View.GONE);
            view.setViewVisibility(R.id.toggleOn, View.VISIBLE);
        } else {
            view.setViewVisibility(R.id.toggleOff, View.VISIBLE);
            view.setViewVisibility(R.id.toggleOn, View.GONE);
        }

        Intent intent = new Intent(Actions.DEVICE_WIDGET_TOGGLE);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(BundleExtraKeys.APP_WIDGET_ID, widgetConfiguration.widgetId);
        intent.putExtra(BundleExtraKeys.DEVICE_NAME, device.getName());

        PendingIntent pendingIntent = PendingIntent.getService(context, widgetConfiguration.widgetId, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        view.setOnClickPendingIntent(R.id.toggleOff, pendingIntent);
        view.setOnClickPendingIntent(R.id.toggleOn, pendingIntent);

        openDeviceDetailPageWhenClicking(R.id.main, view, device, widgetConfiguration);
    }

    @Override
    public boolean supports(Device<?> device) {
        return device instanceof ToggleableDevice;
    }

    @Override
    public long updateInterval() {
        return 3600000; // every hour
    }
}
