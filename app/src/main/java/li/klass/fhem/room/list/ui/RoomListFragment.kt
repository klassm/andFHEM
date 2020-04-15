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

package li.klass.fhem.room.list.ui

import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import kotlinx.android.synthetic.main.room_list.*
import li.klass.fhem.R
import li.klass.fhem.appwidget.update.AppWidgetUpdateService
import li.klass.fhem.dagger.ApplicationComponent
import li.klass.fhem.room.list.backend.ViewableRoomListService
import li.klass.fhem.service.advertisement.AdvertisementService
import li.klass.fhem.update.backend.DeviceListUpdateService
import li.klass.fhem.update.backend.fhemweb.FhemWebConfigurationService
import javax.inject.Inject

open class RoomListFragment @Inject constructor(
        advertisementService: AdvertisementService,
        deviceListUpdateService: DeviceListUpdateService,
        roomListService: ViewableRoomListService,
        appWidgetUpdateService: AppWidgetUpdateService,
        fhemWebConfigurationService: FhemWebConfigurationService
) : RoomListSelectionFragment(advertisementService, deviceListUpdateService, roomListService, appWidgetUpdateService, fhemWebConfigurationService) {

    private val viewModel by navGraphViewModels<RoomListViewModel>(R.id.nav_graph)

    override fun inject(applicationComponent: ApplicationComponent) {
    }

    override fun onDestroyView() {
        viewModel.listState = roomList.onSaveInstanceState()
        super.onDestroyView()
    }

    override fun onClick(roomName: String) {
        findNavController().navigate(
                RoomListFragmentDirections.actionRoomListFragmentToRoomDetailFragment(roomName)
        )
    }

    override fun onUpdateDataFinished() {
        super.onUpdateDataFinished()
        roomList.onRestoreInstanceState(viewModel.listState)
    }
}
