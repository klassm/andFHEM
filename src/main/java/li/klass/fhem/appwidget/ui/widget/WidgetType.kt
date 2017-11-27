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

package li.klass.fhem.appwidget.ui.widget

import android.content.Context
import li.klass.fhem.appwidget.ui.widget.base.AppWidgetView
import li.klass.fhem.appwidget.ui.widget.base.DeviceAppWidgetView
import li.klass.fhem.appwidget.ui.widget.base.OtherAppWidgetView
import li.klass.fhem.appwidget.ui.widget.base.RoomAppWidgetView
import li.klass.fhem.appwidget.ui.widget.big.BigWeatherForecastWidget
import li.klass.fhem.appwidget.ui.widget.medium.*
import li.klass.fhem.appwidget.ui.widget.small.*
import li.klass.fhem.domain.core.FhemDevice

enum class WidgetType(val widgetView: AppWidgetView, val widgetSize: WidgetSize) {

    TEMPERATURE(TemperatureWidgetView(), WidgetSize.MEDIUM),
    TOGGLE(ToggleWidgetView(), WidgetSize.MEDIUM),
    TOGGLE_SMALL(SmallToggleWidget(), WidgetSize.SMALL),
    STATUS(StatusWidgetView(), WidgetSize.MEDIUM),
    INFORMATION(MediumInformationWidgetView(), WidgetSize.MEDIUM),
    HEATING(HeatingWidgetView(), WidgetSize.MEDIUM),
    WEATHER_FORECAST(MediumWeatherForecastWidget(), WidgetSize.MEDIUM),
    WEATHER_FORECAST_BIG(BigWeatherForecastWidget(), WidgetSize.BIG),
    DIM(DimWidgetView(), WidgetSize.MEDIUM),
    TARGET_STATE(TargetStateWidgetView(), WidgetSize.MEDIUM),
    ON_OFF(OnOffWidgetView(), WidgetSize.MEDIUM),
    ROOM_DETAIL_LINK(RoomDetailLinkWidget(), WidgetSize.MEDIUM),
    FAVORITES_LINK(FavoritesLinkWidget(), WidgetSize.SMALL),
    ROOMS_LINK(RoomsLinkWidget(), WidgetSize.SMALL),
    ALL_DEVICES_LINK(AllDevicesLinkWidget(), WidgetSize.SMALL),
    CONVERSION_LINK(ConversionLinkWidget(), WidgetSize.SMALL),
    TIMERS_LINK(TimersLinkWidget(), WidgetSize.SMALL),
    SEND_COMMAND_LINK(SendCommandLinkWidget(), WidgetSize.SMALL),
    UPDATE_WIDGET(DeviceListUpdateWidget(), WidgetSize.SMALL);

    fun createWidgetConfiguration(context: Context, appWidgetId: Int,
                                  callback: WidgetConfigurationCreatedCallback,
                                  vararg payload: String) {
        widgetView.createWidgetConfiguration(context, this, appWidgetId, callback, *payload)
    }

    companion object {

        fun getSupportedDeviceWidgetsFor(size: WidgetSize, device: FhemDevice, context: Context): List<WidgetType> {
            return WidgetType.values()
                    .filter {
                        it.widgetSize == size &&
                                it.widgetView is DeviceAppWidgetView &&
                                it.widgetView.supports(device, context)
                    }
                    .toList()
        }

        fun getSupportedRoomWidgetsFor(size: WidgetSize): List<WidgetType> =
                WidgetType.values()
                        .filter { it.widgetSize == size && it.widgetView is RoomAppWidgetView }
                        .toList()

        fun getOtherWidgetsFor(size: WidgetSize): List<WidgetType> =
                WidgetType.values()
                        .filter { it.widgetSize == size && it.widgetView is OtherAppWidgetView }
                        .toList()
    }
}
