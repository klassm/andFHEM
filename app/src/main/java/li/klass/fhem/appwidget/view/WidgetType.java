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

package li.klass.fhem.appwidget.view;

import android.app.Application;
import android.content.Context;

import com.google.common.base.Predicate;

import java.util.List;

import li.klass.fhem.appwidget.WidgetConfigurationCreatedCallback;
import li.klass.fhem.appwidget.view.widget.base.AppWidgetView;
import li.klass.fhem.appwidget.view.widget.base.DeviceAppWidgetView;
import li.klass.fhem.appwidget.view.widget.base.OtherAppWidgetView;
import li.klass.fhem.appwidget.view.widget.base.RoomAppWidgetView;
import li.klass.fhem.appwidget.view.widget.big.BigWeatherForecastWidget;
import li.klass.fhem.appwidget.view.widget.medium.DimWidgetView;
import li.klass.fhem.appwidget.view.widget.medium.HeatingWidgetView;
import li.klass.fhem.appwidget.view.widget.medium.MediumInformationWidgetView;
import li.klass.fhem.appwidget.view.widget.medium.MediumWeatherForecastWidget;
import li.klass.fhem.appwidget.view.widget.medium.OnOffWidgetView;
import li.klass.fhem.appwidget.view.widget.medium.RoomDetailLinkWidget;
import li.klass.fhem.appwidget.view.widget.medium.StatusWidgetView;
import li.klass.fhem.appwidget.view.widget.medium.TargetStateWidgetView;
import li.klass.fhem.appwidget.view.widget.medium.TemperatureWidgetView;
import li.klass.fhem.appwidget.view.widget.medium.ToggleWidgetView;
import li.klass.fhem.appwidget.view.widget.small.AllDevicesLinkWidget;
import li.klass.fhem.appwidget.view.widget.small.ConversionLinkWidget;
import li.klass.fhem.appwidget.view.widget.small.DeviceListUpdateWidget;
import li.klass.fhem.appwidget.view.widget.small.FavoritesLinkWidget;
import li.klass.fhem.appwidget.view.widget.small.RoomsLinkWidget;
import li.klass.fhem.appwidget.view.widget.small.SendCommandLinkWidget;
import li.klass.fhem.appwidget.view.widget.small.SmallToggleWidget;
import li.klass.fhem.appwidget.view.widget.small.TimersLinkWidget;
import li.klass.fhem.domain.core.Device;

import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Lists.newArrayList;

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
    TARGET_STATE(new TargetStateWidgetView(), WidgetSize.MEDIUM),
    ON_OFF(new OnOffWidgetView(), WidgetSize.MEDIUM),
    ROOM_DETAIL_LINK(new RoomDetailLinkWidget(), WidgetSize.MEDIUM),
    FAVORITES_LINK(new FavoritesLinkWidget(), WidgetSize.SMALL),
    ROOMS_LINK(new RoomsLinkWidget(), WidgetSize.SMALL),
    ALL_DEVICES_LINK(new AllDevicesLinkWidget(), WidgetSize.SMALL),
    CONVERSION_LINK(new ConversionLinkWidget(), WidgetSize.SMALL),
    TIMERS_LINK(new TimersLinkWidget(), WidgetSize.SMALL),
    SEND_COMMAND_LINK(new SendCommandLinkWidget(), WidgetSize.SMALL),
    UPDATE_WIDGET(new DeviceListUpdateWidget(), WidgetSize.SMALL),
    ;

    public final AppWidgetView widgetView;
    public final WidgetSize widgetSize;

    WidgetType(AppWidgetView widgetView, WidgetSize widgetSize) {
        this.widgetView = widgetView;
        this.widgetSize = widgetSize;
    }

    public static List<WidgetType> getSupportedDeviceWidgetsFor(final WidgetSize size, final Device<?> device) {
        return newArrayList(filter(newArrayList(WidgetType.values()), new Predicate<WidgetType>() {
            @Override
            public boolean apply(WidgetType widgetType) {
                return widgetType.widgetSize == size &&
                        widgetType.widgetView instanceof DeviceAppWidgetView &&
                        ((DeviceAppWidgetView) widgetType.widgetView).supports(device);
            }
        }));
    }

    public static List<WidgetType> getSupportedRoomWidgetsFor(final WidgetSize size) {
        return newArrayList(filter(newArrayList(WidgetType.values()), new Predicate<WidgetType>() {
            @Override
            public boolean apply(WidgetType widgetType) {
                return widgetType.widgetSize == size &&
                        widgetType.widgetView instanceof RoomAppWidgetView;
            }
        }));
    }

    public static List<WidgetType> getOtherWidgetsFor(final WidgetSize size) {
        return newArrayList(filter(newArrayList(WidgetType.values()), new Predicate<WidgetType>() {
            @Override
            public boolean apply(WidgetType widgetType) {
                return widgetType.widgetSize == size &&
                        widgetType.widgetView instanceof OtherAppWidgetView;
            }
        }));
    }

    public void createWidgetConfiguration(Application application, Context context, int appWidgetId,
                                          WidgetConfigurationCreatedCallback callback,
                                          String... payload) {
        widgetView.attach(application);
        widgetView.createWidgetConfiguration(context, this, appWidgetId, callback, payload);
    }
}
