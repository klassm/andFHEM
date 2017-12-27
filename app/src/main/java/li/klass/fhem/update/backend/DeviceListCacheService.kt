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

import li.klass.fhem.connection.backend.ConnectionService
import li.klass.fhem.domain.core.RoomDeviceList
import org.joda.time.DateTime
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeviceListCacheService @Inject constructor(
        private val deviceListFileSystemService: DeviceListFileSystemService,
        private val connectionService: ConnectionService
) {
    private val cache = HashMap<String, DeviceListCache>()

    @Synchronized
    fun storeDeviceListMap(roomDeviceList: RoomDeviceList, connectionId: String?): Boolean {
        val connection = connectionFor(connectionId)
        cache.put(connection, deviceListFileSystemService.storeDeviceListMap(roomDeviceList, connection))
        return true
    }

    fun getCachedRoomDeviceListMap(connectionId: String?): RoomDeviceList? {
        val connection = connectionFor(connectionId)
        return getCacheFor(connection)?.deviceList
    }

    private fun getCacheFor(connectionId: String): DeviceListCache? {
        if (!cache.containsKey(connectionId)) {
            cache.put(connectionId, deviceListFileSystemService.getCachedRoomDeviceListMap(connectionId))
        }
        return cache[connectionId]
    }

    fun getLastUpdate(connectionId: String?): DateTime? =
            getCacheFor(connectionFor(connectionId))?.lastUpdate

    fun isCorrupted(connection: String = connectionService.getSelectedId()): Boolean =
            getCacheFor(connection)?.corrupted ?: true

    private fun connectionFor(connectionId: String?) =
            connectionId ?: connectionService.getSelectedId()
}
