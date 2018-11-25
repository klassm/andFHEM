package li.klass.fhem.fcm.history.data

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import li.klass.fhem.fcm.history.data.change.FcmHistoryChangeDao
import li.klass.fhem.fcm.history.data.change.FcmHistoryChangeEntity
import li.klass.fhem.fcm.history.data.message.FcmHistoryMessageDao
import li.klass.fhem.fcm.history.data.message.FcmHistoryMessageEntity
import li.klass.fhem.util.DateTimeProvider
import org.joda.time.DateTime
import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat
import org.json.JSONArray
import org.json.JSONObject
import org.slf4j.LoggerFactory
import javax.inject.Inject

class FcmHistoryService @Inject constructor(private val dateTimeProvider: DateTimeProvider,
                                            private val fcmHistoryMessageDao: FcmHistoryMessageDao,
                                            private val fcmHistoryChangeDao: FcmHistoryChangeDao) {
    fun addMessage(message: ReceivedMessage) {
        val id = fcmHistoryMessageDao.insertMessage(FcmHistoryMessageEntity(
                datetime = message.sentTime.toString(datetimeFormat),
                date = dateFormat.print(message.sentTime),
                text = message.contentText,
                ticker = message.tickerText,
                title = message.contentTitle,
                saveDatetime = DateTime.now().toString(datetimeFormat)
        ))
        logger.info("addMessage - success, id=$id")
    }

    fun getMessages(localDate: LocalDate): List<SavedMessage> {
        return fcmHistoryMessageDao.getMessagesAt(dateFormat.print(localDate))
                .map {
                    SavedMessage(
                            time = DateTime.parse(it.datetime, datetimeFormat),
                            title = it.title ?: "",
                            ticker = it.ticker ?: "",
                            text = it.text ?: "",
                            receiveTime = it.saveDatetime?.let { DateTime.parse(it, datetimeFormat) }
                    )
                }
    }

    fun addChanges(device: String, changes: Map<String, String>, sentTime: DateTime) {
        val changesAsObjects = changes.entries.map { val o = JSONObject(); o.put(it.key, it.value); o }
        val changesAsString = JSONArray(changesAsObjects).toString()

        val id = fcmHistoryChangeDao.insertChange(FcmHistoryChangeEntity(
                datetime = sentTime.toString(datetimeFormat),
                date = dateFormat.print(sentTime),
                device = device,
                changes = changesAsString,
                saveDatetime = DateTime.now().toString(datetimeFormat)
        ))
        logger.info("addChange - success, id=$id")
    }

    fun getChanges(localDate: LocalDate): List<SavedChange> {
        return fcmHistoryChangeDao.getChangesAt(dateFormat.print(localDate))
                .map {
                    val changes = it.changes ?: ""
                    val changesAsJson = if (changes.isEmpty()) JSONArray() else JSONArray(it.changes
                            ?: "")
                    val changesAsPairs = (0 until changesAsJson.length())
                            .map { changesAsJson.get(it) }
                            .map { it as JSONObject }
                            .map {
                                val key = it.keys().next()
                                key to it.getString(key)
                            }

                    SavedChange(
                            time = DateTime.parse(it.datetime, datetimeFormat),
                            changes = changesAsPairs,
                            deviceName = it.device ?: "",
                            receiveTime = it.saveDatetime?.let { DateTime.parse(it, datetimeFormat) }
                    )
                }
    }

    fun deleteContentOlderThan(days: Int) {
        if (days <= 0) {
            logger.info("deleteContentOlderThan - days are $days <= 0, doing nothing")
            return
        }

        GlobalScope.launch {
            try {
                val now = dateTimeProvider.now()
                val deleteUntil = datetimeFormat.print(now.minusDays(days))
                logger.info("deleteContentOlderThan - deleting content older than $days days < $deleteUntil")

                val deletesUpdates = fcmHistoryChangeDao.deleteWhereDataIsBefore(deleteUntil)
                logger.info("deleteContentOlderThan - deleted $deletesUpdates updates")

                val deletesMessages = fcmHistoryMessageDao.deleteWhereDataIsBefore(deleteUntil)
                logger.info("deleteContentOlderThan - deleted $deletesMessages messages")
            } catch (e: Exception) {
                logger.error("deleteContentOlderThan - error during deletion", e)
            }
        }
    }

    class ReceivedMessage(val contentTitle: String,
                          val contentText: String,
                          val tickerText: String,
                          val sentTime: DateTime)

    class SavedMessage(
            val time: DateTime,
            val title: String,
            val text: String,
            val ticker: String,
            val receiveTime: DateTime?)

    class SavedChange(
            val time: DateTime,
            val deviceName: String,
            val changes: List<Pair<String, String>>,
            val receiveTime: DateTime?
    )

    companion object {
        val logger = LoggerFactory.getLogger(FcmHistoryService::class.java)!!
        val datetimeFormat = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")!!
        val dateFormat = DateTimeFormat.forPattern("yyyy-MM-dd")!!
    }
}