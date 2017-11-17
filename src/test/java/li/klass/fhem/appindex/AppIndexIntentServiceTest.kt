package li.klass.fhem.appindex

import android.content.Intent
import com.google.firebase.appindexing.Indexable
import li.klass.fhem.domain.core.FhemDevice
import li.klass.fhem.domain.core.RoomDeviceList
import li.klass.fhem.update.backend.DeviceListService
import org.junit.Before
import org.junit.Test
import org.mockito.BDDMockito.given
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations


class AppIndexIntentServiceTest {
    @InjectMocks
    lateinit var appIndexIntentService: AppIndexIntentService

    @Mock
    lateinit var indexableCreator: IndexableCreator
    @Mock
    lateinit var firebaseIndexWrapper: FirebaseIndexWrapper
    @Mock
    lateinit var deviceListService: DeviceListService

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
    }

    @Test
    fun should_update_index() {
        // given
        val indexableRoomA = mock(Indexable::class.java)
        val indexableRoomB = mock(Indexable::class.java)
        val indexableRoomC = mock(Indexable::class.java)
        given(deviceListService.getRoomNameList())
                .willReturn(setOf("a", "b", "c"))
        given(indexableCreator.indexableFor(appIndexIntentService, "a")).willReturn(indexableRoomA)
        given(indexableCreator.indexableFor(appIndexIntentService, "b")).willReturn(indexableRoomB)
        given(indexableCreator.indexableFor(appIndexIntentService, "c")).willReturn(indexableRoomC)

        val device1 = mock(FhemDevice::class.java)
        val device2 = mock(FhemDevice::class.java)
        val allRoomDeviceList = mock(RoomDeviceList::class.java)
        val indexableDevice1 = mock(Indexable::class.java)
        val indexableDevice2 = mock(Indexable::class.java)
        given(allRoomDeviceList.allDevices).willReturn(setOf(device1, device2))
        given(deviceListService.getAllRoomsDeviceList())
                .willReturn(allRoomDeviceList)
        given(indexableCreator.indexableFor(appIndexIntentService, device1)).willReturn(indexableDevice1)
        given(indexableCreator.indexableFor(appIndexIntentService, device2)).willReturn(indexableDevice2)

        // when
        appIndexIntentService.onHandleIntent(mock(Intent::class.java))

        // then
        verify(firebaseIndexWrapper).update(listOf(indexableRoomA, indexableRoomB, indexableRoomC, indexableDevice1, indexableDevice2))
    }
}