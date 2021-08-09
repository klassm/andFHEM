package li.klass.fhem.appwidget.ui.selection.room

import androidx.fragment.app.activityViewModels
import li.klass.fhem.appwidget.ui.selection.WidgetSelectionViewModel
import li.klass.fhem.appwidget.ui.widget.WidgetTypeProvider
import li.klass.fhem.appwidget.update.AppWidgetUpdateService
import li.klass.fhem.room.list.backend.ViewableRoomListService
import li.klass.fhem.room.list.ui.RoomListSelectionFragment
import li.klass.fhem.update.backend.DeviceListUpdateService
import li.klass.fhem.update.backend.fhemweb.FhemWebConfigurationService
import javax.inject.Inject

class RoomWidgetSelectionFragment @Inject constructor(
    deviceListUpdateService: DeviceListUpdateService,
    roomListService: ViewableRoomListService,
    appWidgetUpdateService: AppWidgetUpdateService,
    fhemWebConfigurationService: FhemWebConfigurationService,
    private val widgetTypeProvider: WidgetTypeProvider
) : RoomListSelectionFragment(
    deviceListUpdateService,
    roomListService,
    appWidgetUpdateService,
    fhemWebConfigurationService
) {

    private val viewModel by activityViewModels<WidgetSelectionViewModel>()

    override fun isRoomSelectable(roomName: String): Boolean =
        widgetTypeProvider.getSupportedRoomWidgetsFor(viewModel.widgetSize).isNotEmpty()

    override fun onClick(roomName: String) = viewModel.roomClicked.postValue(roomName)

    override val layout: Int = li.klass.fhem.R.layout.room_list_page
}