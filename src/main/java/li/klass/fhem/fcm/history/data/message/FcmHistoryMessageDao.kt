package li.klass.fhem.fcm.history.data.message

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query

@Dao
interface FcmHistoryMessageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertMessage(message: FcmHistoryMessageEntity): Long

    @Query("SELECT * FROM fcm_history_messages WHERE date = :date")
    fun getMessagesAt(date: String): List<FcmHistoryMessageEntity>

    @Query("SELECT * FROM fcm_history_messages WHERE datetime <= :date")
    fun deleteWhereDataIsBefore(date: String): Int
}
