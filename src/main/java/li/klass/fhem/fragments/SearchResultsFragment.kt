package li.klass.fhem.fragments

import android.app.SearchManager
import android.os.Bundle
import com.google.common.base.Optional
import li.klass.fhem.dagger.ApplicationComponent
import li.klass.fhem.domain.core.RoomDeviceList
import li.klass.fhem.fragments.core.DeviceListFragment
import li.klass.fhem.service.room.RoomListService
import java.util.*
import javax.inject.Inject


class SearchResultsFragment : DeviceListFragment() {
    lateinit var query: String

    @Inject
    lateinit var roomListService: RoomListService

    override fun setArguments(args: Bundle?) {
        super.setArguments(args)
        args ?: return
        query = toComparable(args.getString(SearchManager.QUERY))
    }

    override fun getRoomDeviceListForUpdate(): RoomDeviceList {
        val allRoomsList = roomListService.getAllRoomsDeviceList(Optional.absent(), activity)
        return allRoomsList.filter(activity, {
            val matches = (toComparable(it.name).contains(query)
                    || toComparable(it.aliasOrName).contains(query)
                    || toComparable(it.roomConcatenated).contains(query))
            if (matches) {
                println("hallo")
            }
            matches
        })
    }

    override fun executeRemoteUpdate() {
        roomListUpdateService.updateAllDevices(Optional.absent(), context)
    }

    override fun inject(applicationComponent: ApplicationComponent) {
        applicationComponent.inject(this)
    }

    private fun toComparable(input: String): String {
        return input.toLowerCase(Locale.getDefault())
                .replace(Regex("[^a-z0-9 ]"), "")
    }
}