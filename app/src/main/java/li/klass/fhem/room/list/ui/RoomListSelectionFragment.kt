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

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ListView
import kotlinx.android.synthetic.main.device_view_header.view.*
import kotlinx.android.synthetic.main.room_list.*
import kotlinx.android.synthetic.main.room_list.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import li.klass.fhem.R
import li.klass.fhem.adapter.rooms.RoomListAdapter
import li.klass.fhem.appwidget.update.AppWidgetUpdateService
import li.klass.fhem.constants.Actions
import li.klass.fhem.fragments.core.BaseFragment
import li.klass.fhem.room.list.backend.ViewableRoomListService
import li.klass.fhem.service.advertisement.AdvertisementService
import li.klass.fhem.update.backend.DeviceListUpdateService
import li.klass.fhem.update.backend.fhemweb.FhemWebConfigurationService
import java.util.*

abstract class RoomListSelectionFragment constructor(
        private val advertisementService: AdvertisementService,
        private val deviceListUpdateService: DeviceListUpdateService,
        private val roomListService: ViewableRoomListService,
        private val appWidgetUpdateService: AppWidgetUpdateService,
        private val fhemWebConfigurationService: FhemWebConfigurationService
) : BaseFragment() {

    private var emptyTextId = R.string.noRooms


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val superView = super.onCreateView(inflater, container, savedInstanceState)
        if (superView != null) return superView
        val myActivity = activity ?: return superView
        retainInstance = true

        val hiddenRooms = fhemWebConfigurationService.getHiddenRooms()

        val adapter = RoomListAdapter(myActivity, R.layout.room_list_name, ArrayList(), hiddenRooms)
        val layout = inflater.inflate(layout, container, false) ?: return null
        advertisementService.addAd(layout, myActivity)

        layout.emptyView?.let {
            fillEmptyView(it, emptyTextId, container!!)
        }

        layout.roomList.adapter = adapter

        layout.roomList.onItemClickListener = AdapterView.OnItemClickListener { _, view, _, _ ->
            val roomName = view.tag.toString()
            onClick(roomName)
        }

        updateAsync(false)

        return layout
    }

    abstract val layout: Int;

    override fun canChildScrollUp(): Boolean {
        if (roomList?.canScrollVertically(-1) == true) {
            return true
        }
        return super.canChildScrollUp()
    }

    protected abstract fun onClick(roomName: String)

    override suspend fun update(refresh: Boolean) {
        if (view == null) return

        hideEmptyView()
        if (refresh) {
            activity?.sendBroadcast(Intent(Actions.SHOW_EXECUTING_DIALOG))
        }

        coroutineScope {
            val roomNameList = withContext(Dispatchers.IO) {
                if (refresh) {
                    deviceListUpdateService.updateAllDevices()
                    appWidgetUpdateService.updateAllWidgets()
                }
                roomListService.sortedRoomNameList()
            }
            handleUpdateData(roomNameList)
        }
    }

    override fun getTitle(context: Context): String? = context.getString(R.string.roomList)

    open fun isRoomSelectable(roomName: String): Boolean = true

    open fun onUpdateDataFinished() {}

    open val selectedRoomName: String? = null

    private val adapter: RoomListAdapter?
        get() {
            if (view == null) return null

            val listView = view!!.findViewById<ListView>(R.id.roomList)
            return listView.adapter as RoomListAdapter
        }

    private fun scrollToSelectedRoom(selectedRoom: String?, roomList: List<String>) {
        if (selectedRoom == null) return

        val view = view ?: return

        for (i in roomList.indices) {
            val roomName = roomList[i]
            if (roomName == selectedRoom) {
                view.roomList.setSelection(i)
                return
            }
        }
    }

    private fun handleUpdateData(roomNameList: List<String>) {
        val selectableRooms = roomNameList.filter { isRoomSelectable(it) }.toList()

        val adapter = adapter ?: return
        if (selectableRooms.isEmpty()) {
            showEmptyView()
            adapter.updateData(selectableRooms)

        } else {
            adapter.updateData(selectableRooms.toMutableList(), selectedRoomName)
            scrollToSelectedRoom(selectedRoomName, adapter.data)

            onUpdateDataFinished()
        }
    }
}
