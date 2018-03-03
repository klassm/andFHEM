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

import android.app.Application;

import org.jetbrains.annotations.NotNull;

import javax.inject.Singleton;

import dagger.Component;
import li.klass.fhem.AndFHEMApplication;
import li.klass.fhem.activities.AndFHEMMainActivity;
import li.klass.fhem.activities.PremiumActivity;
import li.klass.fhem.activities.StartupActivity;
import li.klass.fhem.activities.locale.ConditionQueryLocaleReceiver;
import li.klass.fhem.activities.locale.ConnectionChangeLocaleSettingActivity;
import li.klass.fhem.activities.locale.FireSettingLocaleReceiver;
import li.klass.fhem.activities.locale.SendCommandLocaleSettingActivity;
import li.klass.fhem.adapter.devices.core.GenericOverviewDetailDeviceAdapter;
import li.klass.fhem.adapter.devices.strategy.ToggleableStrategy;
import li.klass.fhem.adapter.devices.toggle.OnOffBehavior;
import li.klass.fhem.alarm.clock.update.AlarmClockIntentService;
import li.klass.fhem.appindex.AppIndexIntentService;
import li.klass.fhem.appwidget.action.AppWidgetBroadcastReceiver;
import li.klass.fhem.appwidget.provider.BigAppWidgetProvider;
import li.klass.fhem.appwidget.provider.MediumAppWidgetProvider;
import li.klass.fhem.appwidget.provider.SmallAppWidgetProvider;
import li.klass.fhem.appwidget.ui.selection.BigWidgetSelectionActivity;
import li.klass.fhem.appwidget.ui.selection.MediumWidgetSelectionActivity;
import li.klass.fhem.appwidget.ui.selection.SmallWidgetSelectionActivity;
import li.klass.fhem.appwidget.ui.widget.activity.TargetStateAdditionalInformationActivity;
import li.klass.fhem.appwidget.ui.widget.base.otherWidgets.OtherWidgetsFragment;
import li.klass.fhem.appwidget.ui.widget.big.BigWeatherForecastWidget;
import li.klass.fhem.appwidget.ui.widget.medium.DimWidgetView;
import li.klass.fhem.appwidget.ui.widget.medium.HeatingWidgetView;
import li.klass.fhem.appwidget.ui.widget.medium.MediumInformationWidgetView;
import li.klass.fhem.appwidget.ui.widget.medium.MediumWeatherForecastWidget;
import li.klass.fhem.appwidget.ui.widget.medium.OnOffWidgetView;
import li.klass.fhem.appwidget.ui.widget.medium.RoomDetailLinkWidget;
import li.klass.fhem.appwidget.ui.widget.medium.StatusWidgetView;
import li.klass.fhem.appwidget.ui.widget.medium.TargetStateWidgetView;
import li.klass.fhem.appwidget.ui.widget.medium.TemperatureWidgetView;
import li.klass.fhem.appwidget.ui.widget.medium.ToggleWidgetView;
import li.klass.fhem.appwidget.ui.widget.small.AllDevicesLinkWidget;
import li.klass.fhem.appwidget.ui.widget.small.ConversionLinkWidget;
import li.klass.fhem.appwidget.ui.widget.small.DeviceListUpdateWidget;
import li.klass.fhem.appwidget.ui.widget.small.FavoritesLinkWidget;
import li.klass.fhem.appwidget.ui.widget.small.RoomsLinkWidget;
import li.klass.fhem.appwidget.ui.widget.small.SendCommandLinkWidget;
import li.klass.fhem.appwidget.ui.widget.small.SmallPresenceWidget;
import li.klass.fhem.appwidget.ui.widget.small.SmallToggleWidget;
import li.klass.fhem.appwidget.ui.widget.small.TimersLinkWidget;
import li.klass.fhem.appwidget.update.AppWidgetListViewUpdateRemoteViewsService;
import li.klass.fhem.appwidget.update.AppWidgetUpdateIntentService;
import li.klass.fhem.backup.ImportExportService;
import li.klass.fhem.connection.ui.ConnectionDetailFragment;
import li.klass.fhem.connection.ui.ConnectionListFragment;
import li.klass.fhem.conversion.ui.ConversionFragment;
import li.klass.fhem.devices.detail.ui.DeviceDetailFragment;
import li.klass.fhem.devices.list.all.ui.AllDevicesFragment;
import li.klass.fhem.devices.list.favorites.ui.FavoritesFragment;
import li.klass.fhem.fcm.history.view.FcmHistoryMessagesFragment;
import li.klass.fhem.fcm.history.view.FcmHistoryUpdatesFragment;
import li.klass.fhem.fcm.receiver.FcmIntentService;
import li.klass.fhem.floorplan.ui.FloorplanFragment;
import li.klass.fhem.fragments.device.DeviceNameListFragment;
import li.klass.fhem.fragments.device.DeviceNameListNavigationFragment;
import li.klass.fhem.fragments.device.DeviceNameSelectionFragment;
import li.klass.fhem.fragments.device.DeviceNameSelectionNavigationFragment;
import li.klass.fhem.fragments.weekprofile.FromToWeekProfileFragment;
import li.klass.fhem.fragments.weekprofile.IntervalWeekProfileFragment;
import li.klass.fhem.graph.backend.gplot.GPlotHolder;
import li.klass.fhem.graph.ui.GraphActivity;
import li.klass.fhem.room.detail.ui.RoomDetailFragment;
import li.klass.fhem.room.list.ui.RoomListFragment;
import li.klass.fhem.search.MySearchSuggestionsProvider;
import li.klass.fhem.search.SearchResultsFragment;
import li.klass.fhem.sendCommand.ui.SendCommandFragment;
import li.klass.fhem.service.intent.DeviceIntentService;
import li.klass.fhem.service.intent.ExternalApiService;
import li.klass.fhem.service.intent.LicenseIntentService;
import li.klass.fhem.service.intent.NotificationIntentService;
import li.klass.fhem.service.intent.RoomListUpdateIntentService;
import li.klass.fhem.service.intent.SendCommandService;
import li.klass.fhem.settings.SettingsActivity;
import li.klass.fhem.settings.SettingsFragment;
import li.klass.fhem.timer.ui.TimerDetailFragment;
import li.klass.fhem.timer.ui.TimerListFragment;
import li.klass.fhem.ui.WebViewFragment;
import li.klass.fhem.update.backend.DeviceListUpdateService;
import li.klass.fhem.update.backend.device.configuration.DeviceConfigurationProvider;
import li.klass.fhem.update.backend.device.configuration.Sanitiser;
import li.klass.fhem.update.backend.group.GroupProvider;
import li.klass.fhem.update.backend.xmllist.XmlListParser;
import li.klass.fhem.widget.deviceFunctionality.DeviceFunctionalityOrderPreference;

@Singleton
@Component(modules = {ApplicationModule.class, DatabaseModule.class})
public interface ApplicationComponent {

    DeviceConfigurationProvider getDeviceConfigurationProvider();

    XmlListParser getXmllistParser();

    GPlotHolder getGPlotHolder();

    GroupProvider getGroupProvider();

    Sanitiser getSanitiser();

    Application getApplication();

    void inject(DeviceFunctionalityOrderPreference object);

    void inject(AndFHEMMainActivity object);

    void inject(GraphActivity object);

    void inject(StartupActivity object);

    void inject(SmallWidgetSelectionActivity object);

    void inject(MediumWidgetSelectionActivity object);

    void inject(BigWidgetSelectionActivity object);

    void inject(PremiumActivity object);


    void inject(GenericOverviewDetailDeviceAdapter object);


    void inject(TemperatureWidgetView object);

    void inject(ToggleWidgetView object);

    void inject(SmallPresenceWidget object);

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

    void inject(DeviceListUpdateService object);


    void inject(AppIndexIntentService object);

    void inject(FcmIntentService object);

    void inject(SendCommandService object);

    void inject(RoomListUpdateIntentService object);

    void inject(NotificationIntentService object);

    void inject(ExternalApiService object);

    void inject(DeviceIntentService object);

    void inject(AppWidgetUpdateIntentService object);

    void inject(LicenseIntentService object);

    void inject(AlarmClockIntentService object);

    void inject(ImportExportService object);

    void inject(AndFHEMApplication object);

    void inject(ConditionQueryLocaleReceiver object);


    void inject(OnOffBehavior object);

    void inject(ToggleableStrategy object);

    void inject(ConnectionChangeLocaleSettingActivity connectionChangeLocaleSettingActivity);

    void inject(SendCommandLocaleSettingActivity sendCommandLocaleSettingActivity);

    void inject(SearchResultsFragment searchResultsFragment);

    void inject(FireSettingLocaleReceiver fireSettingLocaleReceiver);

    void inject(MySearchSuggestionsProvider mySearchSuggestionsProvider);

    void inject(FcmHistoryMessagesFragment fcmHistoryMessagesFragment);

    void inject(FcmHistoryUpdatesFragment fcmHistoryUpdatesFragment);

    void inject(SettingsActivity settingsActivity);

    void inject(SettingsFragment settingsFragment);

    void inject(@NotNull AppWidgetBroadcastReceiver appWidgetBroadcastReceiver);

    void inject(@NotNull TargetStateAdditionalInformationActivity targetStateAdditionalInformationActivity);
}