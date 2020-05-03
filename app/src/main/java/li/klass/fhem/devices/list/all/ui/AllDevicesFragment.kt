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

package li.klass.fhem.devices.list.all.ui

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import li.klass.fhem.R
import li.klass.fhem.adapter.devices.core.GenericOverviewDetailDeviceAdapter
import li.klass.fhem.appwidget.update.AppWidgetUpdateService
import li.klass.fhem.connection.backend.DataConnectionSwitch
import li.klass.fhem.devices.list.all.ui.AllDevicesFragmentDirections.Companion.actionAllDevicesFragmentToRoomDetailFragment
import li.klass.fhem.devices.list.backend.ViewableRoomDeviceListProvider
import li.klass.fhem.devices.list.favorites.backend.FavoritesService
import li.klass.fhem.devices.list.ui.DeviceListFragment
import li.klass.fhem.domain.core.FhemDevice
import li.klass.fhem.domain.core.RoomDeviceList
import li.klass.fhem.room.list.ui.RoomListNavigationFragment
import li.klass.fhem.room.list.ui.RoomListNavigationViewModel
import li.klass.fhem.service.advertisement.AdvertisementService
import li.klass.fhem.update.backend.DeviceListService
import li.klass.fhem.update.backend.DeviceListUpdateService
import li.klass.fhem.util.ApplicationProperties
import li.klass.fhem.util.device.DeviceActionUIService
import javax.inject.Inject

class AllDevicesFragment @Inject constructor(
        dataConnectionSwitch: DataConnectionSwitch,
        applicationProperties: ApplicationProperties,
        viewableRoomDeviceListProvider: ViewableRoomDeviceListProvider,
        advertisementService: AdvertisementService,
        favoritesService: FavoritesService,
        genericOverviewDetailDeviceAdapter: GenericOverviewDetailDeviceAdapter,
        deviceActionUiService: DeviceActionUIService,
        private val deviceListUpdateService: DeviceListUpdateService,
        private val appWidgetUpdateService: AppWidgetUpdateService,
        private val deviceListService: DeviceListService,
        private val roomListNavigationFragment: RoomListNavigationFragment
) : DeviceListFragment(dataConnectionSwitch, applicationProperties, viewableRoomDeviceListProvider,
        advertisementService, favoritesService, genericOverviewDetailDeviceAdapter, deviceActionUiService) {

    private val navigationViewModel by navGraphViewModels<RoomListNavigationViewModel>(R.id.nav_graph)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        navigationViewModel.selectedRoom.observe(viewLifecycleOwner, Observer {
            if (it != null && isResumed) {
                findNavController().navigate(
                        actionAllDevicesFragmentToRoomDetailFragment(it)
                )
            }
        })
    }

    override fun onResume() {
        navigationViewModel.selectedRoom.postValue(null)
        super.onResume()

    }

    override fun getTitle(context: Context) = context.getString(R.string.alldevices)

    override fun executeRemoteUpdate(context: Context) {
        deviceListUpdateService.updateAllDevices()
        appWidgetUpdateService.updateAllWidgets()
    }

    override fun getRoomDeviceListForUpdate(context: Context): RoomDeviceList =
            deviceListService.getAllRoomsDeviceList()

    override val navigationFragment: Fragment = roomListNavigationFragment

    override fun navigateTo(device: FhemDevice) {
        findNavController().navigate(AllDevicesFragmentDirections.actionToDeviceDetailRedirect(device.name, null))
    }

    override val roomNameSaveKey: String = "all_devices"
}
