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

package li.klass.fhem.dagger;

import dagger.Module;
import li.klass.fhem.appwidget.AppWidgetDataHolder;
import li.klass.fhem.appwidget.service.AppWidgetListViewUpdateRemoteViewsService;
import li.klass.fhem.appwidget.type.big.BigAppWidgetProvider;
import li.klass.fhem.appwidget.type.medium.MediumAppWidgetProvider;
import li.klass.fhem.appwidget.type.small.SmallAppWidgetProvider;
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

@Module(complete = false,
        injects = {
                TemperatureWidgetView.class,
                ToggleWidgetView.class,
                SmallToggleWidget.class,
                StatusWidgetView.class,
                MediumInformationWidgetView.class,
                HeatingWidgetView.class,
                MediumWeatherForecastWidget.class,
                BigWeatherForecastWidget.class,
                DimWidgetView.class,
                TargetStateWidgetView.class,
                RoomDetailLinkWidget.class,
                FavoritesLinkWidget.class,
                RoomsLinkWidget.class,
                AllDevicesLinkWidget.class,
                ConversionLinkWidget.class,
                TimersLinkWidget.class,
                SendCommandLinkWidget.class,
                SendCommandLinkWidget.class,
                DeviceListUpdateWidget.class,
                OnOffWidgetView.class,

                AppWidgetListViewUpdateRemoteViewsService.class,
                AppWidgetDataHolder.class,
                SmallAppWidgetProvider.class,
                MediumAppWidgetProvider.class,
                BigAppWidgetProvider.class
        })
public class AppWidgetModule {
}
