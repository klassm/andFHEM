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
import com.google.common.base.Strings
import li.klass.fhem.fcm.receiver.data.FcmMessageData
import li.klass.fhem.fcm.receiver.data.FcmNotifyData
import org.slf4j.LoggerFactory
import javax.inject.Inject

class FcmService @Inject constructor(
        private val fcmNotifyHandler: FcmNotifyHandler,
        private val fcmMessageHandler: FcmMessageHandler,
        private val fcmDecryptor: FcmDecryptor
) {

    fun onMessageReceived(data: Map<String, String>, context: Context) {
        if (!data.containsKey("type") || !data.containsKey("source")) {
            LOG.info("onMessage - received GCM message, but doesn't fit required fields")
            return
        }

        val decrypted = fcmDecryptor.decrypt(data, context)

        val type = decrypted["type"]
        if ("message".equals(type!!, ignoreCase = true)) {
            fcmMessageHandler.handleMessage(FcmMessageData(decrypted), context)
        } else if ("notify".equals(type, ignoreCase = true) || Strings.isNullOrEmpty(type)) {
            fcmNotifyHandler.handleNotify(FcmNotifyData(decrypted), context)
        } else {
            LOG.error("onMessage - unknown type: {}", type)
        }
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(FcmService::class.java)
    }
}