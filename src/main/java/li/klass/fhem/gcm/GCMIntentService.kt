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

package li.klass.fhem.gcm

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import com.google.android.gcm.GCMBaseIntentService
import com.google.android.gcm.GCMRegistrar
import com.google.common.base.Optional
import com.google.common.base.Strings
import com.google.common.collect.ImmutableSet
import com.google.common.collect.Maps.newHashMap
import li.klass.fhem.AndFHEMApplication
import li.klass.fhem.activities.AndFHEMMainActivity
import li.klass.fhem.constants.Actions
import li.klass.fhem.constants.BundleExtraKeys
import li.klass.fhem.constants.PreferenceKeys.GCM_PROJECT_ID
import li.klass.fhem.constants.PreferenceKeys.GCM_REGISTRATION_ID
import li.klass.fhem.domain.core.FhemDevice
import li.klass.fhem.service.room.RoomListService
import li.klass.fhem.util.ApplicationProperties
import li.klass.fhem.util.NotificationUtil
import li.klass.fhem.util.Tasker
import org.apache.commons.codec.binary.Hex
import org.slf4j.LoggerFactory
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject

class GCMIntentService : GCMBaseIntentService() {

    @Inject
    lateinit var applicationProperties: ApplicationProperties

    @Inject
    lateinit var roomListService: RoomListService

    override fun onCreate() {
        super.onCreate()
        (application as AndFHEMApplication).daggerComponent.inject(this)
    }

    override fun onRegistered(context: Context, registrationId: String) {
        applicationProperties.setSharedPreference(GCM_REGISTRATION_ID, registrationId, context)
        LOG.info("onRegistered - device registered with regId {}", registrationId)

        val intent = Intent(Actions.GCM_REGISTERED)
        intent.putExtra(BundleExtraKeys.GCM_REGISTRATION_ID, registrationId)
        sendBroadcast(intent)
    }

    override fun onUnregistered(context: Context, registrationId: String) {
        LOG.info("onUnregistered - device unregistered")
        if (GCMRegistrar.isRegisteredOnServer(context)) {
            GCMRegistrar.unregister(this)
        } else {
            LOG.info("onUnregistered - Ignoring unregister callback")
        }
    }

    override fun onMessage(context: Context, intent: Intent) {
        var extras: Bundle? = intent.extras
        extras ?: return

        LOG.info(GCMBaseIntentService.TAG, "onMessage - received GCM message with content: {}", extras)

        if (!extras.containsKey("type") || !extras.containsKey("source")) {
            LOG.info(GCMBaseIntentService.TAG, "onMessage - received GCM message, but doesn't fit required fields")
            return
        }

        extras = decrypt(extras)

        val type = extras.getString("type")
        if ("message".equals(type!!, ignoreCase = true)) {
            handleMessage(extras)
        } else if ("notify".equals(type, ignoreCase = true) || Strings.isNullOrEmpty(type)) {
            handleNotify(extras)
        } else {
            LOG.error("onMessage - unknown type: {}", type)
        }
    }

    private fun decrypt(extras: Bundle): Bundle {
        if (!extras.containsKey("gcmDeviceName")) {
            return extras
        }

        val device = roomListService.getDeviceForName<FhemDevice>(extras.getString("gcmDeviceName"), Optional.absent<String>(), this)
        if (!device.isPresent) {
            return extras
        }

        val cryptKey = device.get().xmlListDevice.getAttribute("cryptKey")
        if (!cryptKey.isPresent) {
            return extras
        }

        return decrypt(extras, cryptKey.get())
    }

    private fun decrypt(extras: Bundle, cryptKey: String): Bundle {
        val cipherOptional = cipherFor(cryptKey)
        if (!cipherOptional.isPresent) {
            return extras
        }

        val cipher = cipherOptional.get()
        val newBundle = Bundle()
        newBundle.putAll(extras)
        for (key in extras.keySet()) {
            if (DECRYPT_KEYS.contains(key)) {
                newBundle.putString(key, decrypt(cipher, extras.getString(key)))
            }
        }

        return newBundle
    }

    private fun decrypt(cipher: Cipher, value: String): String {
        try {
            val hexBytes = Hex.decodeHex(value.toCharArray())
            return String(cipher.doFinal(hexBytes))
        } catch (e: Exception) {
            e.printStackTrace()
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

    private fun handleMessage(extras: Bundle) {
        var notifyId = 1
        try {
            if (extras.containsKey("notifyId")) {
                notifyId = Integer.valueOf(extras.getString("notifyId"))!!
            }
        } catch (e: Exception) {
            LOG.error("handleMessage - invalid notify id: {}", extras.getString("notifyId"))
        }

        val openIntent = Intent(this, AndFHEMMainActivity::class.java)
        openIntent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        openIntent.putExtra("unique", "foobar://" + SystemClock.elapsedRealtime())
        val pendingIntent = PendingIntent.getActivity(this, notifyId, openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT)

        NotificationUtil.notify(this, notifyId, pendingIntent, extras.getString("contentTitle"),
                extras.getString("contentText"), extras.getString("tickerText"),
                shouldVibrate(extras))
    }

    private fun handleNotify(extras: Bundle) {
        if (!extras.containsKey("changes")) return

        val deviceName = extras.getString("deviceName")

        val changesText = extras.getString("changes") ?: return

        roomListService.parseReceivedDeviceStateMap(deviceName, extractChanges(deviceName, changesText), shouldVibrate(extras), this)
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

    private fun shouldVibrate(extras: Bundle): Boolean {
        return extras.containsKey("vibrate") && "true".equals(extras.getString("vibrate")!!, ignoreCase = true)
    }

    override fun onDeletedMessages(context: Context?, total: Int) {
        LOG.info("onDeletedMessages - Received deleted messages notification")
    }

    public override fun onError(context: Context, errorId: String) {
        LOG.info("onError - received error: " + errorId)
    }

    override fun onRecoverableError(context: Context?, errorId: String?): Boolean {
        LOG.info("onRecoverableError - errorId={}", errorId)
        return super.onRecoverableError(context, errorId)
    }

    override fun getSenderIds(context: Context?): Array<String> {
        val projectId = applicationProperties.getStringSharedPreference(GCM_PROJECT_ID, null, context)
        if (Strings.isNullOrEmpty(projectId)) {
            return arrayOf()
        }
        return arrayOf(projectId)
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(GCMIntentService::class.java)

        private val DECRYPT_KEYS = ImmutableSet.of("type", "notifyId", "changes", "deviceName", "tickerText", "contentText", "contentTitle")
    }
}
