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

import li.klass.fhem.appwidget.ui.widget.base.AppWidgetView
import li.klass.fhem.appwidget.ui.widget.base.DeviceAppWidgetView
import li.klass.fhem.appwidget.ui.widget.base.OtherAppWidgetView
import li.klass.fhem.appwidget.ui.widget.base.RoomAppWidgetView
import li.klass.fhem.appwidget.ui.widget.big.BigWeatherForecastWidget
import li.klass.fhem.appwidget.ui.widget.medium.*
import li.klass.fhem.appwidget.ui.widget.small.*
import li.klass.fhem.domain.core.FhemDevice
import javax.inject.Inject

class WidgetTypeProvider @Inject constructor(
        temperatureWidgetView: TemperatureWidgetView,
        toggleWidgetView: ToggleWidgetView,
        smallToggleWidget: SmallToggleWidget,
        statusWidgetView: StatusWidgetView,
        mediumInformationWidgetView: MediumInformationWidgetView,
        heatingWidgetView: HeatingWidgetView,
        mediumWeatherForecastWidget: MediumWeatherForecastWidget,
        bigWeatherForecastWidget: BigWeatherForecastWidget,
        dimWidgetView: DimWidgetView,
        targetStateWidgetView: TargetStateWidgetView,
        onOffWidgetView: OnOffWidgetView,
        smallRoomDetailLinkWidget: SmallRoomDetailLinkWidget,
        mediumRoomDetailLinkWidget: MediumRoomDetailLinkWidget,
        favoritesLinkWidget: FavoritesLinkWidget,
        roomsLinkWidget: RoomsLinkWidget,
        allDevicesLinkWidget: AllDevicesLinkWidget,
        conversionLinkWidget: ConversionLinkWidget,
        timersLinkWidget: TimersLinkWidget,
        sendCommandLinkWidget: SendCommandLinkWidget,
        deviceListUpdateWidget: DeviceListUpdateWidget,
        smallPresenceWidget: SmallPresenceWidget
) {

    private val allWidgets = setOf(
            temperatureWidgetView,
            toggleWidgetView,
            smallToggleWidget,
            statusWidgetView,
            mediumInformationWidgetView,
            heatingWidgetView,
            mediumWeatherForecastWidget,
            bigWeatherForecastWidget,
            dimWidgetView,
            targetStateWidgetView,
            onOffWidgetView,
            smallRoomDetailLinkWidget,
            mediumRoomDetailLinkWidget,
            favoritesLinkWidget,
            roomsLinkWidget,
            allDevicesLinkWidget,
            conversionLinkWidget,
            timersLinkWidget,
            sendCommandLinkWidget,
            deviceListUpdateWidget,
            smallPresenceWidget
    )

    fun getSupportedDeviceWidgetsFor(size: WidgetSize, device: FhemDevice): List<AppWidgetView> =
            allWidgets
                    .filter {
                        it.widgetSize == size &&
                                it is DeviceAppWidgetView &&
                                it.supports(device)
                    }
                    .toList()

    fun getSupportedRoomWidgetsFor(size: WidgetSize): List<AppWidgetView> =
            allWidgets
                    .filter { it.widgetSize == size && it is RoomAppWidgetView }
                    .toList()

    fun getOtherWidgetsFor(size: WidgetSize): List<AppWidgetView> =
            allWidgets
                    .filter { it.widgetSize == size && it is OtherAppWidgetView }
                    .toList()

    fun widgetFor(widgetType: WidgetType): AppWidgetView =
            allWidgets.find { it.widgetType == widgetType }!!
}
