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

import com.google.common.base.Optional;

import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import li.klass.fhem.domain.core.FhemDevice;
import li.klass.fhem.domain.core.RoomDeviceList;

@Singleton
public class FavoritesService {
    public static final String PREFERENCES_NAME = "favorites";

    @Inject
    RoomListService roomListService;

    @Inject
    public FavoritesService() {
    }

    /**
     * Adds a new favorite device.
     *
     * @param deviceName name of the device to add
     */
    public void addFavorite(Context context, String deviceName) {
        SharedPreferences.Editor editor = getPreferences(context).edit();
        editor.putString(deviceName, deviceName).apply();
    }

    /**
     * @return the {@link android.content.SharedPreferences} object.
     */
    private SharedPreferences getPreferences(Context context) {
        return context.getSharedPreferences(PREFERENCES_NAME, Activity.MODE_PRIVATE);
    }

    /**
     * Removes a favorite.
     *
     * @param deviceName name of the device to remove
     */
    public void removeFavorite(Context context, String deviceName) {
        SharedPreferences.Editor editor = getPreferences(context).edit();
        editor.remove(deviceName).apply();
    }

    /**
     * Reads all saved favorite devices. The result will be provided to the given listener.
     *
     * @return favorite {@link RoomDeviceList}
     */
    public RoomDeviceList getFavorites(Context context) {

        RoomDeviceList allRoomsDeviceList = roomListService.getAllRoomsDeviceList(Optional.<String>absent(), context);
        RoomDeviceList favoritesList = new RoomDeviceList("favorites");
        favoritesList.setHiddenGroups(allRoomsDeviceList.getHiddenGroups());
        favoritesList.setHiddenRooms(allRoomsDeviceList.getHiddenRooms());

        Set<String> favoriteDeviceNames = getPreferences(context).getAll().keySet();
        for (String favoriteDeviceName : favoriteDeviceNames) {
            FhemDevice device = allRoomsDeviceList.getDeviceFor(favoriteDeviceName);
            if (device != null) {
                favoritesList.addDevice(device, context);
            }
        }

        return favoritesList;
    }

    public boolean hasFavorites(Context context) {
        return !getFavorites(context).isEmptyOrOnlyContainsDoNotShowDevices();
    }

    public boolean isFavorite(String deviceName, Context context) {
        return getPreferences(context).getAll().keySet().contains(deviceName);
    }
}
