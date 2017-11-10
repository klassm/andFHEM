package li.klass.fhem.fcm.history.data.change

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query

@Dao
interface FcmHistoryChangeDao {
    @Insert(onConflict = OnConflictStrategy.FAIL)
    fun insertChange(change: FcmHistoryChangeEntity): Long

    @Query("SELECT * FROM fcm_history_changes WHERE date = :date")
    fun getChangesAt(date: String): List<FcmHistoryChangeEntity>

    @Query("SELECT * FROM fcm_history_changes WHERE datetime <= :date")
    fun deleteWhereDataIsBefore(date: String): Int
}