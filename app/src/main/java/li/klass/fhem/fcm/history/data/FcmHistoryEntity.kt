package li.klass.fhem.fcm.history.data

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.PrimaryKey

abstract class FcmHistoryEntity() {
    constructor(datetime: String, date: String) : this() {
        this.datetime = datetime
        this.date = date
    }

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = columnId)
    var id: Long? = null

    @ColumnInfo(name = columnDatetime)
    var datetime: String? = null

    @ColumnInfo(name = columnDate)
    var date: String? = null

    companion object {
        const val columnId = "ID"
        const val columnDatetime = "DATETIME"
        const val columnDate = "DATE"
    }
}