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
import android.widget.LinearLayout
import android.widget.ListView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import li.klass.fhem.R
import li.klass.fhem.adapter.rooms.RoomListAdapter
import li.klass.fhem.appwidget.update.AppWidgetUpdateService
import li.klass.fhem.constants.Actions
import li.klass.fhem.constants.BundleExtraKeys
import li.klass.fhem.constants.BundleExtraKeys.*
import li.klass.fhem.dagger.ApplicationComponent
import li.klass.fhem.fragments.core.BaseFragment
import li.klass.fhem.room.list.backend.ViewableRoomListService
import li.klass.fhem.service.advertisement.AdvertisementService
import li.klass.fhem.ui.FragmentType
import li.klass.fhem.update.backend.DeviceListUpdateService
import li.klass.fhem.util.Reject
import java.io.Serializable
import java.util.*
import javax.inject.Inject

open class RoomListFragment : BaseFragment() {

    @Inject
    lateinit var advertisementService: AdvertisementService
    @Inject
    lateinit var deviceListUpdateService: DeviceListUpdateService
    @Inject
    lateinit var roomListService: ViewableRoomListService
    @Inject
    lateinit var appWidgetUpdateService: AppWidgetUpdateService

    private var roomName: String? = null
    private var emptyTextId = R.string.noRooms
    private var roomSelectableCallback: RoomSelectableCallback? = null
    private var roomClickedCallback: RoomClickedCallback? = null
    private var roomList: ListView? = null

    override fun inject(applicationComponent: ApplicationComponent) {
        applicationComponent.inject(this)
    }

    override fun setArguments(args: Bundle?) {
        super.setArguments(args)
        args ?: return
        roomName = args.getString(ROOM_NAME)
        emptyTextId = if (args.containsKey(EMPTY_TEXT_ID)) args.getInt(EMPTY_TEXT_ID) else R.string.noRooms
        roomSelectableCallback = args.getSerializable(ROOM_SELECTABLE_CALLBACK) as RoomSelectableCallback?
        roomClickedCallback = args.getSerializable(ON_CLICKED_CALLBACK) as RoomClickedCallback?
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val superView = super.onCreateView(inflater, container, savedInstanceState)
        if (superView != null) return superView
        val myActivity = activity ?: return superView

        val adapter = RoomListAdapter(myActivity, R.layout.room_list_name, ArrayList<String>())
        val layout = inflater.inflate(R.layout.room_list, container, false)
        advertisementService.addAd(layout, myActivity)

        assert(layout != null)

        val emptyView = layout!!.findViewById<LinearLayout>(R.id.emptyView)
        fillEmptyView(emptyView, emptyTextId, container!!)

        roomList = layout.findViewById(R.id.roomList)
        Reject.ifNull<ListView>(roomList)
        roomList!!.adapter = adapter

        roomList!!.onItemClickListener = AdapterView.OnItemClickListener { _, view, _, _ ->
            val roomName = view.tag.toString()
            onClick(roomName)
        }

        return layout
    }

    override fun canChildScrollUp(): Boolean {
        if (roomList?.canScrollVertically(-1) == true) {
            return true
        }
        return super.canChildScrollUp()
    }

    protected open fun onClick(roomName: String) {
        if (roomClickedCallback != null) {
            roomClickedCallback!!.onRoomClicked(roomName)
        } else {
            val intent = Intent(Actions.SHOW_FRAGMENT)
            intent.putExtra(BundleExtraKeys.FRAGMENT, FragmentType.ROOM_DETAIL)
            intent.putExtra(ROOM_NAME, roomName)

            activity?.sendBroadcast(intent)
        }
    }

    override suspend fun update(refresh: Boolean) {
        if (view == null) return

        hideEmptyView()
        if (refresh && !isNavigation)
            activity?.sendBroadcast(Intent(Actions.SHOW_EXECUTING_DIALOG))

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

    override fun getTitle(context: Context): CharSequence? = context.getString(R.string.roomList)

    private fun isRoomSelectable(roomName: String): Boolean =
            roomSelectableCallback == null || roomSelectableCallback!!.isRoomSelectable(roomName)

    private val adapter: RoomListAdapter?
        get() {
            if (view == null) return null

            val listView = view!!.findViewById<ListView>(R.id.roomList)
            return listView.adapter as RoomListAdapter
        }

    private fun scrollToSelectedRoom(selectedRoom: String?, roomList: List<String>) {
        if (selectedRoom == null) return

        val view = view ?: return

        val roomListView = view.findViewById<ListView>(R.id.roomList) as ListView

        for (i in roomList.indices) {
            val roomName = roomList[i]
            if (roomName == selectedRoom) {
                roomListView.setSelection(i)
                return
            }
        }
    }

    interface RoomSelectableCallback : Serializable {
        fun isRoomSelectable(roomName: String): Boolean
    }

    interface RoomClickedCallback : Serializable {
        fun onRoomClicked(roomName: String)
    }

    private fun handleUpdateData(roomNameList: List<String>) {
        val selectableRooms = roomNameList.filter { isRoomSelectable(it) }.toList()

        val adapter = adapter!!
        if (selectableRooms.isEmpty()) {
            showEmptyView()
            adapter.updateData(selectableRooms)
        } else {
            adapter.updateData(selectableRooms.toMutableList(), roomName)
            scrollToSelectedRoom(roomName, adapter.data)
        }
    }
}
