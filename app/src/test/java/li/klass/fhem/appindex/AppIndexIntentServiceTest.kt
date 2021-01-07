package li.klass.fhem.appindex

import android.content.Intent
import com.google.firebase.appindexing.Indexable
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verify
import li.klass.fhem.domain.core.FhemDevice
import li.klass.fhem.domain.core.RoomDeviceList
import li.klass.fhem.update.backend.DeviceListService
import org.junit.Before
import org.junit.Test


class AppIndexIntentServiceTest {
    @InjectMockKs
    lateinit var appIndexIntentService: AppIndexIntentService

    @MockK
    lateinit var indexableCreator: IndexableCreator

    @MockK
    lateinit var firebaseIndexWrapper: FirebaseIndexWrapper

    @MockK
    lateinit var deviceListService: DeviceListService

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun should_update_index() {
        // given
        val indexableRoomA: Indexable = mockk()
        val indexableRoomB: Indexable = mockk()
        val indexableRoomC: Indexable = mockk()
        every { deviceListService.getRoomNameList() } returns setOf("a", "b", "c")
        every { indexableCreator.indexableFor(appIndexIntentService, "a") } returns indexableRoomA
        every { indexableCreator.indexableFor(appIndexIntentService, "b") } returns indexableRoomB
        every { indexableCreator.indexableFor(appIndexIntentService, "c") } returns indexableRoomC

        val device1: FhemDevice = mockk()
        val device2: FhemDevice = mockk()
        val allRoomDeviceList: RoomDeviceList = mockk()
        val indexableDevice1: Indexable = mockk()
        val indexableDevice2: Indexable = mockk()
        every { allRoomDeviceList.allDevices } returns setOf(device1, device2)
        every { deviceListService.getAllRoomsDeviceList() } returns allRoomDeviceList
        every { indexableCreator.indexableFor(appIndexIntentService, device1) } returns indexableDevice1
        every { indexableCreator.indexableFor(appIndexIntentService, device2) } returns indexableDevice2
        every { firebaseIndexWrapper.update(any()) } returns Unit

        // when
        appIndexIntentService.onHandleIntent(Intent())

        // then
        verify { firebaseIndexWrapper.update(listOf(indexableRoomA, indexableRoomB, indexableRoomC, indexableDevice1, indexableDevice2)) }
    }
}