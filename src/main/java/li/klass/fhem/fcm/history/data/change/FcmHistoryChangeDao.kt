package li.klass.fhem.fcm.history.data.change

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import li.klass.fhem.fcm.history.data.FcmHistoryEntity

@Dao
interface FcmHistoryChangeDao {
    @Insert(onConflict = OnConflictStrategy.FAIL)
    fun insertChange(change: FcmHistoryChangeEntity): Long

    @Query("SELECT * FROM ${FcmHistoryChangeEntity.tableName} WHERE ${FcmHistoryEntity.columnDate} = :date")
    fun getChangesAt(date: String): List<FcmHistoryChangeEntity>

    @Query("SELECT * FROM ${FcmHistoryChangeEntity.tableName} WHERE ${FcmHistoryEntity.columnDatetime} <= :date")
    fun deleteWhereDataIsBefore(date: String): Int
}