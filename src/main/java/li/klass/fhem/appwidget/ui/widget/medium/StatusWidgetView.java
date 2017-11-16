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

package li.klass.fhem.appwidget.ui.widget.medium;

import android.content.Context;
import android.widget.RemoteViews;

import li.klass.fhem.R;
import li.klass.fhem.appwidget.ui.widget.base.DeviceAppWidgetView;
import li.klass.fhem.appwidget.update.WidgetConfiguration;
import li.klass.fhem.dagger.ApplicationComponent;
import li.klass.fhem.domain.core.FhemDevice;

public class StatusWidgetView extends DeviceAppWidgetView {
    @Override
    public int getWidgetName() {
        return R.string.widget_status;
    }

    @Override
    protected int getContentView() {
        return R.layout.appwidget_state;
    }

    @Override
    protected void fillWidgetView(Context context, RemoteViews view, FhemDevice device, WidgetConfiguration widgetConfiguration) {
        setTextViewOrHide(view, R.id.status, device.getState());
        openDeviceDetailPageWhenClicking(R.id.main, view, device, widgetConfiguration, context);
    }

    @Override
    public boolean supports(FhemDevice device, Context context) {
        return true;
    }

    @Override
    protected void inject(ApplicationComponent applicationComponent) {
        applicationComponent.inject(this);
    }
}
