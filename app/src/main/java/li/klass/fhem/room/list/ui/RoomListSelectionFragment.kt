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
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.LinearLayout
import android.widget.ListView
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
import java.util.*

abstract class RoomListSelectionFragment(
    private val advertisementService: AdvertisementService,
    private val deviceListUpdateService: DeviceListUpdateService,
    private val roomListService: ViewableRoomListService,
    private val appWidgetUpdateService: AppWidgetUpdateService
) : BaseFragment() {

    private var emptyTextId = R.string.noRooms


    fun fillView(rootView: ViewGroup) {
        val activity = activity ?: return
        val adapter = RoomListAdapter(activity, ArrayList())
        advertisementService.addAd(layout, activity)


        emptyView?.let {
            fillEmptyView(it, emptyTextId, rootView)
        }

        roomListView.adapter = adapter

        roomListView.onItemClickListener = AdapterView.OnItemClickListener { _, view, _, _ ->
            val roomName = view.tag.toString()
            onClick(roomName)
        }

        updateAsync(false)
    }

    abstract val layout: View

    abstract val roomListView: ListView

    abstract val emptyView: LinearLayout?

    override fun canChildScrollUp(): Boolean {
        if (roomListView.canScrollVertically(-1)) {
            return true
        }
        return super.canChildScrollUp()
    }

    protected abstract fun onClick(roomName: String)

    override suspend fun update(refresh: Boolean) {
        if (view == null) return

        hideEmptyView()
        if (refresh) {
            activity?.sendBroadcast(Intent(Actions.SHOW_EXECUTING_DIALOG).apply { setPackage(context?.packageName) })
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

    private val adapter: RoomListAdapter
        get() = roomListView.adapter as RoomListAdapter

    private fun scrollToSelectedRoom(selectedRoom: String?, roomList: List<String>) {
        if (selectedRoom == null) return

        for (i in roomList.indices) {
            val roomName = roomList[i]
            if (roomName == selectedRoom) {
                roomListView.setSelection(i)
                return
            }
        }
    }

    private fun handleUpdateData(roomNameList: List<String>) {
        val selectableRooms = roomNameList.filter { isRoomSelectable(it) }.toList()

        if (selectableRooms.isEmpty()) {
            showEmptyView()
            adapter.updateData(selectableRooms.toMutableList())

        } else {
            adapter.updateData(selectableRooms.toMutableList(), selectedRoomName)
            scrollToSelectedRoom(selectedRoomName, adapter.getData())

            onUpdateDataFinished()
        }
    }
}
