package li.klass.fhem.appindex

import android.content.Context
import com.google.common.base.Joiner
import com.google.firebase.appindexing.Indexable
import com.google.firebase.appindexing.builders.Indexables
import li.klass.fhem.R
import li.klass.fhem.domain.core.FhemDevice

class IndexableCreator {
    fun indexableFor(context: Context, roomName: String): Indexable {
        val roomTranslated = context.resources.getString(R.string.room)
        val content = roomTranslated + " " + roomName
        return indexableFor(content, content, "fhem://room=" + roomName)
    }

    fun indexableFor(context: Context, device: FhemDevice): Indexable {
        val roomsTranslated = context.resources.getString(R.string.rooms)
        val name = Joiner.on(" ").skipNulls().join(
                device.aliasOrName,
                if (device.aliasOrName != device.name) "(" + device.name + ")" else null,
                roomsTranslated, device.roomConcatenated
        )

        return indexableFor(name, device.aliasOrName, "fhem://device=" + device.name)
    }

    private fun indexableFor(text: String, name: String, url: String): Indexable {
        return Indexables.textDigitalDocumentBuilder()
                .setName(name)
                .setText(text)
                .setUrl(url)
                .setMetadata(Indexable.Metadata.Builder().setWorksOffline(true).setScore(100))
                .build()
    }
}
