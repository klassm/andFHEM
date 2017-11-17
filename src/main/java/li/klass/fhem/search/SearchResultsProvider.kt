package li.klass.fhem.search

import li.klass.fhem.AndFHEMApplication
import li.klass.fhem.domain.core.RoomDeviceList
import li.klass.fhem.update.backend.DeviceListService
import java.util.*
import javax.inject.Inject

class SearchResultsProvider @Inject constructor(val deviceListService: DeviceListService) {
    fun query(query: String): RoomDeviceList {
        if (query.trim().isEmpty()) {
            return RoomDeviceList(RoomDeviceList.ALL_DEVICES_ROOM)
        }
        val comparableQuery = toComparable(query)
        val context = AndFHEMApplication.application?.applicationContext!!
        val allRoomsList = deviceListService.getAllRoomsDeviceList()
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