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

package li.klass.fhem.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.view.ViewCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.LinearLayout
import android.widget.ListView
import com.google.common.base.Optional
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import li.klass.fhem.R
import li.klass.fhem.adapter.rooms.RoomListAdapter
import li.klass.fhem.constants.Actions
import li.klass.fhem.constants.BundleExtraKeys
import li.klass.fhem.constants.BundleExtraKeys.*
import li.klass.fhem.dagger.ApplicationComponent
import li.klass.fhem.fragments.core.BaseFragment
import li.klass.fhem.service.advertisement.AdvertisementService
import li.klass.fhem.service.room.RoomListService
import li.klass.fhem.service.room.RoomListUpdateService
import li.klass.fhem.util.Reject
import org.jetbrains.anko.coroutines.experimental.bg
import java.io.Serializable
import java.util.*
import javax.inject.Inject

open class RoomListFragment : BaseFragment() {

    @Inject
    lateinit var advertisementService: AdvertisementService
    @Inject
    lateinit var roomListUpdateService: RoomListUpdateService
    @Inject
    lateinit var roomListService: RoomListService

    private var roomName: String? = null
    protected var emptyTextId = R.string.noRooms
        private set
    private var roomSelectableCallback: RoomSelectableCallback? = null
    private var roomClickedCallback: RoomClickedCallback? = null
    private var roomList: ListView? = null

    override fun inject(applicationComponent: ApplicationComponent) {
        applicationComponent.inject(this)
    }

    override fun setArguments(args: Bundle) {
        super.setArguments(args)
        roomName = args.getString(ROOM_NAME)
        emptyTextId = if (args.containsKey(EMPTY_TEXT_ID)) args.getInt(EMPTY_TEXT_ID) else R.string.noRooms
        roomSelectableCallback = args.getSerializable(ROOM_SELECTABLE_CALLBACK) as RoomSelectableCallback?
        roomClickedCallback = args.getSerializable(ON_CLICKED_CALLBACK) as RoomClickedCallback?
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val superView = super.onCreateView(inflater, container, savedInstanceState)
        if (superView != null) return superView

        val adapter = RoomListAdapter(activity, R.layout.room_list_name, ArrayList<String>())
        val layout = inflater!!.inflate(R.layout.room_list, container, false)
        advertisementService.addAd(layout, activity)

        assert(layout != null)

        val emptyView = layout!!.findViewById(R.id.emptyView) as LinearLayout
        fillEmptyView(emptyView, emptyTextId, container!!)

        roomList = layout.findViewById(R.id.roomList) as ListView
        Reject.ifNull<ListView>(roomList)
        roomList!!.adapter = adapter

        roomList!!.onItemClickListener = AdapterView.OnItemClickListener { adapterView, view, i, l ->
            val roomName = view.tag.toString()
            onClick(roomName)
        }

        return layout
    }

    override fun canChildScrollUp(): Boolean {
        if (ViewCompat.canScrollVertically(roomList, -1)) {
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

            activity.sendBroadcast(intent)
        }
    }

    override fun update(refresh: Boolean) {
        if (view == null) return

        hideEmptyView()
        if (refresh && !isNavigation)
            activity.sendBroadcast(Intent(Actions.SHOW_EXECUTING_DIALOG))

        async(UI) {
            val roomNameList = bg {
                if (refresh) {
                    roomListUpdateService.updateAllDevices(Optional.absent(), activity)
                }
                roomListService.getRoomNameList(Optional.absent(), activity)
            }.await()
            handleUpdateData(roomNameList)
        }
    }

    override fun getTitle(context: Context): CharSequence? {
        return context.getString(R.string.roomList)
    }

    protected fun isRoomSelectable(roomName: String): Boolean {
        return roomSelectableCallback == null || roomSelectableCallback!!.isRoomSelectable(roomName)
    }

    private val adapter: RoomListAdapter?
        get() {
            if (view == null) return null

            val listView = view!!.findViewById(R.id.roomList) as ListView
            return listView.adapter as RoomListAdapter
        }

    private fun scrollToSelectedRoom(selectedRoom: String?, roomList: List<String>) {
        if (selectedRoom == null) return

        val view = view ?: return

        val roomListView = view.findViewById(R.id.roomList) as ListView ?: return

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
            adapter.updateData(selectableRooms, roomName)
            scrollToSelectedRoom(roomName, adapter.data)
        }
    }
}
