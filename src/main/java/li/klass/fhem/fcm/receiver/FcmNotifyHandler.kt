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
import android.content.Intent
import com.google.common.base.Optional
import com.google.common.collect.Maps
import li.klass.fhem.appwidget.update.AppWidgetUpdateService
import li.klass.fhem.constants.Actions
import li.klass.fhem.domain.core.FhemDevice
import li.klass.fhem.fcm.history.data.FcmHistoryService
import li.klass.fhem.fcm.receiver.data.FcmNotifyData
import li.klass.fhem.service.NotificationService
import li.klass.fhem.settings.SettingsKeys
import li.klass.fhem.update.backend.RoomListService
import li.klass.fhem.util.ApplicationProperties
import li.klass.fhem.util.Tasker
import javax.inject.Inject

class FcmNotifyHandler @Inject constructor(
        private val roomListService: RoomListService,
        private val fcmHistoryService: FcmHistoryService,
        private val appWidgetUpdateService: AppWidgetUpdateService,
        private val applicationProperties: ApplicationProperties,
        private val notificationService: NotificationService
) {
    fun handleNotify(data: FcmNotifyData, context: Context) {
        val changesText = data.changes ?: return
        val deviceName = data.deviceName ?: return

        val changes = extractChanges(deviceName, changesText, context)
        roomListService.parseReceivedDeviceStateMap(deviceName, changes, context)

        fcmHistoryService.addChanges(deviceName, changes)
        updateWidgets()
        updateUI(context)
        showNotification(deviceName, changes, data.shouldVibrate(), context)
    }

    private fun extractChanges(deviceName: String, changesText: String, context: Context): Map<String, String> {
        val changes = changesText.split("<\\|>".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

        val changeMap = Maps.newHashMap<String, String>()
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

            Tasker.sendTaskerNotifyIntent(context, deviceName, key, value)

            changeMap.put(key, value)
        }
        return changeMap
    }

    private fun updateUI(context: Context) {
        context.sendBroadcast(Intent(Actions.DO_UPDATE))
    }

    private fun updateWidgets() {
        val updateWidgets = applicationProperties.getBooleanSharedPreference(SettingsKeys.GCM_WIDGET_UPDATE, false)
        if (updateWidgets) {
            appWidgetUpdateService.updateAllWidgets()
        }
    }

    private fun showNotification(deviceName: String, updates: Map<String, String>, vibrate: Boolean, context: Context) {
        val device: Optional<FhemDevice> = roomListService.getDeviceForName(deviceName, Optional.absent(), context)
        if (!device.isPresent) {
            return
        }

        notificationService.deviceNotification(updates, device.get(), vibrate, context)
    }
}