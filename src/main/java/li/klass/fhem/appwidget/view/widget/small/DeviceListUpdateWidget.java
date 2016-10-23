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

package li.klass.fhem.appwidget.view.widget.small;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.widget.RemoteViews;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import li.klass.fhem.R;
import li.klass.fhem.appwidget.WidgetConfiguration;
import li.klass.fhem.appwidget.WidgetConfigurationCreatedCallback;
import li.klass.fhem.appwidget.view.WidgetType;
import li.klass.fhem.appwidget.view.widget.base.OtherAppWidgetView;
import li.klass.fhem.dagger.ApplicationComponent;

import static li.klass.fhem.constants.Actions.WIDGET_REQUEST_UPDATE;

public class DeviceListUpdateWidget extends OtherAppWidgetView {
    @Override
    public void createWidgetConfiguration(Context context, WidgetType widgetType, int appWidgetId, WidgetConfigurationCreatedCallback callback, String... payload) {
        callback.widgetConfigurationCreated(new WidgetConfiguration(appWidgetId, widgetType, Optional.<String>absent(), ImmutableList.copyOf(payload)));
    }

    @Override
    public int getWidgetName() {
        return R.string.widget_device_list_update;
    }

    @Override
    protected int getContentView() {
        return R.layout.appwidget_icon_small;
    }

    @Override
    protected void fillWidgetView(Context context, RemoteViews view, WidgetConfiguration widgetConfiguration) {
        view.setImageViewResource(R.id.icon, R.drawable.launcher_refresh);

        Intent updateIntent = new Intent(WIDGET_REQUEST_UPDATE);
        updateIntent.putExtra("unique", "foobar://" + SystemClock.elapsedRealtime());

        view.setOnClickPendingIntent(R.id.layout, PendingIntent.getService(context,
                widgetConfiguration.widgetId, updateIntent,
                PendingIntent.FLAG_UPDATE_CURRENT));
    }

    @Override
    protected void inject(ApplicationComponent applicationComponent) {
        applicationComponent.inject(this);
    }
}