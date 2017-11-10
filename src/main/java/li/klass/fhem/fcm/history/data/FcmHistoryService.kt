package li.klass.fhem.fcm.history.data

import li.klass.fhem.fcm.history.data.change.FcmHistoryChangeDao
import li.klass.fhem.fcm.history.data.change.FcmHistoryChangeEntity
import li.klass.fhem.fcm.history.data.message.FcmHistoryMessageDao
import li.klass.fhem.fcm.history.data.message.FcmHistoryMessageEntity
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

class FcmHistoryService @Inject constructor(private val dateTimeProvider: DateTimeProvider,
                                            private val fcmHistoryMessageDao: FcmHistoryMessageDao,
                                            private val fcmHistoryChangeDao: FcmHistoryChangeDao) {
    fun addMessage(message: ReceivedMessage) {
        val now = LocalDateTime.now()
        val id = fcmHistoryMessageDao.insertMessage(FcmHistoryMessageEntity(
                datetime = now.toString(dateFormat),
                date = ANDFHEM_DATE_FORMAT.print(now),
                text = message.contentText,
                ticker = message.tickerText,
                title = message.contentTitle
        ))
        logger.info("addMessage - success, id=$id")
    }

    fun getMessages(localDate: LocalDate): List<SavedMessage> {
        return fcmHistoryMessageDao.getMessagesAt(ANDFHEM_DATE_FORMAT.print(localDate))
                .map {
                    SavedMessage(
                            time = DateTime.parse(it.datetime, dateFormat),
                            title = it.title ?: "",
                            ticker = it.ticker ?: "",
                            text = it.text ?: ""
                    )
                }
    }

    fun addChanges(device: String, changes: Map<String, String>) {
        val changesAsObjects = changes.entries.map { val o = JSONObject(); o.put(it.key, it.value); o }
        val changesAsString = JSONArray(changesAsObjects).toString()

        val now = LocalDateTime.now()

        val id = fcmHistoryChangeDao.insertChange(FcmHistoryChangeEntity(
                datetime = now.toString(dateFormat),
                date = ANDFHEM_DATE_FORMAT.print(now),
                device = device,
                changes = changesAsString
        ))
        logger.info("addChange - success, id=$id")
    }

    fun getChanges(localDate: LocalDate): List<SavedChange> {
        return fcmHistoryChangeDao.getChangesAt(ANDFHEM_DATE_FORMAT.print(localDate))
                .map {
                    val changes = it.changes ?: ""
                    val changesAsJson = if (changes.isEmpty()) JSONArray() else JSONArray(it.changes ?: "")
                    val changesAsPairs = (0 until changesAsJson.length())
                            .map { changesAsJson.get(it) }
                            .map { it as JSONObject }
                            .map {
                                val key = it.keys().next()
                                key to it.getString(key)
                            }

                    SavedChange(
                            time = DateTime.parse(it.datetime, dateFormat),
                            changes = changesAsPairs,
                            deviceName = it.device ?: ""
                    )
                }
    }

    fun deleteContentOlderThan(days: Int) {
        if (days <= 0) {
            logger.info("deleteContentOlderThan - days are $days <= 0, doing nothing")
            return
        }

        try {
            val now = dateTimeProvider.now()
            val deleteUntil = dateFormat.print(now.minusDays(days))
            logger.info("deleteContentOlderThan - deleting content older than $days days < $deleteUntil")

            val deletesUpdates = fcmHistoryChangeDao.deleteWhereDataIsBefore(deleteUntil)
            logger.info("deleteContentOlderThan - deleted $deletesUpdates updates")

            val deletesMessages = fcmHistoryMessageDao.deleteWhereDataIsBefore(deleteUntil)
            logger.info("deleteContentOlderThan - deleted $deletesMessages messages")
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
        val dateFormat = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")!!
    }
}