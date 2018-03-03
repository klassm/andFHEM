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

import android.content.SharedPreferences
import com.google.common.collect.FluentIterable.from
import li.klass.fhem.connection.backend.ConnectionService
import li.klass.fhem.domain.core.RoomDeviceList
import li.klass.fhem.update.backend.DeviceListService
import li.klass.fhem.util.preferences.SharedPreferencesService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FavoritesService @Inject
constructor(private val deviceListService: DeviceListService,
            private val connectionService: ConnectionService,
            private val sharedPreferencesService: SharedPreferencesService) {

    fun rename(deviceName: String, newName: String) {
        if (getFavorites().getDeviceFor(deviceName) != null) {
            removeFavorite(deviceName)
            addFavorite(newName)
        }
    }

    /**
     * Adds a new favorite device.
     *
     * @param deviceName name of the device to add
     */
    fun addFavorite(deviceName: String) {
        val editor = getPreferences().edit()
        editor.putString(deviceName, deviceName).apply()
    }

    /**
     * @return the [android.content.SharedPreferences] object.
     */
    private fun getPreferences(): SharedPreferences {
        val name = preferenceNameFor(connectionService.getSelectedId())
        return getPreferencesFor(name)
    }

    private fun getPreferencesFor(name: String): SharedPreferences =
            sharedPreferencesService.getPreferences(name)

    fun getPreferenceNames(): Set<String> =
            from(connectionService.listAll()).transform { input -> preferenceNameFor(input!!.id) }.toSet()

    private fun preferenceNameFor(id: String): String = PREFERENCES_NAME + "_" + id

    /**
     * Removes a favorite.
     *
     * @param deviceName name of the device to remove
     */
    fun removeFavorite(deviceName: String) {
        val editor = getPreferences().edit()
        editor.remove(deviceName).apply()
    }

    /**
     * Reads all saved favorite devices. The result will be provided to the given listener.
     *
     * @return favorite [RoomDeviceList]
     */
    fun getFavorites(): RoomDeviceList {
        val allRoomsDeviceList = deviceListService.getAllRoomsDeviceList()
        val favoritesList = RoomDeviceList("favorites")

        val favoriteDeviceNames = getPreferences().all.keys
        favoriteDeviceNames
                .mapNotNull { allRoomsDeviceList.getDeviceFor(it) }
                .forEach { favoritesList.addDevice(it) }

        return favoritesList
    }

    fun hasFavorites(): Boolean =
            !getFavorites().isEmpty

    fun isFavorite(deviceName: String): Boolean =
            getPreferences().all.keys.contains(deviceName)

    companion object {
        private val PREFERENCES_NAME = "favorites"
    }
}
