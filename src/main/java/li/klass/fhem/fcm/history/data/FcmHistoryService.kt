package li.klass.fhem.fcm.history.data

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import li.klass.fhem.util.DateFormatUtil.ANDFHEM_DATE_FORMAT
import li.klass.fhem.util.DateTimeProvider
import org.joda.time.DateTime
import org.joda.time.LocalDate
import org.joda.time.LocalDateTime
import org.joda.time.format.DateTimeFormat
import org.json.JSONArray
import org.json.JSONObject
import org.slf4j.LoggerFactory
import javax.inject.Inject

class FcmHistoryService @Inject constructor(private val dateTimeProvider: DateTimeProvider) {
    fun addMessage(context: Context, message: ReceivedMessage) {
        val now = LocalDateTime.now()
        val values = ContentValues().apply {
            put(FcmMessagesContentProvider.columnDatetime, now.toString(dateFormat))
            put(FcmMessagesContentProvider.columnDate, ANDFHEM_DATE_FORMAT.print(now))
            put(FcmMessagesContentProvider.columnText, message.contentText)
            put(FcmMessagesContentProvider.columnTicker, message.tickerText)
            put(FcmMessagesContentProvider.columnTitle, message.contentTitle)
        }
        val uri = context.contentResolver.insert(FcmMessagesContentProvider.contentUri, values)
        logger.info("addMessage - with URI $uri")
    }

    fun getMessages(context: Context, localDate: LocalDate): List<SavedMessage> {
        val cursor = context.contentResolver.query(FcmMessagesContentProvider.contentUri, null,
                "${FcmMessagesContentProvider.columnDate} = ?", arrayOf(ANDFHEM_DATE_FORMAT.print(localDate)), null)

        val result = readMessagesCursor(cursor)
        cursor.close()
        return result
    }

    private fun readMessagesCursor(cursor: Cursor): List<SavedMessage> {
        val columnDatetime = cursor.getColumnIndex(FcmMessagesContentProvider.columnDatetime)
        val columnText = cursor.getColumnIndex(FcmMessagesContentProvider.columnText)
        val columnTicker = cursor.getColumnIndex(FcmMessagesContentProvider.columnTicker)
        val columnTitle = cursor.getColumnIndex(FcmMessagesContentProvider.columnTitle)


        return generateSequence { if (cursor.moveToNext()) cursor else null }
                .map {
                    SavedMessage(
                            time = DateTime.parse(cursor.getString(columnDatetime), dateFormat),
                            title = cursor.getString(columnTitle),
                            ticker = cursor.getString(columnTicker),
                            text = cursor.getString(columnText))
                }
                .toList()
    }

    fun addChanges(context: Context, device: String, changes: Map<String, String>) {
        val changesAsObjects = changes.entries.map { val o = JSONObject(); o.put(it.key, it.value); o }
        val changesAsString = JSONArray(changesAsObjects).toString()

        val now = LocalDateTime.now()
        val values = ContentValues().apply {
            put(FcmUpdatesContentProvider.columnDatetime, now.toString(dateFormat))
            put(FcmUpdatesContentProvider.columnDate, ANDFHEM_DATE_FORMAT.print(now))
            put(FcmUpdatesContentProvider.columnDevice, device)
            put(FcmUpdatesContentProvider.columnChanges, changesAsString)
        }
        val uri = context.contentResolver.insert(FcmUpdatesContentProvider.contentUri, values)
        logger.info("addMessage - with URI $uri")
    }


    fun getChanges(context: Context, localDate: LocalDate): List<SavedChange> {
        val cursor = context.contentResolver.query(FcmUpdatesContentProvider.contentUri, null,
                "${FcmUpdatesContentProvider.columnDate} = ?", arrayOf(ANDFHEM_DATE_FORMAT.print(localDate)), null)

        val result = readChangesCursor(cursor)
        cursor.close()
        return result
    }

    private fun readChangesCursor(cursor: Cursor): List<SavedChange> {
        val columnDatetime = cursor.getColumnIndex(FcmUpdatesContentProvider.columnDatetime)
        val columnDevice = cursor.getColumnIndex(FcmUpdatesContentProvider.columnDevice)
        val columnChanges = cursor.getColumnIndex(FcmUpdatesContentProvider.columnChanges)

        return generateSequence { if (cursor.moveToNext()) cursor else null }
                .map {
                    val changesAsJson = JSONArray(cursor.getString(columnChanges))
                    val changesAsPairs = (0 until changesAsJson.length())
                            .map { changesAsJson.get(it) }
                            .map { it as JSONObject }
                            .map {
                                val key = it.keys().next()
                                key to it.getString(key)
                            }

                    SavedChange(
                            time = DateTime.parse(cursor.getString(columnDatetime), dateFormat),
                            changes = changesAsPairs,
                            deviceName = cursor.getString(columnDevice))
                }
                .toList()
    }

    fun deleteContentOlderThan(days: Int, context: Context) {
        if (days <= 0) {
            return
        }

        try {
            val now = dateTimeProvider.now()
            val deleteUntil = dateFormat.print(now.minusDays(days))
            logger.info("deleteContentOlderThan - deleting content older than $days days < $deleteUntil")

            val deletesUpdates = context.contentResolver.delete(FcmUpdatesContentProvider.contentUri,
                    "${FcmUpdatesContentProvider.columnDatetime} < ?",
                    arrayOf(deleteUntil))
            logger.info("deleteContentOlderThan - deleted $deletesUpdates updates")

            val deletedMessages = context.contentResolver.delete(FcmUpdatesContentProvider.contentUri,
                    "${FcmMessagesContentProvider.columnDatetime} < ?",
                    arrayOf(deleteUntil))
            logger.info("deleteContentOlderThan - deleted $deletedMessages messages")
        } catch (e: Exception) {
            logger.error("deleteContentOlderThan - error during deletion", e)
        }
    }

    class ReceivedMessage(val contentTitle: String,
                          val contentText: String,
                          val tickerText: String)

    class SavedMessage(
            val time: DateTime,
            val title: String,
            val text: String,
            val ticker: String)

    class SavedChange(
            val time: DateTime,
            val deviceName: String,
            val changes: List<Pair<String, String>>
    )

    companion object {
        val logger = LoggerFactory.getLogger(FcmHistoryService::class.java)!!
        val dateFormat = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")
    }
}