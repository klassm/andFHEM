package li.klass.fhem.data;

import li.klass.fhem.domain.*;
import li.klass.fhem.exception.DeviceListParseException;
import li.klass.fhem.exception.HostConnectionException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static li.klass.fhem.domain.DeviceType.*;

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

            devicesFromDocument(FS20Device.class, roomDeviceListMap, document, "FS20");
            devicesFromDocument(KS300Device.class, roomDeviceListMap, document, "KS300");
            devicesFromDocument(FHTDevice.class, roomDeviceListMap, document, "FHT");
            devicesFromDocument(HMSDevice.class, roomDeviceListMap, document, "HMS");
            devicesFromDocument(OwtempDevice.class, roomDeviceListMap, document, "OWTEMP");
            devicesFromDocument(CULWSDevice.class, roomDeviceListMap, document, "CUL_WS");
            devicesFromDocument(SISPMSDevice.class, roomDeviceListMap, document, "SIS_PMS");
            devicesFromDocument(FileLog.class, roomDeviceListMap, document, "FileLog");
            devicesFromDocument(CULFHTTKDevice.class, roomDeviceListMap, document, "CUL_FHTTK");

            addFileLogsToDevices(roomDeviceListMap);

            return roomDeviceListMap;
        } catch (HostConnectionException e) {
            throw e;
        } catch (Exception e) {
            throw new DeviceListParseException(e);
        }
    }

    private <T extends Device> void devicesFromDocument(Class<T> deviceClass, Map<String,
            RoomDeviceList> roomDeviceListMap, Document document, String tagName) throws IllegalAccessException, InstantiationException {
        
        NodeList nodes = document.getElementsByTagName(tagName);
        for (int i = 0; i < nodes.getLength(); i++) {
            Node item = nodes.item(i);
            deviceFromNode(deviceClass, roomDeviceListMap, item);
        }
    }

    private <T extends Device> void deviceFromNode(Class<T> deviceClass, Map<String, RoomDeviceList> roomDeviceListMap, Node node)
            throws InstantiationException, IllegalAccessException {

        T device = deviceClass.newInstance();
        device.loadXML(node);
        RoomDeviceList roomDeviceList = getOrCreateRoomDeviceList(device.getRoom(), roomDeviceListMap);
        RoomDeviceList allRoomDeviceList = getOrCreateRoomDeviceList(RoomDeviceList.ALL_DEVICES_ROOM, roomDeviceListMap);

        roomDeviceList.addDevice(device);
        allRoomDeviceList.addDevice(device);
    }

    private RoomDeviceList getOrCreateRoomDeviceList(String roomName, Map<String, RoomDeviceList> roomDeviceListMap) {
        if (roomDeviceListMap.containsKey(roomName)) {
            return roomDeviceListMap.get(roomName);
        }
        RoomDeviceList roomDeviceList = new RoomDeviceList(roomName);
        roomDeviceListMap.put(roomName, roomDeviceList);
        return roomDeviceList;
    }

    private void addFileLogsToDevices(Map<String, RoomDeviceList> roomDeviceListMap) {
        RoomDeviceList allDevicesRoom = roomDeviceListMap.get(RoomDeviceList.ALL_DEVICES_ROOM);
        Collection<Device> devices = allDevicesRoom.getAllDevices();

        Collection<FileLog> fileLogDevices = allDevicesRoom.getDevicesOfType(FILE_LOG);
        for (FileLog fileLogDevice : fileLogDevices) {
            addFileLogToDevices(fileLogDevice, devices);
        }
    }

    private void addFileLogToDevices(FileLog fileLogDevice, Collection<Device> devices) {
        for (Device device : devices) {
            if (device.getName().equals(fileLogDevice.getConcerningDevice())) {
                device.setFileLog(fileLogDevice);
                return;
            }
        }
    }
}
