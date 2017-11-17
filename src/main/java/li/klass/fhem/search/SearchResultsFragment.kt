package li.klass.fhem.search

import android.app.SearchManager
import android.content.Context
import android.os.Bundle
import android.provider.SearchRecentSuggestions
import com.google.common.base.Optional
import li.klass.fhem.dagger.ApplicationComponent
import li.klass.fhem.devices.list.ui.DeviceListFragment
import li.klass.fhem.domain.core.RoomDeviceList
import javax.inject.Inject


class SearchResultsFragment : DeviceListFragment() {
    lateinit var query: String

    @Inject
    lateinit var searchResultsProvider: SearchResultsProvider

    override fun setArguments(args: Bundle?) {
        super.setArguments(args)
        args ?: return
        query = args.getString(SearchManager.QUERY)

        saveRecentQuery()
    }

    private fun saveRecentQuery() {
        val suggestions = SearchRecentSuggestions(activity,
                MySearchSuggestionsProvider.AUTHORITY, MySearchSuggestionsProvider.MODE)
        suggestions.saveRecentQuery(query, null)
    }

    override fun getRoomDeviceListForUpdate(context: Context): RoomDeviceList = searchResultsProvider.query(query)

    override fun executeRemoteUpdate(context: Context) {
        deviceListUpdateService.updateAllDevices(Optional.absent(), context)
        appWidgetUpdateService.updateAllWidgets()
    }

    override fun inject(applicationComponent: ApplicationComponent) {
        applicationComponent.inject(this)
    }
}