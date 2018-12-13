package li.klass.fhem

import androidx.room.Database
import androidx.room.RoomDatabase
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