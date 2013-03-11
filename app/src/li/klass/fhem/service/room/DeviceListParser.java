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

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import li.klass.fhem.AndFHEMApplication;
import li.klass.fhem.R;
import li.klass.fhem.constants.Actions;
import li.klass.fhem.constants.BundleExtraKeys;
import li.klass.fhem.domain.FileLogDevice;
import li.klass.fhem.domain.core.Device;
import li.klass.fhem.domain.core.DeviceType;
import li.klass.fhem.domain.core.RoomDeviceList;
import li.klass.fhem.exception.AndFHEMException;
import li.klass.fhem.exception.DeviceListParseException;
import li.klass.fhem.exception.HostConnectionException;
import li.klass.fhem.fhem.DataConnectionSwitch;
import li.klass.fhem.util.StringEscapeUtil;
import li.klass.fhem.util.StringUtil;
import org.w3c.dom.*;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.lang.reflect.Method;
import java.util.*;

import static li.klass.fhem.domain.core.DeviceType.FILE_LOG;


/**
 * Class responsible for reading the current xml list from FHEM.
 */
public class DeviceListParser {

    public static final DeviceListParser INSTANCE = new DeviceListParser();
    public static final String TAG = DeviceListParser.class.getName();

    private Map<Class<Device>, Map<String, Set<Method>>> deviceClassCache;

    private DeviceListParser() {
    }

    /**
     * Reads the current device list, validates it by applying some regular expression replaces and extracts
     * {@link RoomDeviceList} objects.
     *
     * @return Map of room names to their included devices.
     * @throws HostConnectionException  if the current FHEM server cannot be contacted
     * @throws DeviceListParseException if the read xml content cannot be parsed
     * @throws RuntimeException         if some other exception occurred.
     */
    public Map<String, RoomDeviceList> listDevices() {
        Log.i(TAG, "fetching devices for xmllist parsing ...");

        try {
            String xmlList = DataConnectionSwitch.INSTANCE.getCurrentProvider().xmllist();
            if (xmlList != null) {
                xmlList = xmlList.trim();
            }
            Log.d(TAG, "fetched xmllist :\n" + xmlList);

            return parseXMLList(xmlList);
        } catch (AndFHEMException e) {
            throw e;
        } catch (Exception e) {
            Log.e(DeviceListParser.class.getName(), "error parsing device list", e);
            throw new DeviceListParseException(e);
        }
    }

    public Map<String, RoomDeviceList> parseXMLList(String xmlList) throws Exception {
        Map<String, RoomDeviceList> roomDeviceListMap = new HashMap<String, RoomDeviceList>();
        RoomDeviceList allDevicesRoom = new RoomDeviceList(RoomDeviceList.ALL_DEVICES_ROOM);

        if (xmlList == null || "".equals(xmlList)) {
            Log.e(TAG, "xmlList is null or blank");
            return roomDeviceListMap;
        }

        // if a newline happens after a set followed by an attrs, both attributes are appended together without
        // adding a whitespace
        xmlList = xmlList.replaceAll("=\"\"attrs", "=\"\" attrs");

        // replace html attribute
        xmlList = xmlList.replaceAll("<ATTR key=\"htmlattr\"[ A-Za-z0-9=\"]*/>", "");

        xmlList = xmlList.replaceAll("</>", "");
        xmlList = xmlList.replaceAll("< [^>]*>", "");

        //replace values with an unset tag
        xmlList = xmlList.replaceAll("< name=[a-zA-Z\"=0-9 ]+>", "");

        xmlList = xmlList.replaceAll("<_internal__LIST>[\\s\\S]*</_internal__LIST>", "");
        xmlList = xmlList.replaceAll("<notify_LIST[\\s\\S]*</notify_LIST>", "");
        xmlList = xmlList.replaceAll("<weblink_LIST[\\s\\S]*</weblink_LIST>", "");
        xmlList = xmlList.replaceAll("<CUL_IR_LIST>[\\s\\S]*</CUL_IR_LIST>", "");
        xmlList = xmlList.replaceAll("<autocreate_LIST>[\\s\\S]*</autocreate_LIST>", "");
        xmlList = xmlList.replaceAll("<Global_LIST[\\s\\S]*</Global_LIST>", "");

        xmlList = xmlList.replaceAll("_internal_", "internal");

        // fix for invalid umlauts
        xmlList = xmlList.replaceAll("&#[\\s\\S]*;", "");

        // remove "" not being preceded by an =
        xmlList = xmlList.replaceAll("(?:[^=])\"\"+", "\"");
        xmlList = xmlList.replaceAll("\\\\B0", "°");

        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
        Document document = docBuilder.parse(new InputSource(new StringReader(xmlList)));

        DeviceType[] deviceTypes = DeviceType.values();
        int errorCount = 0;
        ArrayList<String> errorDeviceTypes = new ArrayList<String>();
        for (DeviceType deviceType : deviceTypes) {
            int localErrorCount = devicesFromDocument(deviceType.getDeviceClass(), roomDeviceListMap, document, deviceType.getXmllistTag(),
                    allDevicesRoom);
            if (localErrorCount > 0) {
                errorDeviceTypes.add(deviceType.name());
                errorCount += localErrorCount;
            }
        }

        if (errorCount > 0) {
            Context context = AndFHEMApplication.getContext();
            String errorMessage = context.getString(R.string.errorDeviceListLoad);
            String deviceTypesError = StringUtil.concatenate(errorDeviceTypes.toArray(new String[errorDeviceTypes.size()]), ",");
            errorMessage = String.format(errorMessage, "" + errorCount, deviceTypesError);

            Intent intent = new Intent(Actions.SHOW_TOAST);
            intent.putExtra(BundleExtraKeys.CONTENT, errorMessage);
            context.sendBroadcast(intent);
        }

        addFileLogsToDevices(allDevicesRoom);
        performAfterReadOperations(allDevicesRoom, roomDeviceListMap);

        Log.e(TAG, "loaded " + allDevicesRoom.getAllDevices().size() + " devices!");

        return roomDeviceListMap;
    }

    private void performAfterReadOperations(RoomDeviceList allDevicesRoom, Map<String, RoomDeviceList> roomDeviceListMap) {
        for (Device device : allDevicesRoom.getAllDevices()) {
            device.afterXMLRead();
            removeIfUnsupported(device, allDevicesRoom, roomDeviceListMap);
        }
    }

    private void removeIfUnsupported(Device device, RoomDeviceList allDevicesRoom, Map<String, RoomDeviceList> roomDeviceListMap) {
        if (device.isSupported()) return;

        for (String room : device.getRooms()) {
            RoomDeviceList roomDeviceList = roomDeviceListMap.get(room);
            roomDeviceList.removeDevice(device);
        }

        allDevicesRoom.removeDevice(device);
    }

    /**
     * @param deviceClass       class of the device to read
     * @param roomDeviceListMap rooms device list map to read the device into.
     * @param document          xml document to read
     * @param tagName           current tag name to read
     * @param <T>               type of device
     * @return error count while parsing the device list
     */
    private <T extends Device> int devicesFromDocument(Class<T> deviceClass, Map<String,
            RoomDeviceList> roomDeviceListMap, Document document, String tagName, RoomDeviceList allDevicesRoom) {

        int errorCount = 0;

        NodeList nodes = document.getElementsByTagName(tagName);
        for (int i = 0; i < nodes.getLength(); i++) {
            Node item = nodes.item(i);

            if (!deviceFromNode(deviceClass, roomDeviceListMap, item, allDevicesRoom)) {
                errorCount++;
            }
        }

        return errorCount;
    }

    /**
     * Instantiates a new device from the given device class. The current {@link Node} to read will be provided to
     * the device, so that it can extract any values.
     *
     * @param deviceClass       class to instantiate
     * @param roomDeviceListMap map used for saving the device
     * @param node              current xml node
     * @param <T>               specific device type
     * @return true if everything went well
     */
    private <T extends Device> boolean deviceFromNode(Class<T> deviceClass, Map<String, RoomDeviceList> roomDeviceListMap,
                                                      Node node, RoomDeviceList allDevicesRoom) {
        try {
            T device = createAndFillDevice(deviceClass, node, allDevicesRoom);
            Log.d(TAG, "loaded device with name " + device.getName());

            String[] rooms = device.getRooms();
            for (String room : rooms) {
                RoomDeviceList roomDeviceList = getOrCreateRoomDeviceList(room, roomDeviceListMap);
                roomDeviceList.addDevice(device);
            }
            allDevicesRoom.addDevice(device);

            return true;
        } catch (Exception e) {
            Log.e(TAG, "error parsing device", e);
            return false;
        }
    }

    /**
     * Returns the {@link RoomDeviceList} if it is already included within the room-device list map. Otherwise,
     * the appropriate list will be created, put into the map and returned.
     *
     * @param roomName          room name
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
     *
     * @param fileLogDevice {@link FileLogDevice}, of which the matching {@link Device} is searched
     * @param devices       devices to walk through.
     */
    private void addFileLogToDevices(FileLogDevice fileLogDevice, Collection<Device> devices) {
        for (Device device : devices) {
            if (device.getName().equals(fileLogDevice.getConcerningDeviceName())) {
                device.setFileLog(fileLogDevice);
                return;
            }
        }
    }

    private <T extends Device> T createAndFillDevice(Class<T> deviceClass, Node node, RoomDeviceList allDevicesRoom) throws Exception {
        T device = deviceClass.newInstance();
        Map<String, Set<Method>> cache = getDeviceClassCacheEntriesFor(deviceClass);

        NamedNodeMap attributes = node.getAttributes();
        for (int i = 0; i < attributes.getLength(); i++) {
            Node item = attributes.item(i);
            String name = item.getNodeName().toUpperCase().replaceAll("[-.]", "_");
            String value = StringEscapeUtil.unescape(item.getNodeValue());

            device.onAttributeRead(name, value);
        }

        NodeList childNodes = node.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node item = childNodes.item(i);
            if (item == null || item.getAttributes() == null) continue;

            Node keyAttribute = item.getAttributes().getNamedItem("key");
            if (keyAttribute == null) continue;

            String keyValue = keyAttribute.getNodeValue().toUpperCase().trim().replaceAll("[-\\.]", "_");
            String nodeContent = StringEscapeUtil.unescape(item.getAttributes().getNamedItem("value").getNodeValue());

            if (nodeContent == null || nodeContent.length() == 0) {
                continue;
            }

            if (keyValue.equalsIgnoreCase("device")) {
                device.setAssociatedDeviceCallback(new AssociatedDeviceCallback(nodeContent, allDevicesRoom));
            }

            invokeDeviceAttributeMethod(cache, device, keyValue, nodeContent, item.getAttributes(), item.getNodeName());
        }

        return device;
    }

    private <T extends Device> void invokeDeviceAttributeMethod(Map<String, Set<Method>> cache, T device, String key,
                                                                String value, NamedNodeMap attributes, String tagName) throws Exception {

        device.onChildItemRead(tagName, key, value, attributes);
        if (!cache.containsKey(key)) return;
        Set<Method> availableMethods = cache.get(key);
        for (Method availableMethod : availableMethods) {
            Class<?>[] parameterTypes = availableMethod.getParameterTypes();
            if (parameterTypes.length == 1 && parameterTypes[0].equals(String.class)) {
                availableMethod.invoke(device, value);
            }

            if (attributes != null && parameterTypes.length == 2 && parameterTypes[0].equals(String.class) &&
                    parameterTypes[1].equals(NamedNodeMap.class)) {
                availableMethod.invoke(device, value, attributes);
            }

            if (tagName != null && attributes != null && parameterTypes.length == 3 &&
                    parameterTypes[0].equals(String.class) && parameterTypes[1].equals(NamedNodeMap.class) &&
                    parameterTypes[2].equals(String.class)) {
                availableMethod.invoke(device, tagName, attributes, value);
            }
        }
    }


    @SuppressWarnings("unchecked")
    private <T extends Device> Map<String, Set<Method>> getDeviceClassCacheEntriesFor(Class<T> deviceClass) {
        Map<Class<Device>, Map<String, Set<Method>>> cache = getDeviceClassCache();
        if (!cache.containsKey(deviceClass)) {
            cache.put((Class<Device>) deviceClass, initDeviceClassCacheEntries(deviceClass));
        }

        return cache.get(deviceClass);
    }

    /**
     * Loads an initial map of method names (that are parsed to attribute names) incl. the methods polymorphic
     * method parameters.
     *
     * @param deviceClass class of the device
     * @return map of device methods
     */
    private <T extends Device> Map<String, Set<Method>> initDeviceClassCacheEntries(Class<T> deviceClass) {
        Map<String, Set<Method>> cache = new HashMap<String, Set<Method>>();
        Method[] methods = deviceClass.getMethods();
        for (Method method : methods) {
            String methodName = method.getName();
            if (!methodName.startsWith("read")) continue;

            String attributeName = methodName.substring("read".length());
            if (!cache.containsKey(attributeName)) {
                cache.put(attributeName, new HashSet<Method>());
            }
            cache.get(attributeName).add(method);
            method.setAccessible(true);
        }

        return cache;
    }

    private Map<Class<Device>, Map<String, Set<Method>>> getDeviceClassCache() {
        if (deviceClassCache == null) {
            deviceClassCache = new HashMap<Class<Device>, Map<String, Set<Method>>>();
        }

        return deviceClassCache;
    }

    public void parseEvent(String event) throws Exception {
        String[] split = event.split(" ", 5);
        if (split.length == 5) {
            String devName = split[3];
            String measured = split[0] + " " + split[1];
            String value = split[4];
            String state;

            Device device = RoomListService.INSTANCE.getDeviceForName(devName,
                    RoomListService.NEVER_UPDATE_PERIOD);
            if (device != null) {
                Map<String, Set<Method>> cache = getDeviceClassCacheEntriesFor(device.getClass());

                if (!value.contains(":")) {
                    state = "STATE";
                } else {
                    String[] stateTest = value.split(":", 2);
                    stateTest[0] = stateTest[0].replaceAll("[-.]", "_").toUpperCase();
                    if (cache.containsKey(stateTest[0])) {
                        state = stateTest[0];
                        value = stateTest[1].trim();
                    } else {
                        state = "STATE";
                    }
                }

                Log.d(TAG, "new state for " + device.getName() + " " + state
                        + ": " + value);

                DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
                Document doc = docBuilder.newDocument();
                Element e = doc.createElement("anything");

                invokeDeviceAttributeMethod(cache, device, state, value, e.getAttributes(), "INT");
                device.readMEASURED(measured);
            }
        }
    }

}
