package li.klass.fhem.dataprovider;

import android.content.Context;
import android.util.Log;
import li.klass.fhem.AndFHEMApplication;
import li.klass.fhem.domain.Device;
import li.klass.fhem.domain.FS20Device;
import li.klass.fhem.domain.KS300Device;
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

    public RoomDeviceList getFavorites() {
        RoomDeviceList deviceList = new RoomDeviceList("favorites");

        List<Device> toRemove = new ArrayList<Device>();
        for (String key : getFavoritesMap().keySet()) {
            RoomDeviceList roomDeviceList = FHEMService.INSTANCE.deviceListForRoom(key, false);
            Set<Device> favoriteDevices = favorites.get(key);

            for (Device favoriteDevice : favoriteDevices) {
                FS20Device fs20Device = findDeviceFor(favoriteDevice.getName(), roomDeviceList.getFs20Devices());
                deviceList.addFS20Device(fs20Device);
                
                KS300Device ks300Device = findDeviceFor(favoriteDevice.getName(), roomDeviceList.getKS300Devices());
                deviceList.addKS300Device(ks300Device);

                if (fs20Device == null && ks300Device == null) {
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

    private <T extends Device> T findDeviceFor(String deviceName, List<T> roomDevices) {
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