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

package li.klass.fhem.service.favorites;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import li.klass.fhem.AndFHEMApplication;
import li.klass.fhem.domain.Device;
import li.klass.fhem.domain.RoomDeviceList;
import li.klass.fhem.service.room.RoomDeviceListListener;
import li.klass.fhem.service.room.RoomListService;

import java.util.Set;

/**
 * Class accumulating all methods to store and read favorites.
 */
public class FavoritesService {

    public static final FavoritesService INSTANCE = new FavoritesService();
    private static final String PREFERENCES_NAME = "favorites";

    private FavoritesService() {}

    /**
     * Adds a new favorite device.
     * @param device device to add.
     */
    public void addFavorite(Device device) {
        SharedPreferences.Editor editor = getPreferences().edit();
        editor.putString(device.getName(), device.getName()).commit();
    }

    /**
     * Removes a favorite.
     * @param device favorite to remove.
     */
    public void removeFavorite(Device device) {
        SharedPreferences.Editor editor = getPreferences().edit();
        editor.remove(device.getName()).commit();
    }

    /**
     * Reads all saved favorite devices. The result will be provided to the given listener.
     * @param context context in which the action was started.
     * @param refresh should the underlying {@link RoomDeviceList} be refreshed by asking FHEM for new values?
     * @param listener listener to notify when the favorites list has been retireved.
     */
    public void getFavorites(Context context, boolean refresh, final RoomDeviceListListener listener) {

        RoomListService.INSTANCE.getAllRoomsDeviceList(context, refresh, new RoomDeviceListListener() {

            @Override
            public void onRoomListRefresh(RoomDeviceList roomDeviceList) {
                RoomDeviceList deviceList = new RoomDeviceList("favorites");
                Set<String> favoriteDeviceNames = getPreferences().getAll().keySet();
                for (String favoriteDeviceName : favoriteDeviceNames) {
                    Device device = roomDeviceList.getDeviceFor(favoriteDeviceName);
                    if (device != null) {
                        deviceList.addDevice(device);
                    }
                }

                listener.onRoomListRefresh(deviceList);
            }
        });
    }

    /**
     * @return the {@link SharedPreferences} object.
     */
    private SharedPreferences getPreferences() {
        return AndFHEMApplication.getContext().getSharedPreferences(PREFERENCES_NAME, Activity.MODE_PRIVATE);
    }
}