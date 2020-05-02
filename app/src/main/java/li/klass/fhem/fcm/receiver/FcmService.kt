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

package li.klass.fhem.fcm.receiver

import android.content.Context
import li.klass.fhem.connection.backend.ConnectionService
import li.klass.fhem.domain.core.FhemDevice
import li.klass.fhem.fcm.receiver.data.FcmMessageData
import li.klass.fhem.fcm.receiver.data.FcmNotifyData
import li.klass.fhem.update.backend.DeviceListService
import org.joda.time.DateTime
import org.slf4j.LoggerFactory
import javax.inject.Inject

class FcmService @Inject constructor(
        private val fcmNotifyHandler: FcmNotifyHandler,
        private val fcmMessageHandler: FcmMessageHandler,
        private val connectionService: ConnectionService,
        private val deviceListService: DeviceListService,
        private val fcmDecryptor: FcmDecryptor
) {

    fun onMessageReceived(data: Map<String, String>, sentTime: DateTime, context: Context) {
        if (!data.containsKey("type") || !data.containsKey("source")) {
            LOG.info("onMessage - received GCM message, but doesn't fit required fields")
            return
        }

        val gcmDeviceConnection = data["gcmDeviceName"]
                ?.let { findFirstConnectionContaining(it) }
        if (gcmDeviceConnection == null) {
            LOG.error("onMessage - cannot find gcm gcmDevice (${data["gcmDeviceName"]})")
            return
        }
        val (connection, gcmDevice) = gcmDeviceConnection
        val decrypted = gcmDeviceConnection.let { fcmDecryptor.decrypt(data, gcmDevice) }

        val type = decrypted["type"]
        if ("message".equals(type!!, ignoreCase = true)) {
            fcmMessageHandler.handleMessage(FcmMessageData(decrypted, sentTime), context)
        } else if ("notify".equals(type, ignoreCase = true) || type.isNullOrEmpty()) {
            fcmNotifyHandler.handleNotify(FcmNotifyData(decrypted, sentTime, connection), context)
        } else {
            LOG.error("onMessage - unknown type: {}", type)
        }
    }


    private fun findFirstConnectionContaining(deviceName: String): Pair<String, FhemDevice>? {
        return connectionService.listAll().asSequence()
                .map { connection ->
                    val device = deviceListService.getDeviceForName(deviceName, connection.id)
                    if (device == null) null else connection.id to device
                }
                .filterNotNull()
                .firstOrNull()
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(FcmService::class.java)
    }
}