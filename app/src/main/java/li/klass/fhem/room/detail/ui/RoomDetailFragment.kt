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

package li.klass.fhem.room.detail.ui

import android.content.Context
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import li.klass.fhem.adapter.devices.core.GenericOverviewDetailDeviceAdapter
import li.klass.fhem.appwidget.update.AppWidgetUpdateService
import li.klass.fhem.connection.backend.DataConnectionSwitch
import li.klass.fhem.devices.list.backend.ViewableRoomDeviceListProvider
import li.klass.fhem.devices.list.favorites.backend.FavoritesService
import li.klass.fhem.devices.list.ui.DeviceListFragment
import li.klass.fhem.domain.core.FhemDevice
import li.klass.fhem.service.advertisement.AdvertisementService
import li.klass.fhem.settings.SettingsKeys.UPDATE_ON_ROOM_OPEN
import li.klass.fhem.update.backend.DeviceListService
import li.klass.fhem.update.backend.DeviceListUpdateService
import li.klass.fhem.util.ApplicationProperties
import li.klass.fhem.util.device.DeviceActionUIService
import javax.inject.Inject

class RoomDetailFragment @Inject constructor(
        dataConnectionSwitch: DataConnectionSwitch,
        viewableRoomDeviceListProvider: ViewableRoomDeviceListProvider,
        advertisementService: AdvertisementService,
        favoritesService: FavoritesService,
        genericOverviewDetailDeviceAdapter: GenericOverviewDetailDeviceAdapter,
        deviceActionUiService: DeviceActionUIService,
        private val deviceListService: DeviceListService,
        private val applicationProperties: ApplicationProperties,
        private val deviceListUpdateService: DeviceListUpdateService,
        private val appWidgetUpdateService: AppWidgetUpdateService
) : DeviceListFragment(dataConnectionSwitch, applicationProperties, viewableRoomDeviceListProvider,
        advertisementService, favoritesService, genericOverviewDetailDeviceAdapter, deviceActionUiService) {

    val args: RoomDetailFragmentArgs by navArgs()

    override fun onResume() {
        super.onResume()
        val updateOnRoomOpen = applicationProperties.getBooleanSharedPreference(UPDATE_ON_ROOM_OPEN, false)
        if (updateOnRoomOpen) {
            updateAsync(true)
        }
    }

    override fun getTitle(context: Context) = args.roomName

    override fun getRoomDeviceListForUpdate(context: Context) = deviceListService.getDeviceListForRoom(args.roomName)

    override fun executeRemoteUpdate(context: Context) {
        deviceListUpdateService.updateRoom(args.roomName)
        appWidgetUpdateService.updateAllWidgets()
    }

    override fun navigateTo(device: FhemDevice) {
        findNavController().navigate(RoomDetailFragmentDirections.actionToDeviceDetailRedirect(device.name, null))
    }
}
