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

import com.google.common.collect.Lists;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.StringReader;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import li.klass.fhem.AndFHEMApplication;
import li.klass.fhem.R;
import li.klass.fhem.constants.Actions;
import li.klass.fhem.constants.BundleExtraKeys;
import li.klass.fhem.domain.core.Device;
import li.klass.fhem.domain.core.DeviceType;
import li.klass.fhem.domain.core.RoomDeviceList;
import li.klass.fhem.domain.core.XmllistAttribute;
import li.klass.fhem.error.ErrorHolder;
import li.klass.fhem.fhem.RequestResult;
import li.klass.fhem.fhem.RequestResultError;
import li.klass.fhem.util.StringEscapeUtil;
import li.klass.fhem.util.StringUtil;
import li.klass.fhem.util.XMLUtil;


/**
 * Class responsible for reading the current xml list from FHEM.
 */
public class DeviceListParser {

    public static final DeviceListParser INSTANCE = new DeviceListParser();
    public static final String TAG = DeviceListParser.class.getName();

    private Map<Class<Device>, Map<String, Set<Method>>> deviceClassCache;

    private DeviceListParser() {
    }

    public RoomDeviceList parseAndWrapExceptions(String xmlList) {
        try {
            return parseXMLList(xmlList);
        } catch (Exception e) {
            Log.e(TAG, "cannot parse xmllist", e);
            ErrorHolder.setError(e, "cannot parse xmllist.");

            new RequestResult<String>(RequestResultError.DEVICE_LIST_PARSE).handleErrors();
            return null;
        }
    }

    private RoomDeviceList parseXMLList(String xmlList) throws Exception {
        if (xmlList != null) {
            xmlList = xmlList.trim();
        }

        RoomDeviceList allDevicesRoom = new RoomDeviceList(RoomDeviceList.ALL_DEVICES_ROOM);

        if (xmlList == null || "".equals(xmlList)) {
            Log.e(TAG, "xmlList is null or blank");
            return allDevicesRoom;
        }
        xmlList = validateXmllist(xmlList);


        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
        Document document = docBuilder.parse(new InputSource(new StringReader(xmlList)));

        ReadErrorHolder errorHolder = new ReadErrorHolder();

        Map<String, Device> allDevices = new HashMap<String, Device>();

        DeviceType[] deviceTypes = DeviceType.values();
        for (DeviceType deviceType : deviceTypes) {
            int localErrorCount = devicesFromDocument(deviceType.getDeviceClass(), document,
                    deviceType.getXmllistTag(), allDevices);
            if (localErrorCount > 0) {
                errorHolder.addErrors(deviceType, localErrorCount);
            }
        }


        performAfterReadOperations(allDevices, errorHolder);
        RoomDeviceList roomDeviceList = buildRoomDeviceList(allDevices);

        handleErrors(errorHolder);

        Log.i(TAG, "loaded " + allDevices.size() + " devices!");

        return roomDeviceList;
    }

    private RoomDeviceList buildRoomDeviceList(Map<String, Device> allDevices) {
        RoomDeviceList roomDeviceList = new RoomDeviceList(RoomDeviceList.ALL_DEVICES_ROOM);
        for (Device device : allDevices.values()) {
            roomDeviceList.addDevice(device);
        }

        return roomDeviceList;
    }

    private void handleErrors(ReadErrorHolder errorHolder) {
        if (errorHolder.hasErrors()) {
            Context context = AndFHEMApplication.getContext();
            String errorMessage = context.getString(R.string.errorDeviceListLoad);
            String deviceTypesError = StringUtil.concatenate(errorHolder.getErrorDeviceTypeNames(), ",");
            errorMessage = String.format(errorMessage, "" + errorHolder.getErrorCount(), deviceTypesError);

            Intent intent = new Intent(Actions.SHOW_TOAST);
            intent.putExtra(BundleExtraKeys.CONTENT, errorMessage);
            context.sendBroadcast(intent);
        }
    }

    private String validateXmllist(String xmlList) {
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
        xmlList = xmlList.replaceAll("<CUL_IR_LIST>[\\s\\S]*</CUL_IR_LIST>", "");
        xmlList = xmlList.replaceAll("<autocreate_LIST>[\\s\\S]*</autocreate_LIST>", "");
        xmlList = xmlList.replaceAll("<Global_LIST[\\s\\S]*</Global_LIST>", "");

        xmlList = xmlList.replaceAll("_internal_", "internal");

        // fix for invalid umlauts
        xmlList = xmlList.replaceAll("&#[\\s\\S]*;", "");

        // remove "" not being preceded by an =
        xmlList = xmlList.replaceAll("(?:[^=])\"\"+", "\"");
        xmlList = xmlList.replaceAll("\\\\B0", "°");
        xmlList = xmlList.replaceAll("Â", "");

        // replace xmllist device tag extensions
        xmlList = xmlList.replaceAll("_[0-9]+_LIST", "_LIST");
        xmlList = xmlList.replaceAll("(<[/]?[A-Z0-9]+)_[0-9]+([ >])", "$1$2");

        return xmlList;
    }

    private void performAfterReadOperations(Map<String, Device> allDevices, ReadErrorHolder errorHolder) {

        List<Device> allDevicesReadCallbacks = Lists.newArrayList();
        List<Device> deviceReadCallbacks = Lists.newArrayList();

        for (Device device : allDevices.values()) {
            try {
                device.afterAllXMLRead();
                if (device.getDeviceReadCallback() != null) deviceReadCallbacks.add(device);
                if (device.getAllDeviceReadCallback() != null) allDevicesReadCallbacks.add(device);
            } catch (Exception e) {
                allDevices.remove(device.getName());
                errorHolder.addError(DeviceType.getDeviceTypeFor(device));
                Log.e(TAG, "cannot perform after read operations", e);
            }
        }

        List<Device> callbackDevices = Lists.newArrayList();
        callbackDevices.addAll(deviceReadCallbacks);
        callbackDevices.addAll(allDevicesReadCallbacks);

        for (Device device : callbackDevices) {
            try {
                if (device.getDeviceReadCallback() != null) {
                    device.getDeviceReadCallback().devicesRead(allDevices);
                }
                if (device.getAllDeviceReadCallback() != null) {
                    device.getAllDeviceReadCallback().devicesRead(allDevices);
                }
            } catch (Exception e) {
                allDevices.remove(device.getName());
                errorHolder.addError(DeviceType.getDeviceTypeFor(device));
                Log.e(TAG, "cannot handle associated devices callbacks", e);
            }
        }
    }

    /**
     * @param deviceClass class of the device to read
     * @param document    xml document to read
     * @param tagName     current tag name to read
     * @param <T>         type of device
     * @return error count while parsing the device list
     */
    private <T extends Device> int devicesFromDocument(Class<T> deviceClass, Document document,
                                                       String tagName, Map<String, Device> allDevices) {

        int errorCount = 0;

        NodeList nodes = document.getElementsByTagName(tagName);
        String errorXML = "";

        for (int i = 0; i < nodes.getLength(); i++) {
            Node item = nodes.item(i);

            if (!deviceFromNode(deviceClass, item, allDevices)) {
                errorCount++;
                errorXML += XMLUtil.nodeToString(item) + "\r\n\r\n";
            }
        }

        if (errorCount > 0) {
            ErrorHolder.setError("Cannot parse devices: \r\n" + errorXML);
        }

        return errorCount;
    }

    /**
     * Instantiates a new device from the given device class. The current {@link Node} to read will be provided to
     * the device, so that it can extract any values.
     *
     * @param deviceClass       class to instantiate
     * @param node              current xml node
     * @param <T>               specific device type
     * @return true if everything went well
     */
    private <T extends Device> boolean deviceFromNode(Class<T> deviceClass,
                                                      Node node, Map<String, Device> allDevices) {
        try {
            T device = createAndFillDevice(deviceClass, node);
            device.afterDeviceXMLRead();

            Log.v(TAG, "loaded device with name " + device.getName());

            allDevices.put(device.getName(), device);

            return true;
        } catch (Exception e) {
            Log.e(TAG, "error parsing device", e);
            return false;
        }
    }

    private <T extends Device> T createAndFillDevice(Class<T> deviceClass, Node node) throws Exception {
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

            String originalKey = keyAttribute.getNodeValue().trim().replaceAll("[-\\.]", "_");
            if (!device.acceptXmlKey(originalKey)) {
                continue;
            }

            String keyValue = originalKey.toUpperCase();
            String nodeContent = StringEscapeUtil.unescape(item.getAttributes().getNamedItem("value").getNodeValue());

            if (nodeContent == null || nodeContent.length() == 0) {
                continue;
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
        Class<Device> clazz = (Class<Device>) deviceClass;
        Map<Class<Device>, Map<String, Set<Method>>> cache = getDeviceClassCache();
        if (!cache.containsKey(clazz)) {
            cache.put(clazz, initDeviceClassCacheEntries(deviceClass));
        }

        return cache.get(clazz);
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
            if (method.isAnnotationPresent(XmllistAttribute.class)) {
                XmllistAttribute annotation = method.getAnnotation(XmllistAttribute.class);
                addToCache(cache, method, annotation.value());
            } else if (method.getName().startsWith("read")) {
                String attribute = method.getName().substring("read".length());
                addToCache(cache, method, attribute);
            }
        }

        return cache;
    }

    private void addToCache(Map<String, Set<Method>> cache, Method method, String attribute) {
        if (!cache.containsKey(attribute)) {
            cache.put(attribute, new HashSet<Method>());
        }
        cache.get(attribute).add(method);
        method.setAccessible(true);
    }

    private Map<Class<Device>, Map<String, Set<Method>>> getDeviceClassCache() {
        if (deviceClassCache == null) {
            deviceClassCache = new HashMap<Class<Device>, Map<String, Set<Method>>>();
        }

        return deviceClassCache;
    }

    public void fillDeviceWith(Device device, Map<String, String> updates) {
        Class<? extends Device> deviceClass = device.getClass();

        fillDeviceWith(device, updates, deviceClass);
        device.afterDeviceXMLRead();
    }

    private boolean fillDeviceWith(Device device, Map<String, String> updates, Class<?> deviceClass) {
        Method[] methods = deviceClass.getDeclaredMethods();

        boolean changed = false;

        for (Method method : methods) {
            if (method.getParameterTypes().length != 1) continue;

            String name = method.getName();
            if (!name.startsWith("read") && !name.startsWith("gcm")) continue;

            name = name.replaceAll("read", "").replaceAll("gcm", "").toUpperCase();
            if (updates.containsKey(name)) {
                try {
                    Log.i(TAG, "invoke " + method.getName());
                    method.setAccessible(true);
                    method.invoke(device, updates.get(name));

                    changed = true;
                } catch (Exception e) {
                    Log.e(TAG, "cannot invoke " + method.getName() + " for argument " + updates.get(name));
                }
            }
        }

        if (deviceClass.getSuperclass() != null) {
            return changed | fillDeviceWith(device, updates, deviceClass.getSuperclass());
        } else {
            return changed;
        }
    }

    private class ReadErrorHolder {
        private Map<DeviceType, Integer> deviceTypeErrorCount = new HashMap<DeviceType, Integer>();

        public int getErrorCount() {
            int errors = 0;
            for (Integer deviceTypeErrors : deviceTypeErrorCount.values()) {
                errors += deviceTypeErrors;
            }
            return errors;
        }

        public boolean hasErrors() {
            return deviceTypeErrorCount.size() != 0;
        }

        public void addError(DeviceType deviceType) {
            addErrors(deviceType, 1);
        }

        public void addErrors(DeviceType deviceType, int errorCount) {
            int count = 0;
            if (deviceTypeErrorCount.containsKey(deviceType)) {
                count = deviceTypeErrorCount.get(deviceType);
            }
            deviceTypeErrorCount.put(deviceType, count + errorCount);
        }

        public List<String> getErrorDeviceTypeNames() {
            if (deviceTypeErrorCount.size() == 0) return Collections.emptyList();

            List<String> errorDeviceTypeNames = new ArrayList<String>();
            for (DeviceType deviceType : deviceTypeErrorCount.keySet()) {
                errorDeviceTypeNames.add(deviceType.name());
            }

            return errorDeviceTypeNames;
        }
    }
}
