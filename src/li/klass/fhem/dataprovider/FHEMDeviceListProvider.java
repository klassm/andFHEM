package li.klass.fhem.dataprovider;

import android.util.Log;
import li.klass.fhem.domain.KS300Device;
import li.klass.fhem.domain.RoomDeviceList;
import li.klass.fhem.domain.FS20Device;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

public class FHEMDeviceListProvider {

    public static final FHEMDeviceListProvider INSTANCE = new FHEMDeviceListProvider();

    private FHEMDeviceListProvider() {}

    public Map<String, RoomDeviceList> listDevices() {

        Map<String, RoomDeviceList> roomDeviceListMap = new HashMap<String, RoomDeviceList>();
        try {
            String xmllist = TelnetFHEM.INSTANCE.xmllist();

            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            Document document = docBuilder.parse(new InputSource(new StringReader(xmllist)));

            fs20Devices(roomDeviceListMap, document);
            ks300Devices(roomDeviceListMap, document);
            
            return roomDeviceListMap;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void fs20Devices(Map<String, RoomDeviceList> roomDeviceListMap, Document document) {
        NodeList fs20Nodes = document.getElementsByTagName("FS20");
        for (int i = 0; i < fs20Nodes.getLength(); i++) {
            Node item = fs20Nodes.item(i);
            FS20Device fs20Device = new FS20Device();
            fs20Device.loadXML(item);
            Log.e(FHEMDeviceListProvider.class.getName(), fs20Device.toString());
            RoomDeviceList roomDeviceList = getOrCreateRoomDeviceList(fs20Device.getRoom(), roomDeviceListMap);
            roomDeviceList.addFS20Device(fs20Device);
        }
    }

    private void ks300Devices(Map<String, RoomDeviceList> roomDeviceListMap, Document document) {
        NodeList ks300Nodes = document.getElementsByTagName("KS300");
        for (int i = 0; i < ks300Nodes.getLength(); i++) {
            Node item = ks300Nodes.item(i);
            KS300Device ks300Device = new KS300Device();
            ks300Device.loadXML(item);
            Log.e(FHEMDeviceListProvider.class.getName(), ks300Device.toString());
            RoomDeviceList roomDeviceList = getOrCreateRoomDeviceList(ks300Device.getRoom(), roomDeviceListMap);
            roomDeviceList.addKS300Device(ks300Device);
        }
    }

    private RoomDeviceList getOrCreateRoomDeviceList(String roomName, Map<String, RoomDeviceList> roomDeviceListMap) {
        if (roomDeviceListMap.containsKey(roomName)) {
            return roomDeviceListMap.get(roomName);
        }
        RoomDeviceList roomDeviceList = new RoomDeviceList(roomName);
        roomDeviceListMap.put(roomName, roomDeviceList);
        return roomDeviceList;
    }
}
