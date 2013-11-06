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

package li.klass.fhem.appwidget.view;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import li.klass.fhem.appwidget.WidgetConfigurationCreatedCallback;
import li.klass.fhem.appwidget.view.widget.base.AppWidgetView;
import li.klass.fhem.appwidget.view.widget.big.BigWeatherForecastWidget;
import li.klass.fhem.appwidget.view.widget.medium.DimWidgetView;
import li.klass.fhem.appwidget.view.widget.medium.HeatingWidgetView;
import li.klass.fhem.appwidget.view.widget.medium.MediumInformationWidgetView;
import li.klass.fhem.appwidget.view.widget.medium.MediumWeatherForecastWidget;
import li.klass.fhem.appwidget.view.widget.medium.StatusWidgetView;
import li.klass.fhem.appwidget.view.widget.medium.TargetStateWidgetView;
import li.klass.fhem.appwidget.view.widget.medium.TemperatureWidgetView;
import li.klass.fhem.appwidget.view.widget.medium.ToggleWidgetView;
import li.klass.fhem.appwidget.view.widget.small.SmallToggleWidget;
import li.klass.fhem.domain.core.Device;

public enum WidgetType {
    TEMPERATURE(new TemperatureWidgetView(), WidgetSize.MEDIUM),
    TOGGLE(new ToggleWidgetView(), WidgetSize.MEDIUM),
    TOGGLE_SMALL(new SmallToggleWidget(), WidgetSize.SMALL),
    STATUS(new StatusWidgetView(), WidgetSize.MEDIUM),
    INFORMATION(new MediumInformationWidgetView(), WidgetSize.MEDIUM),
    HEATING(new HeatingWidgetView(), WidgetSize.MEDIUM),
    WEATHER_FORECAST(new MediumWeatherForecastWidget(), WidgetSize.MEDIUM),
    WEATHER_FORECAST_BIG(new BigWeatherForecastWidget(), WidgetSize.BIG),
    DIM(new DimWidgetView(), WidgetSize.MEDIUM),
    TARGET_STATE(new TargetStateWidgetView(), WidgetSize.MEDIUM);

    public final AppWidgetView widgetView;
    public final WidgetSize widgetSize;

    WidgetType(AppWidgetView widgetView, WidgetSize widgetSize) {
        this.widgetView = widgetView;
        this.widgetSize = widgetSize;
    }

    public static List<WidgetType> getSupportedWidgetTypesFor(WidgetSize size, Device<?> device) {
        List<WidgetType> widgetTypes = new ArrayList<WidgetType>();
        for (WidgetType widgetType : WidgetType.values()) {
            if (widgetType.widgetSize == size && widgetType.widgetView.supports(device)) {
                widgetTypes.add(widgetType);
            }
        }
        return widgetTypes;
    }

    public void createWidgetConfiguration(Context context, int appWidgetId, Device device,
                                          WidgetConfigurationCreatedCallback callback) {
        widgetView.createWidgetConfiguration(context, this, appWidgetId, device, callback);
    }
}
