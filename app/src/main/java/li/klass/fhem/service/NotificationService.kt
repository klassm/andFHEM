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

package li.klass.fhem.service

import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.SystemClock
import com.google.common.base.Joiner
import com.google.common.collect.Maps.newHashMap
import li.klass.fhem.activities.AndFHEMMainActivity
import li.klass.fhem.constants.BundleExtraKeys
import li.klass.fhem.domain.core.FhemDevice
import li.klass.fhem.ui.FragmentType
import li.klass.fhem.util.NotificationUtil
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationService @Inject
constructor() {

    fun rename(deviceName: String, deviceNewName: String, context: Context) {
        val preferences = getPreferences(context)
        if (preferences.contains(deviceName)) {
            val value = preferences.getInt(deviceName, 0)
            preferences.edit().remove(deviceName).putInt(deviceNewName, value).apply()
        }
    }

    fun setDeviceNotification(deviceName: String, updateType: Int, context: Context) {
        getPreferences(context).edit().putInt(deviceName, updateType).apply()
    }

    fun deviceNotification(updateMap: Map<String, String>, device: FhemDevice, vibrate: Boolean, context: Context) {
        val value = getPreferences(context).getInt(device.name, 0)

        if (isValueAllUpdates(value)) {
            generateNotification(device, updateMap, vibrate, context)
        } else if (isValueStateUpdates(value) && updateMap.containsKey("STATE")) {
            val values = newHashMap<String, String>()
            values.put("STATE", updateMap["STATE"])
            generateNotification(device, values, vibrate, context)
        }
    }

    fun forDevice(context: Context, deviceName: String): Int =
            getPreferences(context).getInt(deviceName, 0)

    private fun isValueAllUpdates(value: Int): Boolean = value == ALL_UPDATES

    private fun isValueStateUpdates(value: Int): Boolean = value == STATE_UPDATES


    private fun generateNotification(device: FhemDevice, updateMap: Map<String, String>, vibrate: Boolean, context: Context) {
        val deviceName = device.name

        val openIntent = Intent(context, AndFHEMMainActivity::class.java)
                .setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                .putExtra(BundleExtraKeys.FRAGMENT, FragmentType.DEVICE_DETAIL)
                .putExtra(BundleExtraKeys.DEVICE_NAME, deviceName)
                .putExtra("unique", "foobar://" + SystemClock.elapsedRealtime())
        val pendingIntent = PendingIntent.getActivity(context, deviceName.hashCode(), openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT)

        val text: String = if (updateMap.size == 1 && updateMap.containsKey("state")) {
            updateMap["state"]!!
        } else if (updateMap.size == 1 && updateMap.containsKey("STATE")) {
            updateMap["STATE"]!!
        } else {
            Joiner.on(",").withKeyValueSeparator(" : ").join(updateMap)
        }

        logger.info("generateNotification(device=$deviceName) - text=$text, vibrate=$vibrate")
        NotificationUtil.notify(context, deviceName.hashCode(), pendingIntent, deviceName, text,
                deviceName, vibrate)
    }


    private fun getPreferences(context: Context): SharedPreferences =
            context.getSharedPreferences(PREFERENCES_NAME, Activity.MODE_PRIVATE)

    companion object {
        val NO_UPDATES = 0
        val ALL_UPDATES = 1
        val STATE_UPDATES = 2

        val PREFERENCES_NAME = "deviceNotifications"
        val logger = LoggerFactory.getLogger(NotificationService::class.java)
    }
}
