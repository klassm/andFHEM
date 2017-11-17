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

package li.klass.fhem.devices.list.favorites.backend

import android.content.Context
import android.content.SharedPreferences
import com.google.common.collect.FluentIterable.from
import li.klass.fhem.connection.backend.ConnectionService
import li.klass.fhem.domain.core.FhemDevice
import li.klass.fhem.domain.core.RoomDeviceList
import li.klass.fhem.update.backend.DeviceListService
import li.klass.fhem.util.preferences.SharedPreferencesService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FavoritesService @Inject
constructor(val deviceListService: DeviceListService,
            val connectionService: ConnectionService,
            val sharedPreferencesService: SharedPreferencesService) {

    /**
     * Adds a new favorite device.
     *
     * @param deviceName name of the device to add
     */
    fun addFavorite(context: Context, deviceName: String) {
        val editor = getPreferences(context).edit()
        editor.putString(deviceName, deviceName).apply()
    }

    /**
     * @return the [android.content.SharedPreferences] object.
     */
    private fun getPreferences(context: Context): SharedPreferences {
        val name = preferenceNameFor(connectionService.getSelectedId())
        return getPreferencesFor(context, name)
    }

    private fun getPreferencesFor(context: Context, name: String): SharedPreferences =
            sharedPreferencesService.getPreferences(name, context)

    fun getPreferenceNames(): Set<String> =
            from(connectionService.listAll()).transform { input -> preferenceNameFor(input!!.id) }.toSet()

    private fun preferenceNameFor(id: String): String = PREFERENCES_NAME + "_" + id

    /**
     * Removes a favorite.
     *
     * @param deviceName name of the device to remove
     */
    fun removeFavorite(context: Context, deviceName: String) {
        val editor = getPreferences(context).edit()
        editor.remove(deviceName).apply()
    }

    /**
     * Reads all saved favorite devices. The result will be provided to the given listener.
     *
     * @return favorite [RoomDeviceList]
     */
    fun getFavorites(context: Context): RoomDeviceList {
        val allRoomsDeviceList = deviceListService.getAllRoomsDeviceList()
        val favoritesList = RoomDeviceList("favorites")

        val favoriteDeviceNames = getPreferences(context).all.keys
        favoriteDeviceNames
                .mapNotNull { allRoomsDeviceList.getDeviceFor<FhemDevice>(it) }
                .forEach { favoritesList.addDevice(it, context) }

        return favoritesList
    }

    fun hasFavorites(context: Context): Boolean =
            !getFavorites(context).isEmptyOrOnlyContainsDoNotShowDevices

    fun isFavorite(deviceName: String, context: Context): Boolean =
            getPreferences(context).all.keys.contains(deviceName)

    companion object {
        private val PREFERENCES_NAME = "favorites"
    }
}
