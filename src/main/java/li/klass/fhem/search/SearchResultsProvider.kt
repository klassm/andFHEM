package li.klass.fhem.search

import com.google.common.base.Optional
import li.klass.fhem.AndFHEMApplication
import li.klass.fhem.domain.core.RoomDeviceList
import li.klass.fhem.service.room.RoomListService
import java.util.*
import javax.inject.Inject

class SearchResultsProvider @Inject constructor(val roomListService: RoomListService) {
    fun query(query: String): RoomDeviceList {
        if (query.trim().isEmpty()) {
            return RoomDeviceList(RoomDeviceList.ALL_DEVICES_ROOM)
        }
        val comparableQuery = toComparable(query)
        val context = AndFHEMApplication.getApplication().applicationContext
        val allRoomsList = roomListService.getAllRoomsDeviceList(Optional.absent(), context)
        return allRoomsList.filter(context, {
            (toComparable(it.name).contains(comparableQuery)
                    || toComparable(it.aliasOrName).contains(comparableQuery)
                    || toComparable(it.roomConcatenated).contains(comparableQuery))
        })

    }

    private fun toComparable(input: String): String {
        return input.toLowerCase(Locale.getDefault())
                .replace(Regex("[^a-z0-9 ]"), "")
    }
}