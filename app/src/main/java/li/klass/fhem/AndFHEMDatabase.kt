package li.klass.fhem

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import li.klass.fhem.fcm.history.data.change.FcmHistoryChangeDao
import li.klass.fhem.fcm.history.data.change.FcmHistoryChangeEntity
import li.klass.fhem.fcm.history.data.message.FcmHistoryMessageDao
import li.klass.fhem.fcm.history.data.message.FcmHistoryMessageEntity

@Database(entities = [(FcmHistoryMessageEntity::class), (FcmHistoryChangeEntity::class)],
        version = AndFHEMDatabase.version)
abstract class AndFHEMDatabase : RoomDatabase() {
    abstract fun getFcmHistoryMessagesDao(): FcmHistoryMessageDao
    abstract fun getFcmHistoryUpdatesDao(): FcmHistoryChangeDao

    companion object {
        const val version = 2
    }
}