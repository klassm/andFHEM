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

package li.klass.fhem.service.room

import android.content.Context
import com.google.common.base.Optional
import li.klass.fhem.domain.FHEMWEBDevice
import li.klass.fhem.domain.core.RoomDeviceList
import li.klass.fhem.service.connection.ConnectionService
import li.klass.fhem.util.ApplicationProperties
import li.klass.fhem.util.preferences.SharedPreferencesService
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoomListHolderService @Inject constructor(
        val applicationProperties: ApplicationProperties,
        val connectionService: ConnectionService,
        val sharedPreferencesService: SharedPreferencesService
) {
    private val cache = HashMap<String, RoomListCache>()

    @Synchronized
    fun storeDeviceListMap(roomDeviceList: RoomDeviceList, connectionId: Optional<String>, context: Context): Boolean =
            getCacheFor(connectionId, context).storeDeviceListMap(roomDeviceList, context)

    fun getCachedRoomDeviceListMap(connectionId: Optional<String>, context: Context): Optional<RoomDeviceList> =
            getCacheFor(connectionId, context).getCachedRoomDeviceListMap(context)

    private fun getCacheFor(connectionId: Optional<String>, context: Context): RoomListCache {
        val toLoad = if (connectionService.exists(connectionId, context)) connectionId else Optional.absent()
        return getCacheForConnectionId(toLoad, context)
    }

    fun getLastUpdate(connectionId: Optional<String>, context: Context): Long =
            getCacheFor(connectionId, context).getLastUpdate(context)

    private fun getCacheForConnectionId(connectionId: Optional<String>, context: Context): RoomListCache {
        val id = connectionId.or(connectionService.getSelectedId(context))
        if (!cache.containsKey(id)) {
            cache.put(id, RoomListCache(id, applicationProperties, connectionService, sharedPreferencesService))
        }
        return cache[id]!!
    }

    fun findFHEMWEBDevice(roomDeviceList: RoomDeviceList, context: Context): FHEMWEBDevice =
            FHEMWebDeviceInRoomDeviceListSupplier(applicationProperties, connectionService, roomDeviceList, context).get()

    fun isCorrupted(connection: Optional<String>, context: Context): Boolean =
            getCacheForConnectionId(connection, context).isCorrupted()
}
