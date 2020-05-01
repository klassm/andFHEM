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

package li.klass.fhem.devices.list.favorites.ui

import android.content.Context
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.navigation.fragment.findNavController
import li.klass.fhem.R
import li.klass.fhem.adapter.devices.core.GenericOverviewDetailDeviceAdapter
import li.klass.fhem.appwidget.update.AppWidgetUpdateService
import li.klass.fhem.connection.backend.DataConnectionSwitch
import li.klass.fhem.devices.list.backend.ViewableRoomDeviceListProvider
import li.klass.fhem.devices.list.favorites.backend.FavoritesService
import li.klass.fhem.devices.list.ui.DeviceListFragment
import li.klass.fhem.domain.core.FhemDevice
import li.klass.fhem.service.advertisement.AdvertisementService
import li.klass.fhem.update.backend.DeviceListUpdateService
import li.klass.fhem.util.ApplicationProperties
import li.klass.fhem.util.Reject
import li.klass.fhem.util.device.DeviceActionUIService
import javax.inject.Inject

class FavoritesFragment @Inject constructor(
        dataConnectionSwitch: DataConnectionSwitch,
        applicationProperties: ApplicationProperties,
        viewableRoomDeviceListProvider: ViewableRoomDeviceListProvider,
        advertisementService: AdvertisementService,
        genericOverviewDetailDeviceAdapter: GenericOverviewDetailDeviceAdapter,
        deviceActionUiService: DeviceActionUIService,
        private val favoritesService: FavoritesService,
        private val deviceListUpdateService: DeviceListUpdateService,
        private val appWidgetUpdateService: AppWidgetUpdateService
) : DeviceListFragment(dataConnectionSwitch, applicationProperties, viewableRoomDeviceListProvider,
        advertisementService, favoritesService, genericOverviewDetailDeviceAdapter, deviceActionUiService) {

    override fun fillEmptyView(view: LinearLayout, viewGroup: ViewGroup) {
        val inflater = activity?.layoutInflater
        Reject.ifNull(inflater)
        view.addView(inflater?.inflate(R.layout.favorites_empty_view, viewGroup, false))
    }

    override fun getTitle(context: Context) = context.getString(R.string.favorites)

    override fun getRoomDeviceListForUpdate(context: Context) = favoritesService.getFavorites()

    override fun executeRemoteUpdate(context: Context) {
        deviceListUpdateService.updateAllDevices()
        appWidgetUpdateService.updateAllWidgets()
    }

    override fun navigateTo(device: FhemDevice) {
        findNavController().navigate(FavoritesFragmentDirections.actionToDeviceDetailRedirect(device.name, null))
    }
}
