package li.klass.fhem.appindex

import android.app.IntentService
import android.content.Intent
import com.google.common.base.Joiner
import com.google.common.base.Optional
import com.google.firebase.appindexing.FirebaseAppIndex
import com.google.firebase.appindexing.Indexable
import com.google.firebase.appindexing.builders.Indexables
import li.klass.fhem.AndFHEMApplication
import li.klass.fhem.R
import li.klass.fhem.service.room.RoomListService
import org.slf4j.LoggerFactory
import javax.inject.Inject

class AppIndexIntentService : IntentService(AppIndexIntentService::class.java.name) {
    @Inject
    lateinit var roomListService: RoomListService

    override fun onCreate() {
        super.onCreate()
        val component = (application as AndFHEMApplication).daggerComponent
        component.inject(this)
    }

    override fun onHandleIntent(intent: Intent?) {
        LOGGER.info("onHandleIntent - updating index")

        val deviceList = roomListService.getAllRoomsDeviceList(Optional.absent<String>(), this)
        val roomNames = roomListService.getRoomNameList(Optional.absent<String>(), this)

        val roomTranslated = resources.getString(R.string.room)
        val roomsTranslated = resources.getString(R.string.rooms)

        val indexableRoomNames = roomNames.map { roomName ->
            Indexables.textDigitalDocumentBuilder()
                    .setName(roomTranslated + " " + roomName)
                    .setText(roomTranslated + " " + roomName)
                    .setUrl("fhem://room=" + roomName)
                    .setMetadata(Indexable.Metadata.Builder().setWorksOffline(true).setScore(100))
                    .build()
        }

        val indexableDevices = deviceList.allDevices.map { device ->
            val name = Joiner.on(" ").skipNulls().join(
                    device.getAliasOrName(),
                    if (device.getAliasOrName() != device.getName()) "(" + device.getName() + ")" else null,
                    roomsTranslated, device.getRoomConcatenated()
            )

            Indexables.textDigitalDocumentBuilder()
                    .setName(device.getAliasOrName())
                    .setText(name)
                    .setUrl("fhem://device=" + device.getName())
                    .setMetadata(Indexable.Metadata.Builder().setWorksOffline(true).setScore(100))
                    .build()
        }
        val indexables = indexableDevices + indexableRoomNames

        if (indexables.isNotEmpty()) {
            val index = FirebaseAppIndex.getInstance()
            index.removeAll()
            index.update(*(indexables.toTypedArray()))
        }
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(AppIndexIntentService::class.java)
    }
}
