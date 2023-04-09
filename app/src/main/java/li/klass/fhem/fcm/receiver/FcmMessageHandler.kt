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

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import li.klass.fhem.activities.AndFHEMMainActivity
import li.klass.fhem.fcm.history.data.FcmHistoryService
import li.klass.fhem.fcm.receiver.data.FcmMessageData
import li.klass.fhem.util.NotificationUtil
import javax.inject.Inject

class FcmMessageHandler @Inject constructor(
        private val fcmHistoryService: FcmHistoryService
) {
    fun handleMessage(data: FcmMessageData, context: Context) {
        val notifyId = data.notifyId

        val openIntent = Intent(context, AndFHEMMainActivity::class.java)
        openIntent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        openIntent.putExtra("unique", "foobar://" + SystemClock.elapsedRealtime())
        val pendingIntent = PendingIntent.getActivity(
            context, notifyId, openIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        NotificationUtil.notify(context, notifyId, pendingIntent, data.title, data.text, data.ticker, data.shouldVibrate())
        fcmHistoryService.addMessage(FcmHistoryService.ReceivedMessage(
                contentTitle = data.title ?: "",
                contentText = data.text ?: "",
                tickerText = data.ticker ?: "",
                sentTime = data.sentTime
        ))
    }
}