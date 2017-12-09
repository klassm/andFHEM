package li.klass.fhem.fcm.history.data.change

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.Ignore
import li.klass.fhem.fcm.history.data.FcmHistoryEntity

@Entity(tableName = FcmHistoryChangeEntity.tableName)
class FcmHistoryChangeEntity : FcmHistoryEntity {

    @ColumnInfo(name = columnDevice)
    var device: String? = null

    @ColumnInfo(name = columnChanges)
    var changes: String? = null

    constructor() : super()

    @Ignore
    constructor(datetime: String, date: String, device: String?, changes: String?) : super(datetime, date) {
        this.device = device
        this.changes = changes
    }

    companion object {
        const val columnDevice: String = "DEVICE"
        const val columnChanges: String = "CHANGES"
        const val tableName: String = "fcm_history_changes"
    }
}