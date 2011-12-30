package li.klass.fhem.data.provider.favorites;

import android.app.Activity;
import android.content.SharedPreferences;
import li.klass.fhem.AndFHEMApplication;
import li.klass.fhem.domain.Device;
import li.klass.fhem.domain.RoomDeviceList;
import li.klass.fhem.service.RoomListService;

import java.util.Set;

public class FavoritesService {

    public static final FavoritesService INSTANCE = new FavoritesService();
    private static final String PREFERENCES_NAME = "favorites";

    private FavoritesService() {
    }

    public void addFavorite(Device device) {
        getPreferences()
                .edit().
                putString(device.getName(), device.getName())
                .commit();
    }

    public void removeFavorite(Device device) {
        getPreferences().edit().remove(device.getName()).commit();
    }

    public RoomDeviceList getFavorites(boolean refresh) {
        RoomDeviceList deviceList = new RoomDeviceList("favorites");
        RoomDeviceList roomDeviceList = RoomListService.INSTANCE.deviceListForAllRooms(refresh);

        Set<String> favoriteDeviceNames = getPreferences().getAll().keySet();
        for (String favoriteDeviceName : favoriteDeviceNames) {
            Device device = roomDeviceList.getDeviceFor(favoriteDeviceName);
            if (device != null) {
                deviceList.addDevice(device);
            }
        }

        return deviceList;
    }

    private SharedPreferences getPreferences() {
        return AndFHEMApplication.getContext().getSharedPreferences(PREFERENCES_NAME, Activity.MODE_PRIVATE);
    }
}