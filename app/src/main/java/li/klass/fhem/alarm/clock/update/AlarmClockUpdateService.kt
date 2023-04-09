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

package li.klass.fhem.alarm.clock.update

import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import li.klass.fhem.connection.backend.ConnectionService
import li.klass.fhem.connection.backend.DummyServerSpec
import li.klass.fhem.constants.Actions
import li.klass.fhem.devices.backend.GenericDeviceService
import li.klass.fhem.update.backend.DeviceListService
import org.joda.time.DateTime
import org.slf4j.LoggerFactory
import java.util.*
import javax.inject.Inject

class AlarmClockUpdateService @Inject constructor(
        private val application: Application,
        private val deviceService: GenericDeviceService,
        private val deviceListService: DeviceListService,
        private val connectionService: ConnectionService
) {
    fun updateNextAlarmClock() {
        val nextAlarmClock = alarmManager.nextAlarmClock
                ?.triggerTime
                ?.let { DateTime(Date(it)).toString(alarmClockPattern) } ?: return
        connectionService.listAll()
                .filter { it !is DummyServerSpec }
                .forEach { updateNextAlarmClock(it.id, nextAlarmClock) }
    }

    private fun updateNextAlarmClock(connection: String, nextAlarmClockTrigger: String) {
        val nextAlarmClockReceiver = deviceListService.getDeviceForName(alarmClockDeviceName, connection) ?: return

        logger.info("updateNextAlarmClock(connection={}) - notifying alarm clock receiver for time {}", connection, nextAlarmClockTrigger)

        deviceService.setState(nextAlarmClockReceiver.xmlListDevice, nextAlarmClockTrigger, connection, false)
    }

    fun scheduleUpdate() {
        val pendingIntent = getNextAlarmClockPendingIntent()
        alarmManager.cancel(pendingIntent)
        alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, 0, alarmClockUpdateInterval, pendingIntent)
    }

    private fun getNextAlarmClockPendingIntent(): PendingIntent {
        val intent = Intent(Actions.UPDATE_NEXT_ALARM_CLOCK)
        return PendingIntent.getService(
            applicationContext,
            "nextAlarmClock".hashCode(),
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )
    }

    private val applicationContext get() = application.applicationContext
    private val alarmManager get() = applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    companion object {
        private val logger = LoggerFactory.getLogger(AlarmClockUpdateService::class.java)
        private const val alarmClockUpdateInterval = 6 * 60 * 60 * 1000L
        private const val alarmClockPattern = "dd.MM.YYYY HH:mm"
        private const val alarmClockDeviceName = "nextAlarmClock"
    }
}