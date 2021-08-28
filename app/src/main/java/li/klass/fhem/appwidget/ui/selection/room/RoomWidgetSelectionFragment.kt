package li.klass.fhem.appwidget.ui.selection.room

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ListView
import androidx.fragment.app.activityViewModels
import li.klass.fhem.appwidget.ui.selection.WidgetSelectionViewModel
import li.klass.fhem.appwidget.ui.widget.WidgetTypeProvider
import li.klass.fhem.appwidget.update.AppWidgetUpdateService
import li.klass.fhem.databinding.RoomListPageBinding
import li.klass.fhem.room.list.backend.ViewableRoomListService
import li.klass.fhem.room.list.ui.RoomListSelectionFragment
import li.klass.fhem.service.advertisement.AdvertisementService
import li.klass.fhem.update.backend.DeviceListUpdateService
import javax.inject.Inject

class RoomWidgetSelectionFragment @Inject constructor(
    advertisementService: AdvertisementService,
    deviceListUpdateService: DeviceListUpdateService,
    roomListService: ViewableRoomListService,
    appWidgetUpdateService: AppWidgetUpdateService,
    private val widgetTypeProvider: WidgetTypeProvider
) : RoomListSelectionFragment(
    advertisementService,
    deviceListUpdateService,
    roomListService,
    appWidgetUpdateService
) {

    private val viewModel by activityViewModels<WidgetSelectionViewModel>()
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

    override fun isRoomSelectable(roomName: String): Boolean =
        widgetTypeProvider.getSupportedRoomWidgetsFor(viewModel.widgetSize).isNotEmpty()

    override fun onClick(roomName: String) = viewModel.roomClicked.postValue(roomName)

    override val roomListView: ListView
        get() = viewBinding.roomList.roomList
    override val emptyView: LinearLayout
        get() = viewBinding.deviceViewHeader.emptyView
    override val layout: View
        get() = viewBinding.root
}