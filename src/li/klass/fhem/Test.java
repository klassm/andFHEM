package li.klass.fhem;

import li.klass.fhem.dataprovider.FHEMDeviceListProvider;
import li.klass.fhem.domain.RoomDeviceList;
import li.klass.fhem.domain.FS20Device;

import java.util.Map;

public class Test {
    public static void main(String[] args) throws Exception {
        Map<String,RoomDeviceList> roomDeviceListMap = FHEMDeviceListProvider.INSTANCE.listDevices();
        for (String room : roomDeviceListMap.keySet()) {
            System.out.println(">> " + room);
            
            for (FS20Device fs20Device : roomDeviceListMap.get(room).getFs20Devices()) {
                System.out.println(fs20Device.toString());
            }
        }

    }
}
