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

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ListView
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import li.klass.fhem.R
import li.klass.fhem.appwidget.update.AppWidgetUpdateService
import li.klass.fhem.databinding.RoomListPageBinding
import li.klass.fhem.room.list.backend.ViewableRoomListService
import li.klass.fhem.service.advertisement.AdvertisementService
import li.klass.fhem.update.backend.DeviceListUpdateService
import javax.inject.Inject

open class RoomListFragment @Inject constructor(
    advertisementService: AdvertisementService,
    deviceListUpdateService: DeviceListUpdateService,
    roomListService: ViewableRoomListService,
    appWidgetUpdateService: AppWidgetUpdateService
) : RoomListSelectionFragment(
    advertisementService,
    deviceListUpdateService,
    roomListService,
    appWidgetUpdateService
) {

    private val viewModel by navGraphViewModels<RoomListViewModel>(R.id.nav_graph)
    private lateinit var viewBinding: RoomListPageBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        if (view != null) {
            viewBinding = RoomListPageBinding.bind(view)
            return view
        }

        viewBinding = RoomListPageBinding.inflate(inflater, container, false)

        fillView(viewBinding.root)
        return viewBinding.root
    }

    override fun onDestroyView() {
        viewModel.listState = roomListView.onSaveInstanceState()
        super.onDestroyView()
    }

    override fun onClick(roomName: String) {
        findNavController().navigate(
            RoomListFragmentDirections.actionRoomListFragmentToRoomDetailFragment(roomName)
        )
    }

    override fun onUpdateDataFinished() {
        super.onUpdateDataFinished()
        viewModel.listState?.let {
            roomListView.onRestoreInstanceState(it)
        }
    }

    override val roomListView: ListView
        get() = viewBinding.roomList.roomList
    override val emptyView: LinearLayout
        get() = viewBinding.deviceViewHeader.emptyView
    override val layout: View
        get() = viewBinding.root
}
