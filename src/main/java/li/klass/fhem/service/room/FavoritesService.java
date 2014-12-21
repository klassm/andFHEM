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

package li.klass.fhem.service.room;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import li.klass.fhem.dagger.ForApplication;
import li.klass.fhem.domain.core.Device;
import li.klass.fhem.domain.core.RoomDeviceList;

@Singleton
public class FavoritesService {
    private static final String PREFERENCES_NAME = "favorites";
    @Inject
    RoomListService roomListService;
    @Inject
    @ForApplication
    Context applicationContext;

    /**
     * Adds a new favorite device.
     *
     * @param device device to add.
     */
    public void addFavorite(Device device) {
        SharedPreferences.Editor editor = getPreferences().edit();
        editor.putString(device.getName(), device.getName()).apply();
    }

    /**
     * @return the {@link android.content.SharedPreferences} object.
     */
    private SharedPreferences getPreferences() {
        return applicationContext.getSharedPreferences(PREFERENCES_NAME, Activity.MODE_PRIVATE);
    }

    /**
     * Removes a favorite.
     *
     * @param device favorite to remove.
     */
    public void removeFavorite(Device device) {
        SharedPreferences.Editor editor = getPreferences().edit();
        editor.remove(device.getName()).apply();
    }

    /**
     * Reads all saved favorite devices. The result will be provided to the given listener.
     *
     * @return favorite {@link RoomDeviceList}
     */
    public RoomDeviceList getFavorites() {

        RoomDeviceList allRoomsDeviceList = roomListService.getAllRoomsDeviceList();
        RoomDeviceList favoritesList = new RoomDeviceList("favorites");
        favoritesList.setHiddenGroups(allRoomsDeviceList.getHiddenGroups());
        favoritesList.setHiddenRooms(allRoomsDeviceList.getHiddenRooms());

        Set<String> favoriteDeviceNames = getPreferences().getAll().keySet();
        for (String favoriteDeviceName : favoriteDeviceNames) {
            Device device = allRoomsDeviceList.getDeviceFor(favoriteDeviceName);
            if (device != null) {
                favoritesList.addDevice(device);
            }
        }

        return favoritesList;
    }

    public boolean hasFavorites() {
        return !getFavorites().isEmptyOrOnlyContainsDoNotShowDevices();
    }

    public boolean isFavorite(String deviceName) {
        return getPreferences().getAll().keySet().contains(deviceName);
    }
}
