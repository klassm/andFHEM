package li.klass.fhem.fcm.history.data.message

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.Ignore
import li.klass.fhem.fcm.history.data.FcmHistoryEntity

@Entity(tableName = FcmHistoryMessageEntity.tableName)
class FcmHistoryMessageEntity : FcmHistoryEntity {
    @ColumnInfo(name = columnTitle)
    var title: String? = null

    @ColumnInfo(name = columnText)
    var text: String? = null

    @ColumnInfo(name = columnTicker)
    var ticker: String? = null

    constructor()

    @Ignore
    constructor(datetime: String, date: String, title: String, text: String, ticker: String)
            : super(datetime, date) {
        this.title = title
        this.ticker = ticker
        this.text = text
    }

    companion object {
        const val columnTitle: String = "TITLE"
        const val columnText: String = "TEXT"
        const val columnTicker: String = "TICKER"
        const val tableName: String = "fcm_history_messages"
    }
}