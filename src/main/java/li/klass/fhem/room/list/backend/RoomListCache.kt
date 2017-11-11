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

package li.klass.fhem.room.list.backend

import android.content.Context
import android.content.SharedPreferences
import com.google.common.base.Optional
import li.klass.fhem.connection.backend.ConnectionService
import li.klass.fhem.domain.FHEMWEBDevice
import li.klass.fhem.domain.core.RoomDeviceList
import li.klass.fhem.util.ApplicationProperties
import li.klass.fhem.util.CloseableUtil
import li.klass.fhem.util.preferences.SharedPreferencesService
import org.slf4j.LoggerFactory
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

class RoomListCache(private val connectionId: String, internal var applicationProperties: ApplicationProperties, internal var connectionService: ConnectionService, private val sharedPreferencesService: SharedPreferencesService) {
    @Volatile private var cachedRoomList: RoomDeviceList? = null
    @Volatile private var fileStoreNotFilled = false
    @Volatile private var excptionDuringLoad = false
    private val lastUpdateProperty = RoomListService.LAST_UPDATE_PROPERTY + "_" + this.connectionId

    @Synchronized fun storeDeviceListMap(roomDeviceList: RoomDeviceList?, context: Context): Boolean {
        if (roomDeviceList == null) {
            LOG.info("storeDeviceListMap() : won't store device list, as empty")
            return false
        }
        storeDeviceListMapInternal(roomDeviceList, context)
        setLastUpdate(context)
        return true
    }

    private fun storeDeviceListMapInternal(roomDeviceList: RoomDeviceList, context: Context) {
        fillHiddenRoomsAndHiddenGroups(roomDeviceList, findFHEMWEBDevice(roomDeviceList, context))
        cachedRoomList = roomDeviceList
        LOG.info("storeDeviceListMap() : storing device list to cache")
        val startLoad = System.currentTimeMillis()
        var objectOutputStream: ObjectOutputStream? = null
        try {
            objectOutputStream = ObjectOutputStream(BufferedOutputStream(context.openFileOutput(fileName, Context.MODE_PRIVATE)))
            objectOutputStream.writeObject(roomDeviceList)
            fileStoreNotFilled = false
            LOG.info("storeDeviceListMap() : storing device list to cache completed after {} ms",
                    System.currentTimeMillis() - startLoad)
        } catch (e: Exception) {
            LOG.error("storeDeviceListMap() : error occurred while writing data to disk", e)
        } finally {
            CloseableUtil.close(objectOutputStream)
        }
    }

    private val fileName: String
        get() = connectionId + ".serverCache"

    private fun fillHiddenRoomsAndHiddenGroups(newRoomDeviceList: RoomDeviceList?,
                                               fhemwebDevice: FHEMWEBDevice) {
        if (newRoomDeviceList == null) return

        newRoomDeviceList.hiddenGroups = fhemwebDevice.hiddenGroups
        newRoomDeviceList.hiddenRooms = fhemwebDevice.hiddenRooms
    }


    private fun findFHEMWEBDevice(allRoomDeviceList: RoomDeviceList, context: Context): FHEMWEBDevice =
            FHEMWebDeviceInRoomDeviceListSupplier(applicationProperties, connectionService, allRoomDeviceList, context).get()

    /**
     * Loads the currently cached room device list map data from the file storage.

     * @return cached room device list map
     * *
     * @param context
     */
    fun getCachedRoomDeviceListMap(context: Context): Optional<RoomDeviceList> {
        if (cachedRoomList != null || fileStoreNotFilled) {
            return Optional.fromNullable(cachedRoomList)
        }
        synchronized(this) {
            if (cachedRoomList != null || fileStoreNotFilled) {
                return Optional.fromNullable(cachedRoomList)
            }

            var objectInputStream: ObjectInputStream? = null
            try {
                LOG.info("getCachedRoomDeviceListMap() : fetching device list from cache")
                val startLoad = System.currentTimeMillis()

                objectInputStream = ObjectInputStream(BufferedInputStream(context.openFileInput(fileName)))
                cachedRoomList = objectInputStream.readObject() as RoomDeviceList
                LOG.info("getCachedRoomDeviceListMap() : loading device list from cache completed after {} ms",
                        System.currentTimeMillis() - startLoad)
                if (cachedRoomList != null && cachedRoomList!!.isEmptyOrOnlyContainsDoNotShowDevices) {
                    cachedRoomList = null
                    fileStoreNotFilled = true
                }
            } catch (e: Exception) {
                LOG.info("getCachedRoomDeviceListMap() : error occurred while de-serializing data", e)
                fileStoreNotFilled = true
                excptionDuringLoad = true
                return Optional.absent<RoomDeviceList>()
            } finally {
                CloseableUtil.close(objectInputStream)
            }
        }

        return Optional.fromNullable(cachedRoomList)
    }

    fun getLastUpdate(context: Context): Long =
            getPreferences(context).getLong(lastUpdateProperty, 0L)

    private fun setLastUpdate(context: Context) {
        getPreferences(context).edit().putLong(lastUpdateProperty, System.currentTimeMillis()).apply()
    }

    private fun getPreferences(context: Context): SharedPreferences =
            sharedPreferencesService.getPreferences(RoomListService.PREFERENCES_NAME, context)

    fun isCorrupted() = excptionDuringLoad

    companion object {
        private val LOG = LoggerFactory.getLogger(RoomListCache::class.java)
    }
}
