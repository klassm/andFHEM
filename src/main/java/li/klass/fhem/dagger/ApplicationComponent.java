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

import org.jetbrains.annotations.NotNull;

import javax.inject.Singleton;

import dagger.Component;
import li.klass.fhem.AndFHEMApplication;
import li.klass.fhem.activities.AndFHEMMainActivity;
import li.klass.fhem.activities.PreferencesActivity;
import li.klass.fhem.activities.PremiumActivity;
import li.klass.fhem.activities.StartupActivity;
import li.klass.fhem.activities.graph.ChartingActivity;
import li.klass.fhem.activities.locale.ConditionQueryLocaleReceiver;
import li.klass.fhem.activities.locale.ConnectionChangeLocaleSettingActivity;
import li.klass.fhem.activities.locale.FireSettingLocaleReceiver;
import li.klass.fhem.activities.locale.SendCommandLocaleSettingActivity;
import li.klass.fhem.adapter.devices.DmxAdapter;
import li.klass.fhem.adapter.devices.EnOceanAdapter;
import li.klass.fhem.adapter.devices.EnigmaDeviceAdapter;
import li.klass.fhem.adapter.devices.FS20ZDRDeviceAdapter;
import li.klass.fhem.adapter.devices.FloorplanAdapter;
import li.klass.fhem.adapter.devices.GCMSendDeviceAdapter;
import li.klass.fhem.adapter.devices.HarmonyDeviceAdapter;
import li.klass.fhem.adapter.devices.LightSceneAdapter;
import li.klass.fhem.adapter.devices.MiLightDeviceAdapter;
import li.klass.fhem.adapter.devices.PIDDeviceAdapter;
import li.klass.fhem.adapter.devices.PioneerAvrDeviceAdapter;
import li.klass.fhem.adapter.devices.PioneerAvrZoneDeviceAdapter;
import li.klass.fhem.adapter.devices.ReadingsProxyDeviceAdapter;
import li.klass.fhem.adapter.devices.RemoteControlAdapter;
import li.klass.fhem.adapter.devices.SBPlayerDeviceAdapter;
import li.klass.fhem.adapter.devices.STVDeviceAdapter;
import li.klass.fhem.adapter.devices.SonosPlayerAdapter;
import li.klass.fhem.adapter.devices.SwapDeviceAdapter;
import li.klass.fhem.adapter.devices.ThresholdAdapter;
import li.klass.fhem.adapter.devices.WeatherAdapter;
import li.klass.fhem.adapter.devices.WebLinkAdapter;
import li.klass.fhem.adapter.devices.WifiLightDeviceAdapter;
import li.klass.fhem.adapter.devices.core.DimmableAdapter;
import li.klass.fhem.adapter.devices.core.ExplicitOverviewDetailDeviceAdapterWithSwitchActionRow;
import li.klass.fhem.adapter.devices.core.GenericOverviewDetailDeviceAdapter;
import li.klass.fhem.adapter.devices.core.ToggleableAdapter;
import li.klass.fhem.adapter.devices.strategy.ToggleableStrategy;
import li.klass.fhem.adapter.devices.strategy.WebcmdStrategy;
import li.klass.fhem.adapter.devices.toggle.OnOffBehavior;
import li.klass.fhem.appindex.AppIndexIntentService;
import li.klass.fhem.appwidget.AppWidgetDataHolder;
import li.klass.fhem.appwidget.service.AppWidgetListViewUpdateRemoteViewsService;
import li.klass.fhem.appwidget.service.AppWidgetUpdateService;
import li.klass.fhem.appwidget.type.big.BigAppWidgetProvider;
import li.klass.fhem.appwidget.type.big.BigWidgetSelectionActivity;
import li.klass.fhem.appwidget.type.medium.MediumAppWidgetProvider;
import li.klass.fhem.appwidget.type.medium.MediumWidgetSelectionActivity;
import li.klass.fhem.appwidget.type.small.SmallAppWidgetProvider;
import li.klass.fhem.appwidget.type.small.SmallWidgetSelectionActivity;
import li.klass.fhem.appwidget.view.widget.base.otherWidgets.OtherWidgetsFragment;
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
import li.klass.fhem.fcm.FcmIntentService;
import li.klass.fhem.fragments.AllDevicesFragment;
import li.klass.fhem.fragments.ConversionFragment;
import li.klass.fhem.fragments.FavoritesFragment;
import li.klass.fhem.fragments.FloorplanFragment;
import li.klass.fhem.fragments.RoomDetailFragment;
import li.klass.fhem.fragments.RoomListFragment;
import li.klass.fhem.fragments.SearchResultsFragment;
import li.klass.fhem.fragments.SendCommandFragment;
import li.klass.fhem.fragments.TimerDetailFragment;
import li.klass.fhem.fragments.TimerListFragment;
import li.klass.fhem.fragments.WebViewFragment;
import li.klass.fhem.fragments.connection.ConnectionDetailFragment;
import li.klass.fhem.fragments.connection.ConnectionListFragment;
import li.klass.fhem.fragments.core.DeviceDetailFragment;
import li.klass.fhem.fragments.device.DeviceNameListFragment;
import li.klass.fhem.fragments.device.DeviceNameListNavigationFragment;
import li.klass.fhem.fragments.device.DeviceNameSelectionFragment;
import li.klass.fhem.fragments.device.DeviceNameSelectionNavigationFragment;
import li.klass.fhem.fragments.weekprofile.FromToWeekProfileFragment;
import li.klass.fhem.fragments.weekprofile.IntervalWeekProfileFragment;
import li.klass.fhem.service.deviceConfiguration.DeviceConfigurationProvider;
import li.klass.fhem.service.graph.gplot.GPlotHolder;
import li.klass.fhem.service.importexport.ImportExportService;
import li.klass.fhem.service.intent.AppActionsIntentService;
import li.klass.fhem.service.intent.DeviceIntentService;
import li.klass.fhem.service.intent.ExternalApiService;
import li.klass.fhem.service.intent.ImageIntentService;
import li.klass.fhem.service.intent.LicenseIntentService;
import li.klass.fhem.service.intent.NotificationIntentService;
import li.klass.fhem.service.intent.RoomListUpdateIntentService;
import li.klass.fhem.service.intent.SendCommandService;
import li.klass.fhem.service.intent.VoiceCommandIntentService;
import li.klass.fhem.service.room.RoomListUpdateService;
import li.klass.fhem.service.room.group.GroupProvider;
import li.klass.fhem.service.room.xmllist.XmlListParser;
import li.klass.fhem.widget.deviceFunctionality.DeviceFunctionalityOrderPreference;

@Singleton
@Component(modules = {ApplicationModule.class, DetailActionsModule.class, DeviceGroupProviderModule.class})
public interface ApplicationComponent {

    void inject(DeviceFunctionalityOrderPreference object);


    void inject(AndFHEMMainActivity object);

    void inject(ChartingActivity object);

    void inject(StartupActivity object);

    void inject(PreferencesActivity object);

    void inject(SmallWidgetSelectionActivity object);

    void inject(MediumWidgetSelectionActivity object);

    void inject(BigWidgetSelectionActivity object);

    void inject(PremiumActivity object);


    void inject(DmxAdapter object);

    void inject(EnOceanAdapter object);

    void inject(FloorplanAdapter object);

    void inject(FS20ZDRDeviceAdapter object);

    void inject(GCMSendDeviceAdapter object);

    void inject(LightSceneAdapter object);

    void inject(PIDDeviceAdapter object);

    void inject(ReadingsProxyDeviceAdapter object);

    void inject(RemoteControlAdapter object);

    void inject(SonosPlayerAdapter object);

    void inject(SwapDeviceAdapter object);

    void inject(ThresholdAdapter object);

    void inject(ToggleableAdapter object);

    void inject(WeatherAdapter object);

    void inject(WebLinkAdapter object);

    void inject(WifiLightDeviceAdapter object);

    void inject(EnigmaDeviceAdapter object);

    void inject(PioneerAvrDeviceAdapter object);

    void inject(MiLightDeviceAdapter object);

    void inject(STVDeviceAdapter object);

    void inject(PioneerAvrZoneDeviceAdapter object);

    void inject(SBPlayerDeviceAdapter object);

    void inject(HarmonyDeviceAdapter object);

    void inject(GenericOverviewDetailDeviceAdapter object);

    void inject(DimmableAdapter object);

    void inject(ExplicitOverviewDetailDeviceAdapterWithSwitchActionRow object);


    void inject(TemperatureWidgetView object);

    void inject(ToggleWidgetView object);

    void inject(SmallToggleWidget object);

    void inject(StatusWidgetView object);

    void inject(MediumInformationWidgetView object);

    void inject(HeatingWidgetView object);

    void inject(MediumWeatherForecastWidget object);

    void inject(BigWeatherForecastWidget object);

    void inject(DimWidgetView object);

    void inject(TargetStateWidgetView object);

    void inject(RoomDetailLinkWidget object);

    void inject(FavoritesLinkWidget object);

    void inject(RoomsLinkWidget object);

    void inject(AllDevicesLinkWidget object);

    void inject(ConversionLinkWidget object);

    void inject(TimersLinkWidget object);

    void inject(SendCommandLinkWidget object);

    void inject(DeviceListUpdateWidget object);

    void inject(OnOffWidgetView object);

    void inject(AppWidgetListViewUpdateRemoteViewsService object);

    void inject(AppWidgetDataHolder object);

    void inject(SmallAppWidgetProvider object);

    void inject(MediumAppWidgetProvider object);

    void inject(BigAppWidgetProvider object);


    void inject(FavoritesFragment object);

    void inject(RoomListFragment object);

    void inject(AllDevicesFragment object);

    void inject(ConversionFragment object);

    void inject(DeviceDetailFragment object);

    void inject(FromToWeekProfileFragment object);

    void inject(IntervalWeekProfileFragment object);

    void inject(FloorplanFragment object);

    void inject(RoomDetailFragment object);

    void inject(SendCommandFragment object);

    void inject(DeviceNameListFragment object);

    void inject(DeviceNameSelectionFragment object);

    void inject(DeviceNameListNavigationFragment object);

    void inject(TimerListFragment object);

    void inject(TimerDetailFragment object);

    void inject(ConnectionListFragment object);

    void inject(ConnectionDetailFragment object);

    void inject(WebViewFragment object);

    void inject(OtherWidgetsFragment object);

    void inject(DeviceNameSelectionNavigationFragment object);

    void inject(RoomListUpdateService object);


    void inject(AppIndexIntentService object);

    void inject(FcmIntentService object);

    void inject(SendCommandService object);

    void inject(RoomListUpdateIntentService object);

    void inject(NotificationIntentService object);

    void inject(ImageIntentService object);

    void inject(ExternalApiService object);

    void inject(DeviceIntentService object);

    void inject(AppWidgetUpdateService object);

    void inject(LicenseIntentService object);

    void inject(AppActionsIntentService object);

    void inject(VoiceCommandIntentService object);

    void inject(ImportExportService object);

    void inject(AndFHEMApplication object);

    void inject(ConditionQueryLocaleReceiver object);


    void inject(OnOffBehavior object);

    void inject(ToggleableStrategy object);

    void inject(WebcmdStrategy object);


    XmlListParser getXmlListParser();

    GPlotHolder getGPlotHolder();

    DeviceConfigurationProvider getDeviceConfigurationProvider();

    GroupProvider getGroupProvider();

    void inject(@NotNull ConnectionChangeLocaleSettingActivity connectionChangeLocaleSettingActivity);

    void inject(@NotNull SendCommandLocaleSettingActivity sendCommandLocaleSettingActivity);

    void inject(@NotNull SearchResultsFragment searchResultsFragment);

    void inject(@NotNull FireSettingLocaleReceiver fireSettingLocaleReceiver);
}
