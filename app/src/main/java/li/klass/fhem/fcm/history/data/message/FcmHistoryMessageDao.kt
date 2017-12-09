package li.klass.fhem.fcm.history.data.message

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import li.klass.fhem.fcm.history.data.FcmHistoryEntity

@Dao
interface FcmHistoryMessageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertMessage(message: FcmHistoryMessageEntity): Long

    @Query("SELECT * FROM ${FcmHistoryMessageEntity.tableName} WHERE ${FcmHistoryEntity.columnDate} = :date  ORDER BY ${FcmHistoryEntity.columnDatetime} DESC")
    fun getMessagesAt(date: String): List<FcmHistoryMessageEntity>

    @Query("SELECT * FROM ${FcmHistoryMessageEntity.tableName} WHERE ${FcmHistoryEntity.columnDatetime} <= :date")
    fun deleteWhereDataIsBefore(date: String): Int
}
