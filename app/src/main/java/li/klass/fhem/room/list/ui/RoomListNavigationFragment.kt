package li.klass.fhem.room.list.ui

import android.content.Context
import androidx.navigation.navGraphViewModels
import li.klass.fhem.R
import li.klass.fhem.appwidget.update.AppWidgetUpdateService
import li.klass.fhem.room.list.backend.ViewableRoomListService
import li.klass.fhem.service.advertisement.AdvertisementService
import li.klass.fhem.update.backend.DeviceListUpdateService
import li.klass.fhem.update.backend.fhemweb.FhemWebConfigurationService
import javax.inject.Inject

class RoomListNavigationFragment @Inject constructor(
        advertisementService: AdvertisementService,
        deviceListUpdateService: DeviceListUpdateService,
        roomListService: ViewableRoomListService,
        appWidgetUpdateService: AppWidgetUpdateService,
        fhemWebConfigurationService: FhemWebConfigurationService
) : RoomListSelectionFragment(advertisementService, deviceListUpdateService, roomListService, appWidgetUpdateService, fhemWebConfigurationService) {
    private val viewModel by navGraphViewModels<RoomListNavigationViewModel>(R.id.nav_graph)

    override fun onClick(roomName: String) {
        viewModel.selectedRoom.postValue(roomName)
    }

    override val selectedRoomName: String?
        get() = viewModel.selectedRoom.value

    override val canUpdateRemotely: Boolean = false

    override fun getTitle(context: Context): String? = null

    override val layout: Int = R.layout.room_list

    override fun mayPullToRefresh(): Boolean = false
}