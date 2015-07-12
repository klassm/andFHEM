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
import android.widget.RemoteViews;

import li.klass.fhem.R;
import li.klass.fhem.appwidget.WidgetConfiguration;
import li.klass.fhem.appwidget.annotation.WidgetTemperatureAdditionalField;
import li.klass.fhem.appwidget.annotation.WidgetTemperatureField;
import li.klass.fhem.appwidget.view.widget.base.DeviceAppWidgetView;
import li.klass.fhem.dagger.ApplicationComponent;
import li.klass.fhem.domain.core.FhemDevice;

public class TemperatureWidgetView extends DeviceAppWidgetView {
    @Override
    public int getWidgetName() {
        return R.string.widget_temperature;
    }

    @Override
    public int getContentView() {
        return R.layout.appwidget_temperature;
    }

    @Override
    public void fillWidgetView(Context context, RemoteViews view, FhemDevice<?> device, WidgetConfiguration widgetConfiguration) {
        if (device != null) {
            String temperature = valueForAnnotation(device, WidgetTemperatureField.class);
            String additionalFieldValue = valueForAnnotation(device, WidgetTemperatureAdditionalField.class);
            setTextViewOrHide(view, R.id.additional, additionalFieldValue);

            view.setTextViewText(R.id.temperature, temperature);

            openDeviceDetailPageWhenClicking(R.id.main, view, device, widgetConfiguration, context);
        }
    }

    @Override
    protected void inject(ApplicationComponent applicationComponent) {
        applicationComponent.inject(this);
    }
}
