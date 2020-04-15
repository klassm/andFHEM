package li.klass.fhem.appwidget.ui.selection.device

import android.content.Context
import androidx.fragment.app.activityViewModels
import li.klass.fhem.R
import li.klass.fhem.appwidget.ui.selection.WidgetSelectionViewModel
import li.klass.fhem.appwidget.ui.widget.WidgetTypeProvider
import li.klass.fhem.appwidget.update.AppWidgetUpdateService
import li.klass.fhem.dagger.ApplicationComponent
import li.klass.fhem.devices.list.backend.ViewableElementsCalculator
import li.klass.fhem.domain.core.FhemDevice
import li.klass.fhem.fragments.device.DeviceNameListFragment
import li.klass.fhem.update.backend.DeviceListService
import li.klass.fhem.update.backend.DeviceListUpdateService
import javax.inject.Inject

class WidgetDeviceSelectionFragment @Inject constructor(
        deviceListService: DeviceListService,
        viewableElementsCalculator: ViewableElementsCalculator,
        deviceListUpdateService: DeviceListUpdateService,
        appWidgetUpdateService: AppWidgetUpdateService,
        private val widgetTypeProvider: WidgetTypeProvider
) : DeviceNameListFragment(deviceListService, viewableElementsCalculator, deviceListUpdateService, appWidgetUpdateService) {
    private val viewModel by activityViewModels<WidgetSelectionViewModel>()

    override fun onDeviceNameClick(child: FhemDevice) {
        viewModel.deviceClicked.postValue(child)
    }

    override fun inject(applicationComponent: ApplicationComponent) {
    }

    override val deviceFilter: DeviceFilter
        get() = object : DeviceFilter {
            override fun isSelectable(device: FhemDevice): Boolean {
                return widgetTypeProvider.getSupportedDeviceWidgetsFor(viewModel.widgetSize, device).isNotEmpty()
            }
        }

    override val emptyTextId: Int
        get() = R.string.widgetNoDevices

    override fun getTitle(context: Context): String? = context.getString(R.string.widget_devices)
}