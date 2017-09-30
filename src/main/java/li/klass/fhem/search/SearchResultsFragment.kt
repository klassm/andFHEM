package li.klass.fhem.search

import android.app.SearchManager
import android.os.Bundle
import android.provider.SearchRecentSuggestions
import com.google.common.base.Optional
import li.klass.fhem.dagger.ApplicationComponent
import li.klass.fhem.domain.core.RoomDeviceList
import li.klass.fhem.fragments.core.DeviceListFragment
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

    override fun getRoomDeviceListForUpdate(): RoomDeviceList = searchResultsProvider.query(query)

    override fun executeRemoteUpdate() {
        roomListUpdateService.updateAllDevices(Optional.absent(), context)
    }

    override fun inject(applicationComponent: ApplicationComponent) {
        applicationComponent.inject(this)
    }
}