package li.klass.fhem.favorites;

import android.content.Context;
import android.util.Log;
import li.klass.fhem.AndFHEMApplication;
import li.klass.fhem.data.FHEMService;
import li.klass.fhem.domain.Device;
import li.klass.fhem.domain.DeviceType;
import li.klass.fhem.domain.RoomDeviceList;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.*;

public class FavoritesService {

    private Map<String, Set<Device>> favorites = null;

    public static final FavoritesService INSTANCE = new FavoritesService();
    private static final String CACHE_FILENAME = "favorites.dat";

    private FavoritesService() {
    }

    public void addFavorite(Device device) {
        String room = device.getRoom();
        if (favorites.get(room) == null) {
            favorites.put(room, new HashSet<Device>());
        }
        favorites.get(room).add(device);
    }

    public void removeFavorite(Device device) {
        Set<Device> devices = getFavoritesMap().get(device.getRoom());
        if (devices != null) {
            devices.remove(device);
        }
    }

    public RoomDeviceList getFavorites(boolean refresh) {
        RoomDeviceList deviceList = new RoomDeviceList("favorites");

        List<Device> toRemove = new ArrayList<Device>();
        for (String key : getFavoritesMap().keySet()) {
            RoomDeviceList roomDeviceList = FHEMService.INSTANCE.deviceListForRoom(key, refresh);
            Set<Device> favoriteDevices = favorites.get(key);

            for (Device favoriteDevice : favoriteDevices) {
                Device foundDevice = null;
                for (DeviceType deviceType : DeviceType.values()) {
                    Collection<Device> roomDevices = roomDeviceList.getDevicesOfType(deviceType);
                    foundDevice = findDeviceFor(favoriteDevice.getName(), roomDevices);
                    deviceList.addDevice(deviceType, foundDevice);

                    if (foundDevice != null) break;
                }


                if (foundDevice == null) {
                    toRemove.add(favoriteDevice);
                }

            }
        }

        for (Device device : toRemove) {
            getFavoritesMap().get(device.getRoom()).remove(device);
        }

        return deviceList;
    }

    private Map<String, Set<Device>> getFavoritesMap() {
        if (favorites != null) {
            return favorites;
        }

        favorites = getStoredDataFromFile();
        return favorites;
    }

    private <T extends Device> T findDeviceFor(String deviceName, Collection<T> roomDevices) {
        if (roomDevices == null) return null;
        
        for (T device : roomDevices) {
            if (deviceName.equals(device.getName())) {
                return device;
            }
        }
        return null;
    }


    public void storeFavorites() {
        cacheRoomDeviceListMap(favorites);
    }

    private void cacheRoomDeviceListMap(Map<String, Set<Device>> content) {
        Context context = AndFHEMApplication.getContext();
        try {
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(context.openFileOutput(CACHE_FILENAME, Context.MODE_PRIVATE));
            objectOutputStream.writeObject(content);
        } catch (Exception e) {
            Log.e(FHEMService.class.getName(), "error occurred while serializing data", e);
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Set<Device>> getStoredDataFromFile() {
        try {
            Context context = AndFHEMApplication.getContext();
            ObjectInputStream objectInputStream = new ObjectInputStream(context.openFileInput(CACHE_FILENAME));
            return (Map<String, Set<Device>>) objectInputStream.readObject();
        } catch (Exception e) {
            Log.e(FHEMService.class.getName(), "error occurred while de-serializing mensa data", e);
        }
        return new HashMap<String, Set<Device>>();
    }
}