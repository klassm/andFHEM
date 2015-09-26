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

import android.content.Context;
import android.view.View;
import android.widget.RemoteViews;

import java.util.Locale;

import li.klass.fhem.R;
import li.klass.fhem.appwidget.WidgetConfiguration;
import li.klass.fhem.appwidget.view.widget.base.DeviceAppWidgetView;
import li.klass.fhem.dagger.ApplicationComponent;
import li.klass.fhem.domain.core.FhemDevice;
import li.klass.fhem.service.room.xmllist.XmlListDevice;

public class HeatingWidgetView extends DeviceAppWidgetView {
    @Override
    public int getWidgetName() {
        return R.string.widget_heating;
    }

    @Override
    protected int getContentView() {
        return R.layout.appwidget_heating;
    }

    @Override
    protected void fillWidgetView(Context context, RemoteViews view, FhemDevice<?> device, WidgetConfiguration widgetConfiguration) {
        XmlListDevice xmlListDevice = device.getXmlListDevice();

        String warnings = xmlListDevice.getState("warnings").get();
        String temperature = xmlListDevice.getState("temperature").get();
        String desiredTemp = xmlListDevice.getState("desired-temp").get();

        if (warnings != null && warnings.toLowerCase(Locale.getDefault()).contains("open")) {
            view.setViewVisibility(R.id.windowOpen, View.VISIBLE);
        } else {
            view.setViewVisibility(R.id.windowOpen, View.GONE);
        }

        String target = context.getString(R.string.target);
        setTextViewOrHide(view, R.id.temperature, temperature);

        if (temperature != null && desiredTemp != null) {
            String text = target + ": " + desiredTemp;
            setTextViewOrHide(view, R.id.additional, text);
        } else {
            view.setViewVisibility(R.id.additional, View.GONE);
        }

        openDeviceDetailPageWhenClicking(R.id.main, view, device, widgetConfiguration, context);
    }

    @Override
    public boolean supports(FhemDevice<?> device) {
        XmlListDevice xmlListDevice = device.getXmlListDevice();
        return xmlListDevice.containsState("warnings")
                && xmlListDevice.containsState("temperature")
                && xmlListDevice.containsState("desired-temp");
    }

    @Override
    protected void inject(ApplicationComponent applicationComponent) {
        applicationComponent.inject(this);
    }
}
