package li.klass.fhem.service.room;

import li.klass.fhem.domain.Device;
import li.klass.fhem.domain.DeviceType;
import li.klass.fhem.domain.FileLog;
import li.klass.fhem.domain.RoomDeviceList;
import li.klass.fhem.exception.DeviceListParseException;
import li.klass.fhem.exception.HostConnectionException;
import li.klass.fhem.fhem.DataConnectionSwitch;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.IOException;
import java.io.StringReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static li.klass.fhem.domain.DeviceType.FILE_LOG;

public class DeviceListParser {

    public static final DeviceListParser INSTANCE = new DeviceListParser();
    
    private DeviceListParser() {}

    public Map<String, RoomDeviceList> listDevices() {

        Map<String, RoomDeviceList> roomDeviceListMap = new HashMap<String, RoomDeviceList>();
        try {
            String xmlList = DataConnectionSwitch.INSTANCE.getCurrentProvider().xmllist();
            xmlList = xmlList.replaceAll("\"<", "\"&lt;");
            xmlList = xmlList.replaceAll(">\"", "&gt;\"");
            xmlList = xmlList.replaceAll("_internal_", "internal");
            xmlList = xmlList.replaceAll("<notify_LIST[\\s\\S]*</notify_LIST>", "");
            xmlList = xmlList.replaceAll("<CUL_IR_LIST>[\\s\\S]*</CUL_IR_LIST>", "");
            xmlList = xmlList.replaceAll("value=\"\"[A-Za-z0-9 ${},]*\"\"", "");
            xmlList = xmlList.replaceAll("</>", "");
            xmlList = xmlList.replaceAll("< [a-zA-Z\"=0-9 ]*>", "");

            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            Document document = docBuilder.parse(new InputSource(new StringReader(xmlList)));

            DeviceType[] deviceTypes = DeviceType.values();
            for (DeviceType deviceType : deviceTypes) {
                devicesFromDocument(deviceType.getDeviceClass(), roomDeviceListMap, document, deviceType.getXmllistTag());
            }

            addFileLogsToDevices(roomDeviceListMap);

            return roomDeviceListMap;
        } catch (HostConnectionException e) {
            throw e;
        } catch (IOException e) {
            throw new RuntimeException(e);
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
            if (device.getName().equals(fileLogDevice.getConcerningDeviceName())) {
                device.setFileLog(fileLogDevice);
                return;
            }
        }
    }
}
