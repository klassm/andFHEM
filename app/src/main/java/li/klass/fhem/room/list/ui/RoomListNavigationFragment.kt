package li.klass.fhem.room.list.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ListView
import androidx.navigation.navGraphViewModels
import li.klass.fhem.R
import li.klass.fhem.appwidget.update.AppWidgetUpdateService
import li.klass.fhem.databinding.RoomListBinding
import li.klass.fhem.room.list.backend.ViewableRoomListService
import li.klass.fhem.service.advertisement.AdvertisementService
import li.klass.fhem.update.backend.DeviceListUpdateService
import javax.inject.Inject

class RoomListNavigationFragment @Inject constructor(
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
    private val viewModel by navGraphViewModels<RoomListNavigationViewModel>(R.id.nav_graph)
    private lateinit var viewBinding: RoomListBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        if (view != null) {
            viewBinding = RoomListBinding.bind(view)
            return view
        }

        viewBinding = RoomListBinding.inflate(inflater, container, false)

        fillView(viewBinding.root)
        return viewBinding.root
    }

    override fun onClick(roomName: String) {
        viewModel.selectedRoom.postValue(roomName)
    }

    override val selectedRoomName: String?
        get() = viewModel.selectedRoom.value

    override val canUpdateRemotely: Boolean = false

    override fun getTitle(context: Context): String? = null

    override val layout: View
        get() = viewBinding.roomList
    override val roomListView: ListView
        get() = viewBinding.roomList
    override val emptyView: LinearLayout? = null

    override fun mayPullToRefresh(): Boolean = false
}