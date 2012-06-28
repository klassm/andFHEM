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

package li.klass.fhem.service.room;

import android.util.Log;
import li.klass.fhem.domain.Device;
import li.klass.fhem.domain.DeviceType;
import li.klass.fhem.domain.FileLogDevice;
import li.klass.fhem.domain.RoomDeviceList;
import li.klass.fhem.exception.AndFHEMException;
import li.klass.fhem.exception.DeviceListParseException;
import li.klass.fhem.exception.HostConnectionException;
import li.klass.fhem.fhem.DataConnectionSwitch;
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

import static li.klass.fhem.domain.DeviceType.FILE_LOG;

/**
 * Class responsible for reading the current xml list from FHEM.
 */
public class DeviceListParser {

    public static final DeviceListParser INSTANCE = new DeviceListParser();

    private DeviceListParser() {}

    /**
     * Reads the current device list, validates it by applying some regular expression replaces and extracts
     * {@link RoomDeviceList} objects.
     * @return Map of room names to their included devices.
     * @throws HostConnectionException if the current FHEM server cannot be contacted
     * @throws DeviceListParseException if the read xml content cannot be parsed
     * @throws RuntimeException if some other exception occurred.
     */
    public Map<String, RoomDeviceList> listDevices() {

        Map<String, RoomDeviceList> roomDeviceListMap = new HashMap<String, RoomDeviceList>();
        RoomDeviceList allDevicesRoom = new RoomDeviceList(RoomDeviceList.ALL_DEVICES_ROOM);

        try {
            String xmlList = DataConnectionSwitch.INSTANCE.getCurrentProvider().xmllist();
            if (xmlList != null) {
                xmlList = xmlList.trim();
            }

            if (xmlList == null || "".equals(xmlList)) {
                Log.e(DeviceListParser.class.getName(), "xmlList is null or blank");
                return roomDeviceListMap;
            }

            xmlList = new String(xmlList.getBytes(), "UTF8");

            // if a newline happens after a set followed by an attrs, both attributes are appended together without
            // adding a whitespace
            xmlList = xmlList.replaceAll("=\"\"attrs", "=\"\" attrs");

            // replace html attribute
            xmlList = xmlList.replaceAll("<ATTR key=\"htmlattr\"[ A-Za-z0-9=\"]*/>", "");

            // remove double ""
            xmlList = xmlList.replaceAll(" +(?= )", "");

            // replace double quotes if not followed by a space or slash
            xmlList = xmlList.replaceAll("\"\"+(?![ /])", "\"");

            xmlList = xmlList.replaceAll("</>", "");
            xmlList = xmlList.replaceAll("< [^>]*>", "");

            //replace values with an unset tag
            xmlList = xmlList.replaceAll("< name=[a-zA-Z\"=0-9 ]+>", "");

            //xmlList = xmlList.replaceAll("<_internal__LIST>[\\s\\S]*</_internal__LIST>", "");
            xmlList = xmlList.replaceAll("<notify_LIST[\\s\\S]*</notify_LIST>", "");
            xmlList = xmlList.replaceAll("<CUL_IR_LIST>[\\s\\S]*</CUL_IR_LIST>", "");
            xmlList = xmlList.replaceAll("<at_LIST>[\\s\\S]*</at_LIST>", "");

            xmlList = xmlList.replaceAll("\"<", "\"&lt;");
            xmlList = xmlList.replaceAll(">\"", "&gt;\"");
            xmlList = xmlList.replaceAll("_internal_", "internal");

            // fix for invalid umlauts
            xmlList = xmlList.replaceAll("&#[\\s\\S]*;", "");

            // remove "" not being preceded by an =
            xmlList = xmlList.replaceAll("(?:[^=])\"\"+", "\"");

            Log.d(DeviceListParser.class.getName(), "xmllist content:\n" + xmlList);

            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            Document document = docBuilder.parse(new InputSource(new StringReader(xmlList)));

            DeviceType[] deviceTypes = DeviceType.values();
            for (DeviceType deviceType : deviceTypes) {
                devicesFromDocument(deviceType.getDeviceClass(), roomDeviceListMap, document, deviceType.getXmllistTag(),
                        allDevicesRoom);
            }

            addFileLogsToDevices(allDevicesRoom);

            return roomDeviceListMap;
        } catch (AndFHEMException e) {
            throw e;
        } catch (Exception e) {
            Log.e(DeviceListParser.class.getName(), "error parsing device list", e);
            throw new DeviceListParseException(e);
        }
    }

    /**
     *
     * @param deviceClass class of the device to read
     * @param roomDeviceListMap room device list map to read the device into.
     * @param document xml document to read
     * @param tagName current tag name to read
     * @param <T> type of device
     * @throws Exception if something went utterly wrong
     */
    private <T extends Device> void devicesFromDocument(Class<T> deviceClass, Map<String,
            RoomDeviceList> roomDeviceListMap, Document document, String tagName, RoomDeviceList allDevicesRoom) throws Exception {

        NodeList nodes = document.getElementsByTagName(tagName);
        for (int i = 0; i < nodes.getLength(); i++) {
            Node item = nodes.item(i);
            deviceFromNode(deviceClass, roomDeviceListMap, item, allDevicesRoom);
        }
    }

    /**
     * Instantiates a new device from the given device class. The current {@link Node} to read will be provided to
     * the device, so that it can extract any values.
     * @param deviceClass class to instantiate
     * @param roomDeviceListMap map used for saving the device
     * @param node current xml node
     * @param <T> specific device type
     * @throws Exception if something went utterly wrong
     */
    private <T extends Device> void deviceFromNode(Class<T> deviceClass, Map<String, RoomDeviceList> roomDeviceListMap,
                                                   Node node, RoomDeviceList allDevicesRoom)
            throws Exception {

        T device = deviceClass.newInstance();
        device.loadXML(node);
        String roomConcatenated = device.getRoom();
        String[] roomParts = roomConcatenated.split(",");
        if (device.isSupported()) {
            for (String room : roomParts) {
                RoomDeviceList roomDeviceList = getOrCreateRoomDeviceList(room, roomDeviceListMap);
                roomDeviceList.addDevice(device);
            }
        }
        allDevicesRoom.addDevice(device);
    }

    /**
     * Returns the {@link RoomDeviceList} if it is already included within the room-device list map. Otherwise,
     * the appropriate list will be created, put into the map and returned.
     * @param roomName room name to look for
     * @param roomDeviceListMap current map including room names and associated device lists.
     * @return matching {@link RoomDeviceList}
     */
    private RoomDeviceList getOrCreateRoomDeviceList(String roomName, Map<String, RoomDeviceList> roomDeviceListMap) {
        if (roomDeviceListMap.containsKey(roomName)) {
            return roomDeviceListMap.get(roomName);
        }
        RoomDeviceList roomDeviceList = new RoomDeviceList(roomName);
        roomDeviceListMap.put(roomName, roomDeviceList);
        return roomDeviceList;
    }

    /**
     * Walks through all {@link li.klass.fhem.domain.FileLogDevice}s and tries to find the matching {@link Device} it
     * is associated to.
     */
    private void addFileLogsToDevices(RoomDeviceList allDevicesRoom) {
        Collection<Device> devices = allDevicesRoom.getAllDevices();

        Collection<FileLogDevice> fileLogDevices = allDevicesRoom.getDevicesOfType(FILE_LOG);
        for (FileLogDevice fileLogDevice : fileLogDevices) {
            addFileLogToDevices(fileLogDevice, devices);
        }
    }

    /**
     * Walks through all devices and tries to find the matching {@link Device} for one given {@link FileLogDevice}.
     * @param fileLogDevice {@link FileLogDevice}, of which the matching {@link Device} is searched
     * @param devices devices to walk through.
     */
    private void addFileLogToDevices(FileLogDevice fileLogDevice, Collection<Device> devices) {
        for (Device device : devices) {
            if (device.getName().equals(fileLogDevice.getConcerningDeviceName())) {
                device.setFileLog(fileLogDevice);
                return;
            }
        }
    }

}
