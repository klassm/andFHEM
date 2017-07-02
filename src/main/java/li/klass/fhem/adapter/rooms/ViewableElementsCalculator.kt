package li.klass.fhem.adapter.rooms

import android.content.Context
import li.klass.fhem.domain.core.DeviceFunctionality
import li.klass.fhem.domain.core.FhemDevice
import li.klass.fhem.domain.core.RoomDeviceList
import li.klass.fhem.widget.deviceFunctionality.DeviceGroupHolder
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ViewableElementsCalculator @Inject constructor(
        private val deviceGroupHolder: DeviceGroupHolder
) {
    fun calculateElements(context: Context, roomDeviceList: RoomDeviceList): List<Element> {

        val visibleParents: List<String> = deviceGroupHolder.getVisible(context)
                .map { it.getCaptionText(context) }
                .toList()
        val invisibleParents = deviceGroupHolder.getInvisible(context)
                .map { it.getCaptionText(context) }
                .toList()
        val customParents = roomDeviceList.allDevices.flatMap { it.getInternalDeviceGroupOrGroupAttributes(context) as List<String> }.toSet()

        val groupComparator = GroupComparator(DeviceFunctionality.UNKNOWN.getCaptionText(context), visibleParents)
        val elementsInGroup = (visibleParents + customParents)
                .filter { it !in invisibleParents }
                .map { parent -> Pair(parent, roomDeviceList.getDevicesOfFunctionality(parent)) }
                .toMap()

        val groups = elementsInGroup.keys.sortedWith(groupComparator)
                .filter { elementsInGroup[it]!!.isNotEmpty() }

        return groups
                .flatMap { group ->
                    val devices = elementsInGroup[group]!!
                            .sortedBy { it.aliasOrName.toLowerCase() }
                            .map { Element.Device(it) }
                    listOf(Element.Group(group)) + devices
                }
    }

    sealed class Element {
        data class Device(val device: FhemDevice) : Element()
        data class Group(val group: String) : Element()
    }
}