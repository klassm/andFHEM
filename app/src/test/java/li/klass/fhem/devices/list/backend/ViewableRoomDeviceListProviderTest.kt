package li.klass.fhem.devices.list.backend

import android.content.Context
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verify
import li.klass.fhem.domain.core.FhemDevice
import li.klass.fhem.domain.core.RoomDeviceList
import li.klass.fhem.testutil.MockRule
import li.klass.fhem.update.backend.fhemweb.FhemWebConfigurationService
import li.klass.fhem.update.backend.xmllist.DeviceNode
import li.klass.fhem.update.backend.xmllist.XmlListDevice
import org.assertj.core.api.Assertions.assertThat
import org.joda.time.DateTime
import org.junit.Rule
import org.junit.Test

class ViewableRoomDeviceListProviderTest {
    @MockK
    lateinit var viewableElementsCalculator: ViewableElementsCalculator

    @MockK
    lateinit var hiddenRoomsDeviceFilter: HiddenRoomsDeviceFilter

    @MockK
    lateinit var fhemWebConfigurationService: FhemWebConfigurationService

    @MockK
    lateinit var context: Context

    @InjectMockKs
    lateinit var viewableRoomDeviceListProvider: ViewableRoomDeviceListProvider

    private val resultElements = listOf(mockk<ViewableElementsCalculator.Element>())

    @Rule
    @JvmField
    var mockRule: MockRule = MockRule()

    @Test
    fun should_filter_out_hidden_groups_in_some_room() {
        val deviceList = RoomDeviceList("someName").apply {
            addDevice(FhemDevice(XmlListDevice("bla", attributes = mutableMapOf(
                    "group" to nodeOf("hiddengroup"),
                    "room" to nodeOf("someRoom"),
            ))))
        }
        val filteredRoom = RoomDeviceList("filteredGroups")
        every { fhemWebConfigurationService.filterHiddenGroupsFrom(deviceList) } returns filteredRoom
        every { viewableElementsCalculator.calculateElements(context, filteredRoom) } returns resultElements

        val result = viewableRoomDeviceListProvider.provideFor(context, deviceList)

        assertThat(result).isEqualTo(resultElements)
        verify(exactly = 0) { hiddenRoomsDeviceFilter.filterHiddenDevicesIfRequired(any()) }
    }

    @Test
    fun should_filter_out_hidden_rooms_in_the_all_room() {
        val deviceList = RoomDeviceList("roomContainingAllDevices").apply {
            addDevice(FhemDevice(XmlListDevice("bla", attributes = mutableMapOf(
                    "group" to nodeOf("hiddengroup"),
                    "room" to nodeOf("someRoom"),
            ))))
        }
        val filteredGroupsRoom = RoomDeviceList(RoomDeviceList.ALL_DEVICES_ROOM)
        val filteredHiddenRoom = RoomDeviceList("filteredHiddenDevices")
        every { fhemWebConfigurationService.filterHiddenGroupsFrom(deviceList) } returns filteredGroupsRoom
        every { hiddenRoomsDeviceFilter.filterHiddenDevicesIfRequired(filteredGroupsRoom) } returns filteredHiddenRoom
        every { viewableElementsCalculator.calculateElements(context, filteredHiddenRoom) } returns resultElements

        val result = viewableRoomDeviceListProvider.provideFor(context, deviceList)

        assertThat(result).isEqualTo(resultElements)
    }

    private fun nodeOf(content: String): DeviceNode =
            DeviceNode(DeviceNode.DeviceNodeType.ATTR, "", content, DateTime.now())

}