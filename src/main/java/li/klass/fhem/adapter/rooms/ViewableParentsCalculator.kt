package li.klass.fhem.adapter.rooms

import android.content.Context
import li.klass.fhem.domain.core.DeviceFunctionality
import li.klass.fhem.domain.core.RoomDeviceList
import li.klass.fhem.widget.deviceFunctionality.DeviceGroupHolder
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ViewableParentsCalculator @Inject constructor(
        private val deviceGroupHolder: DeviceGroupHolder
) {
    fun calculateParents(context: Context, roomDeviceList: RoomDeviceList): List<String> {
        val visibleParents: List<String> = deviceGroupHolder.getVisible(context)
                .map { it.getCaptionText(context) }
                .toList()
        val invisibleParents = deviceGroupHolder.getInvisible(context)
                .map { it.getCaptionText(context) }
                .toList()
        val customParents = roomDeviceList.allDevices.flatMap { it.getInternalDeviceGroupOrGroupAttributes(context) as List<String> }.toSet()

        val groupComparator = GroupComparator(DeviceFunctionality.UNKNOWN.getCaptionText(context), visibleParents)
        return (visibleParents + customParents)
                .filter { it !in invisibleParents}
                .filter { !roomDeviceList.getDevicesOfFunctionality(it).isEmpty() }
                .distinct()
                .sortedWith(groupComparator)

    }
}