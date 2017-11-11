/*
 * AndFHEM - Open Source Android application to control a FHEM home automation
 * server.
 *
 * Copyright (c) 2011, Matthias Klass or third-party contributors as
 * indicated by the @author tags or express copyright attribution
 * statements applied by the authors.  All third-party contributions are
 * distributed under license by Red Hat Inc.
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU GENERAL PUBLIC LICENSE, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU GENERAL PUBLIC LICENSE
 * for more details.
 *
 * You should have received a copy of the GNU GENERAL PUBLIC LICENSE
 * along with this distribution; if not, write to:
 *   Free Software Foundation, Inc.
 *   51 Franklin Street, Fifth Floor
 *   Boston, MA  02110-1301  USA
 */

package li.klass.fhem.fcm

import android.app.PendingIntent
import android.content.Intent
import android.os.SystemClock
import com.google.common.base.Optional
import com.google.common.base.Strings
import com.google.common.collect.ImmutableSet
import com.google.common.collect.Maps.newHashMap
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import li.klass.fhem.AndFHEMApplication
import li.klass.fhem.activities.AndFHEMMainActivity
import li.klass.fhem.domain.core.FhemDevice
import li.klass.fhem.fcm.history.data.FcmHistoryService
import li.klass.fhem.room.list.backend.RoomListService
import li.klass.fhem.util.ApplicationProperties
import li.klass.fhem.util.NotificationUtil
import li.klass.fhem.util.Tasker
import org.apache.commons.codec.binary.Hex
import org.slf4j.LoggerFactory
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject

class FcmIntentService : FirebaseMessagingService() {

    @Inject
    lateinit var applicationProperties: ApplicationProperties

    @Inject
    lateinit var roomListService: RoomListService

    @Inject
    lateinit var fcmHistoryService: FcmHistoryService

    override fun onCreate() {
        super.onCreate()
        (application as AndFHEMApplication).daggerComponent.inject(this)
    }

    override fun onMessageReceived(message: RemoteMessage?) {
        super.onMessageReceived(message)

        message ?: return

        val data = message.data?.toMap() ?: return

        if (!data.containsKey("type") || !data.containsKey("source")) {
            LOG.info("onMessage - received GCM message, but doesn't fit required fields")
            return
        }

        val decrypted = decrypt(data)

        val type = decrypted["type"]
        if ("message".equals(type!!, ignoreCase = true)) {
            handleMessage(decrypted)
        } else if ("notify".equals(type, ignoreCase = true) || Strings.isNullOrEmpty(type)) {
            handleNotify(decrypted)
        } else {
            LOG.error("onMessage - unknown type: {}", type)
        }
    }

    private fun decrypt(data: Map<String, String>): Map<String, String> {
        if (!data.containsKey("gcmDeviceName")) {
            return data
        }

        val device = roomListService.getDeviceForName<FhemDevice>(data["gcmDeviceName"], Optional.absent<String>(), this)
        if (!device.isPresent) {
            return data
        }

        val cryptKey = device.get().xmlListDevice.getAttribute("cryptKey")
        if (!cryptKey.isPresent) {
            return data
        }

        return decrypt(data, cryptKey.get())
    }

    private fun decrypt(data: Map<String, String>, cryptKey: String): Map<String, String> {
        val cipherOptional = cipherFor(cryptKey)
        if (!cipherOptional.isPresent) {
            return data
        }

        val cipher = cipherOptional.get()
        return data.entries
                .map {
                    if (DECRYPT_KEYS.contains(it.key)) {
                        it.key to decrypt(cipher, it.value)
                    } else it.key to it.value
                }.toMap()
    }

    private fun decrypt(cipher: Cipher, value: String): String {
        try {
            val hexBytes = Hex.decodeHex(value.toCharArray())
            return String(cipher.doFinal(hexBytes))
        } catch (e: Exception) {
            LOG.error("decrypt($value)", e)
            return value
        }

    }

    private fun cipherFor(key: String): Optional<Cipher> {
        try {
            val keyBytes = key.toByteArray()
            val skey = SecretKeySpec(keyBytes, "AES")
            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            val ivSpec = IvParameterSpec(keyBytes)
            cipher.init(Cipher.DECRYPT_MODE, skey, ivSpec)
            return Optional.of(cipher)
        } catch (e: Exception) {
            LOG.error("cipherFor - cannot create cipher", e)
            return Optional.absent<Cipher>()
        }

    }

    private fun handleMessage(data: Map<String, String>) {
        var notifyId = 1
        try {
            if (data.containsKey("notifyId")) {
                notifyId = Integer.valueOf(data["notifyId"])!!
            }
        } catch (e: Exception) {
            LOG.error("handleMessage - invalid notify id: {}", data["notifyId"])
        }

        val openIntent = Intent(this, AndFHEMMainActivity::class.java)
        openIntent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        openIntent.putExtra("unique", "foobar://" + SystemClock.elapsedRealtime())
        val pendingIntent = PendingIntent.getActivity(this, notifyId, openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT)

        val title = data["contentTitle"]
        val text = data["contentText"]
        val ticker = data["tickerText"]

        NotificationUtil.notify(this, notifyId, pendingIntent, title, text, ticker, shouldVibrate(data))
        fcmHistoryService.addMessage(FcmHistoryService.ReceivedMessage(
                title ?: "", text ?: "", ticker ?: ""
        ))
    }

    private fun handleNotify(data: Map<String, String>) {
        if (!data.containsKey("changes")) return

        val deviceName = data["deviceName"] ?: return
        val changesText = data["changes"] ?: return

        val changes = extractChanges(deviceName, changesText)
        fcmHistoryService.addChanges(deviceName, changes)
        roomListService.parseReceivedDeviceStateMap(deviceName, changes, shouldVibrate(data), this)
    }

    fun extractChanges(deviceName: String, changesText: String): Map<String, String> {
        val changes = changesText.split("<\\|>".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

        val changeMap = newHashMap<String, String>()
        for (change in changes) {
            val parts = change.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            if (parts.size < 2) continue

            val key: String
            val value: String
            if (parts.size > 2) {
                key = "state"
                value = change
            } else {
                key = parts[0].trim { it <= ' ' }
                value = parts[1].trim { it <= ' ' }
            }

            Tasker.sendTaskerNotifyIntent(this, deviceName, key, value)

            changeMap.put(key, value)
        }
        return changeMap
    }

    private fun shouldVibrate(data: Map<String, String>): Boolean =
            data.containsKey("vibrate") && "true".equals(data["vibrate"]!!, ignoreCase = true)

    companion object {
        private val LOG = LoggerFactory.getLogger(FcmIntentService::class.java)

        private val DECRYPT_KEYS = ImmutableSet.of("type", "notifyId", "changes", "deviceName", "tickerText", "contentText", "contentTitle")
    }
}
