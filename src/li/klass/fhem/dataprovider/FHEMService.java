package li.klass.fhem.dataprovider;

import li.klass.fhem.domain.RoomDeviceList;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FHEMService {
    public static final FHEMService INSTANCE = new FHEMService();

    private TelnetFHEM telnetFHEM = TelnetFHEM.INSTANCE;

    private Map<String, RoomDeviceList> roomDeviceListMap = FHEMDeviceListProvider.INSTANCE.listDevices();

    private FHEMService() {}

    public List<String> getRoomList() {
        return new ArrayList<String>(roomDeviceListMap.keySet());
    }

    public RoomDeviceList deviceListForRoom(String roomName) {
        return roomDeviceListMap.get(roomName);
    }

    public void executeCommand(String command) {
        telnetFHEM.executeCommand(command);
    }
}
