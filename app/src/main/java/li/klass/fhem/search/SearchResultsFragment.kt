package li.klass.fhem.search

import android.content.Context
import android.os.Bundle
import android.provider.SearchRecentSuggestions
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import li.klass.fhem.R
import li.klass.fhem.adapter.devices.core.GenericOverviewDetailDeviceAdapter
import li.klass.fhem.appwidget.update.AppWidgetUpdateService
import li.klass.fhem.connection.backend.DataConnectionSwitch
import li.klass.fhem.devices.list.backend.ViewableRoomDeviceListProvider
import li.klass.fhem.devices.list.favorites.backend.FavoritesService
import li.klass.fhem.devices.list.ui.DeviceListFragment
import li.klass.fhem.domain.core.FhemDevice
import li.klass.fhem.domain.core.RoomDeviceList
import li.klass.fhem.service.advertisement.AdvertisementService
import li.klass.fhem.update.backend.DeviceListUpdateService
import li.klass.fhem.util.ApplicationProperties
import li.klass.fhem.util.device.DeviceActionUIService
import javax.inject.Inject

class SearchResultsFragment @Inject constructor(
        private val searchResultsProvider: SearchResultsProvider,
        dataConnectionSwitch: DataConnectionSwitch,
        applicationProperties: ApplicationProperties,
        viewableRoomDeviceListProvider: ViewableRoomDeviceListProvider,
        advertisementService: AdvertisementService,
        favoritesService: FavoritesService,
        genericOverviewDetailDeviceAdapter: GenericOverviewDetailDeviceAdapter,
        deviceActionUiService: DeviceActionUIService,
        private val deviceListUpdateService: DeviceListUpdateService,
        private val appWidgetUpdateService: AppWidgetUpdateService
) : DeviceListFragment(dataConnectionSwitch, applicationProperties, viewableRoomDeviceListProvider,
        advertisementService, favoritesService, genericOverviewDetailDeviceAdapter, deviceActionUiService) {
    val args: SearchResultsFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        saveRecentQuery()
    }

    private fun saveRecentQuery() {
        val context: Context = activity ?: return
        @Suppress("SENSELESS_COMPARISON") // can be nullable according to annotations
        if (context != null) {
            val suggestions =
                    SearchRecentSuggestions(context, MySearchSuggestionsProvider.AUTHORITY,
                            MySearchSuggestionsProvider.MODE)
            suggestions.saveRecentQuery(args.query, null)
        }
    }

    override fun getRoomDeviceListForUpdate(context: Context): RoomDeviceList = searchResultsProvider.query(args.query)

    override fun executeRemoteUpdate(context: Context) {
        deviceListUpdateService.updateAllDevices()
        appWidgetUpdateService.updateAllWidgets()
    }

    override fun navigateTo(device: FhemDevice) {
        findNavController().navigate(SearchResultsFragmentDirections.actionToDeviceDetailRedirect(device.name, null))
    }

    override val roomNameSaveKey: String? = null

    override fun getTitle(context: Context): String = context.resources.getString(R.string.search_title)
}