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

package li.klass.fhem.update.backend

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import li.klass.fhem.domain.core.RoomDeviceList
import li.klass.fhem.util.preferences.SharedPreferencesService
import org.joda.time.DateTime
import org.slf4j.LoggerFactory
import java.io.*
import javax.inject.Inject

class DeviceListFileSystemService @Inject constructor(
        private val application: Application,
        private val sharedPreferencesService: SharedPreferencesService
) {
    @Synchronized
    fun storeDeviceListMap(roomDeviceList: RoomDeviceList, connection: String): DeviceListCache {
        storeDeviceListMapInternal(roomDeviceList, connection)
        setLastUpdate(connection)
        return getCachedRoomDeviceListMap(connection)
    }

    private fun storeDeviceListMapInternal(roomDeviceList: RoomDeviceList, connection: String) {
        LOG.info("storeDeviceListMap() : storing device list to cache")
        val startLoad = System.currentTimeMillis()
        try {
            val fileOutput = fileOutputFor(connection)
            ObjectOutputStream(BufferedOutputStream(fileOutput)).use {
                it.writeObject(RoomDeviceList(roomDeviceList))
            }
            LOG.info("storeDeviceListMap() : storing device list to cache completed after {} ms",
                    System.currentTimeMillis() - startLoad)
        } catch (e: Exception) {
            LOG.error("storeDeviceListMap() : error occurred while writing data to disk", e)
        }
    }

    /**
     * Loads the currently cached room device list map data from the file storage.

     * @return cached room device list map
     * *
     * @param context
     */
    fun getCachedRoomDeviceListMap(connectionId: String): DeviceListCache {

        synchronized(this) {

            try {
                LOG.info("getCachedRoomDeviceListMap() : fetching device list from cache")
                val startLoad = System.currentTimeMillis()

                val fileInput = fileInputFor(connectionId)
                val deviceList = ObjectInputStream(BufferedInputStream(fileInput)).use {
                    it.readObject() as RoomDeviceList
                }
                LOG.info("getCachedRoomDeviceListMap() : loading device list from cache completed after {} ms",
                        System.currentTimeMillis() - startLoad)

                return DeviceListCache(
                        corrupted = false,
                        deviceList = deviceList,
                        lastUpdate = getLastUpdate(connectionId))
            } catch (e: Exception) {
                LOG.info("getCachedRoomDeviceListMap() : error occurred while de-serializing data", e)
                return DeviceListCache(corrupted = true, deviceList = null, lastUpdate = null)
            }
        }
    }

    private fun fileOutputFor(connection: String): FileOutputStream? {
        val fileName = fileNameFor(connection)
        return applicationContext.openFileOutput(fileName, Context.MODE_PRIVATE)
    }

    private fun fileInputFor(connection: String): FileInputStream? {
        val fileName = fileNameFor(connection)
        return applicationContext.openFileInput(fileName)
    }

    fun getLastUpdate(connectionId: String): DateTime =
            DateTime(getPreferences().getLong(lastUpdatePropertyFor(connectionId), 0L))

    private fun setLastUpdate(connectionId: String) {
        getPreferences().edit().putLong(lastUpdatePropertyFor(connectionId), System.currentTimeMillis()).apply()
    }

    private fun lastUpdatePropertyFor(connectionId: String) = LAST_UPDATE_PROPERTY + "_" + connectionId
    private fun fileNameFor(connectionId: String): String = connectionId + ".serverCache"

    private fun getPreferences(): SharedPreferences =
            sharedPreferencesService.getPreferences(PREFERENCES_NAME)

    private val applicationContext: Context get() = application.applicationContext

    companion object {
        val PREFERENCES_NAME = DeviceListFileSystemService::class.java.name

        private val LOG = LoggerFactory.getLogger(DeviceListFileSystemService::class.java)
        val LAST_UPDATE_PROPERTY = "LAST_UPDATE"
    }
}