package li.klass.fhem.adapter.rooms

import android.content.Context
import li.klass.fhem.constants.PreferenceKeys.SHOW_HIDDEN_DEVICES
import li.klass.fhem.domain.core.DeviceFunctionality
import li.klass.fhem.domain.core.FhemDevice
import li.klass.fhem.domain.core.RoomDeviceList
import li.klass.fhem.util.ApplicationProperties
import li.klass.fhem.widget.deviceFunctionality.DeviceGroupHolder
import javax.inject.Inject

// No singleton (to reread the ordering configuration regularly)
class ViewableElementsCalculator @Inject constructor(
        private val deviceGroupHolder: DeviceGroupHolder,
        private val applicationProperties: ApplicationProperties
) {
    fun calculateElements(context: Context, roomDeviceList: RoomDeviceList): List<Element> {

        val showHiddenDevices = applicationProperties.getBooleanSharedPreference(SHOW_HIDDEN_DEVICES, false, context)
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
                .map { parent ->
                    Pair(parent, roomDeviceList.getDevicesOfFunctionality(parent)
                            .filter { !it.isInRoom("hidden") || showHiddenDevices })
                }
                .toMap()

        val groups = elementsInGroup.keys.sortedWith(groupComparator)
                .filter { elementsInGroup[it]!!.isNotEmpty() }

        return groups
                .flatMap { group ->
                    val devices = elementsInGroup[group]!!
                            .sortedWith(FhemDevice.BY_NAME)
                            .map { Element.Device(it) }
                    listOf(Element.Group(group)) + devices
                }
    }

    sealed class Element {
        data class Device(val device: FhemDevice) : Element()
        data class Group(val group: String) : Element()
    }
}