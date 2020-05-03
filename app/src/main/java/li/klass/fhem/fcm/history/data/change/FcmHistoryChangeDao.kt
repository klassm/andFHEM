package li.klass.fhem.fcm.history.data.change

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import li.klass.fhem.fcm.history.data.FcmHistoryEntity

@Dao
interface FcmHistoryChangeDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insertChange(change: FcmHistoryChangeEntity): Long

    @Query("SELECT * FROM ${FcmHistoryChangeEntity.tableName} WHERE ${FcmHistoryEntity.columnDate} = :date ORDER BY ${FcmHistoryEntity.columnDatetime} DESC")
    fun getChangesAt(date: String): List<FcmHistoryChangeEntity>

    @Query("SELECT * FROM ${FcmHistoryChangeEntity.tableName} WHERE ${FcmHistoryEntity.columnDatetime} <= :date")
    fun deleteWhereDataIsBefore(date: String): Int
}