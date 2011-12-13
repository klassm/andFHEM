package li.klass.fhem.data;

import li.klass.fhem.domain.*;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

public class DeviceListProvider {

    public static final DeviceListProvider INSTANCE = new DeviceListProvider();

    private DeviceListProvider() {}

    public Map<String, RoomDeviceList> listDevices() {

        Map<String, RoomDeviceList> roomDeviceListMap = new HashMap<String, RoomDeviceList>();
        try {
            String xmlList = DataProviderSwitch.INSTANCE.getCurrentProvider().xmllist();
            xmlList = xmlList.replaceAll("\"<", "\"&lt;");
            xmlList = xmlList.replaceAll(">\"", "&gt;\"");
            xmlList = xmlList.replaceAll("_internal_", "internal");
            xmlList = xmlList.replaceAll("<notify_LIST[\\s\\S]*</notify_LIST>", "");
            xmlList = xmlList.replaceAll("<CUL_IR_LIST>[\\s\\S]*</CUL_IR_LIST>", "");
            xmlList = xmlList.replaceAll("value=\"\"[A-Za-z0-9 ${},]*\"\"", "");

            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            Document document = docBuilder.parse(new InputSource(new StringReader(xmlList)));

            devicesFromDocument(DeviceType.FS20, FS20Device.class, roomDeviceListMap, document, "FS20");
            devicesFromDocument(DeviceType.KS300, KS300Device.class, roomDeviceListMap, document, "KS300");
            devicesFromDocument(DeviceType.FHT, FHTDevice.class, roomDeviceListMap, document, "FHT");
            devicesFromDocument(DeviceType.HMS, HMSDevice.class, roomDeviceListMap, document, "HMS");
            devicesFromDocument(DeviceType.OWTEMP, OwtempDevice.class, roomDeviceListMap, document, "OWTEMP");
            devicesFromDocument(DeviceType.CUL_WS, CULWSDevice.class, roomDeviceListMap, document, "CUL_WS");
            devicesFromDocument(DeviceType.SIS_PMS, SISPMSDevice.class, roomDeviceListMap, document, "SIS_PMS");

            return roomDeviceListMap;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private <T extends Device> void devicesFromDocument(DeviceType deviceType, Class<T> deviceClass, Map<String,
            RoomDeviceList> roomDeviceListMap, Document document, String tagName) throws  Exception {
        
        NodeList nodes = document.getElementsByTagName(tagName);
        for (int i = 0; i < nodes.getLength(); i++) {
            Node item = nodes.item(i);
            deviceFromNode(deviceType, deviceClass, roomDeviceListMap, item);
        }
    }

    private <T extends Device> void deviceFromNode(DeviceType deviceType, Class<T> deviceClass, Map<String, RoomDeviceList> roomDeviceListMap, Node node)
            throws InstantiationException, IllegalAccessException {

        T device = deviceClass.newInstance();
        device.loadXML(node);
        RoomDeviceList roomDeviceList = getOrCreateRoomDeviceList(device.getRoom(), roomDeviceListMap);
        roomDeviceList.addDevice(deviceType, device);
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
