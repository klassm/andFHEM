package li.klass.fhem.dagger

import androidx.fragment.app.Fragment
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import li.klass.fhem.adapter.devices.core.detail.DeviceDetailRedirectFragment
import li.klass.fhem.connection.ui.ConnectionDetailFragment
import li.klass.fhem.connection.ui.ConnectionListFragment
import li.klass.fhem.conversion.ui.ConversionFragment
import li.klass.fhem.devices.detail.ui.DeviceDetailFragment
import li.klass.fhem.devices.list.all.ui.AllDevicesFragment
import li.klass.fhem.devices.list.favorites.ui.FavoritesFragment
import li.klass.fhem.fcm.history.view.FcmHistoryFragment
import li.klass.fhem.fcm.history.view.FcmHistoryMessagesFragment
import li.klass.fhem.fcm.history.view.FcmHistoryUpdatesFragment
import li.klass.fhem.floorplan.ui.FloorplanFragment
import li.klass.fhem.fragments.MainFragment
import li.klass.fhem.fragments.device.DeviceNameListNavigationFragment
import li.klass.fhem.fragments.device.DeviceNameSelectionFragment
import li.klass.fhem.fragments.weekprofile.FromToWeekProfileFragment
import li.klass.fhem.fragments.weekprofile.IntervalWeekProfileFragment
import li.klass.fhem.room.detail.ui.RoomDetailFragment
import li.klass.fhem.room.list.ui.RoomListFragment
import li.klass.fhem.search.SearchResultsFragment
import li.klass.fhem.sendCommand.ui.SendCommandFragment
import li.klass.fhem.timer.ui.TimerDetailFragment
import li.klass.fhem.timer.ui.TimerListFragment
import li.klass.fhem.ui.WebViewFragment

@Module
interface FragmentsBindingModule {
    @Binds
    @IntoMap
    @FragmentKey(MainFragment::class)
    fun bindMainFragment(fragment: MainFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(RoomListFragment::class)
    fun bindRoomListFragment(fragment: RoomListFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(RoomDetailFragment::class)
    fun bindRoomDetailFragment(fragment: RoomDetailFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(FavoritesFragment::class)
    fun bindFavoritesFragment(fragment: FavoritesFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(AllDevicesFragment::class)
    fun bindAllDevicesFragment(fragment: AllDevicesFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(DeviceDetailRedirectFragment::class)
    fun bindDeviceRedirectFragment(fragment: DeviceDetailRedirectFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(FloorplanFragment::class)
    fun bindFloorplanFragment(fragment: FloorplanFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(WebViewFragment::class)
    fun bindWebViewFragment(fragment: WebViewFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(DeviceDetailFragment::class)
    fun bindDeviceDetailFragment(fragment: DeviceDetailFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(SearchResultsFragment::class)
    fun bindSearchResultsFragment(fragment: SearchResultsFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(ConnectionListFragment::class)
    fun bindConnectionListFragment(fragment: ConnectionListFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(ConnectionDetailFragment::class)
    fun bindConnectionDetailFragment(fragment: ConnectionDetailFragment): Fragment
    @Binds
    @IntoMap
    @FragmentKey(TimerListFragment::class)
    fun bindTimerListFragment(fragment: TimerListFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(TimerDetailFragment::class)
    fun bindTimerDetailFragment(fragment: TimerDetailFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(SendCommandFragment::class)
    fun bindSendCommandFragment(fragment: SendCommandFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(ConversionFragment::class)
    fun bindConversionFragment(fragment: ConversionFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(FcmHistoryFragment::class)
    fun bindFcmHistoryFragment(fragment: FcmHistoryFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(FcmHistoryUpdatesFragment::class)
    fun bindFcmHistoryUpdatesFragment(fragment: FcmHistoryUpdatesFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(FcmHistoryMessagesFragment::class)
    fun bindFcmHistoryMessagesFragment(fragment: FcmHistoryMessagesFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(FromToWeekProfileFragment::class)
    fun bindFromToWeekProfileFragment(fragment: FromToWeekProfileFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(IntervalWeekProfileFragment::class)
    fun bindIntervalWeekProfileFragment(fragment: IntervalWeekProfileFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(DeviceNameSelectionFragment::class)
    fun bindDeviceNameSelectionFragment(fragment: DeviceNameSelectionFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(DeviceNameListNavigationFragment::class)
    fun bindDeviceNameListNavigationFragment(fragment: DeviceNameListNavigationFragment): Fragment
}