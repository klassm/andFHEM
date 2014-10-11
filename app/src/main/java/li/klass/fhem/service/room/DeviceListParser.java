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

import com.google.common.collect.Sets;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.Serializable;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import li.klass.fhem.R;
import li.klass.fhem.constants.Actions;
import li.klass.fhem.constants.BundleExtraKeys;
import li.klass.fhem.dagger.ForApplication;
import li.klass.fhem.domain.StatisticsDevice;
import li.klass.fhem.domain.core.Device;
import li.klass.fhem.domain.core.DeviceType;
import li.klass.fhem.domain.core.RoomDeviceList;
import li.klass.fhem.domain.core.XmllistAttribute;
import li.klass.fhem.domain.log.LogDevice;
import li.klass.fhem.error.ErrorHolder;
import li.klass.fhem.fhem.RequestResult;
import li.klass.fhem.fhem.RequestResultError;
import li.klass.fhem.service.connection.ConnectionService;
import li.klass.fhem.util.StringEscapeUtil;
import li.klass.fhem.util.StringUtil;
import li.klass.fhem.util.XMLUtil;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static li.klass.fhem.domain.core.DeviceType.getDeviceTypeFor;


/**
 * Class responsible for reading the current xml list from FHEM.
 */
public class DeviceListParser {

    public static final String TAG = DeviceListParser.class.getName();

    @Inject
    ConnectionService connectionService;

    @Inject
    @ForApplication
    Context applicationContext;

    private Map<Class<Device>, Map<String, Set<DeviceClassCacheEntry>>> deviceClassCache = newHashMap();

    public RoomDeviceList parseAndWrapExceptions(String xmlList) {
        try {
            return parseXMLListUnsafe(xmlList);
        } catch (Exception e) {
            Log.e(TAG, "cannot parse xmllist", e);
            ErrorHolder.setError(e, "cannot parse xmllist.");

            new RequestResult<String>(RequestResultError.DEVICE_LIST_PARSE).handleErrors();
            return null;
        }
    }

    public RoomDeviceList parseXMLListUnsafe(String xmlList) throws Exception {
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

        Map<String, Device> allDevices = newHashMap();

        DeviceType[] deviceTypes = DeviceType.values();
        for (DeviceType deviceType : deviceTypes) {
            if (connectionService.mayShowInCurrentConnectionType(deviceType)) {
                int localErrorCount = devicesFromDocument(deviceType.getDeviceClass(), document,
                        deviceType.getXmllistTag(), allDevices);

                if (localErrorCount > 0) {
                    errorHolder.addErrors(deviceType, localErrorCount);
                }
            }
        }

        performAfterReadOperations(allDevices, errorHolder);
        RoomDeviceList roomDeviceList = buildRoomDeviceList(allDevices);

        handleErrors(errorHolder);

        Log.i(TAG, "loaded " + allDevices.size() + " devices!");

        return roomDeviceList;
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

    private void performAfterReadOperations(Map<String, Device> allDevices, ReadErrorHolder errorHolder) {

        List<Device> allDevicesReadCallbacks = newArrayList();
        List<Device> deviceReadCallbacks = newArrayList();

        for (Device device : allDevices.values()) {
            try {
                device.afterAllXMLRead();
                if (device.getDeviceReadCallback() != null) deviceReadCallbacks.add(device);
                if (device.getAllDeviceReadCallback() != null) allDevicesReadCallbacks.add(device);
            } catch (Exception e) {
                allDevices.remove(device.getName());
                errorHolder.addError(getDeviceTypeFor(device));
                Log.e(TAG, "cannot perform after read operations", e);
            }
        }

        List<Device> callbackDevices = newArrayList();
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
                errorHolder.addError(getDeviceTypeFor(device));
                Log.e(TAG, "cannot handle associated devices callbacks", e);
            }
        }
    }

    private RoomDeviceList buildRoomDeviceList(Map<String, Device> allDevices) {
        RoomDeviceList roomDeviceList = new RoomDeviceList(RoomDeviceList.ALL_DEVICES_ROOM);
        for (Device device : allDevices.values()) {
            // We don't want to show log devices in any kind of view. Log devices
            // are already associated with their respective devices during after read
            // operations.
            if (!(device instanceof LogDevice) && !(device instanceof StatisticsDevice)) {
                roomDeviceList.addDevice(device);
            }
        }

        return roomDeviceList;
    }

    private void handleErrors(ReadErrorHolder errorHolder) {
        if (errorHolder.hasErrors()) {
            String errorMessage = applicationContext.getString(R.string.errorDeviceListLoad);
            String deviceTypesError = StringUtil.concatenate(errorHolder.getErrorDeviceTypeNames(), ",");
            errorMessage = String.format(errorMessage, "" + errorHolder.getErrorCount(), deviceTypesError);

            Intent intent = new Intent(Actions.SHOW_TOAST);
            intent.putExtra(BundleExtraKeys.CONTENT, errorMessage);
            applicationContext.sendBroadcast(intent);
        }
    }

    /**
     * Instantiates a new device from the given device class. The current {@link Node} to read will be provided to
     * the device, so that it can extract any values.
     *
     * @param deviceClass class to instantiate
     * @param node        current xml node  @return true if everything went well
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
        Map<String, Set<DeviceClassCacheEntry>> cache = getDeviceClassCacheEntriesFor(deviceClass);

        NamedNodeMap attributes = node.getAttributes();
        for (int i = 0; i < attributes.getLength(); i++) {
            Node item = attributes.item(i);
            String name = item.getNodeName().toUpperCase(Locale.getDefault()).replaceAll("[-.]", "_");
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

            String keyValue = originalKey.toUpperCase(Locale.getDefault());
            String nodeContent = StringEscapeUtil.unescape(item.getAttributes().getNamedItem("value").getNodeValue());

            if (nodeContent == null || nodeContent.length() == 0) {
                continue;
            }

            invokeDeviceAttributeMethod(cache, device, keyValue, nodeContent, item.getAttributes(), item.getNodeName());
        }

        return device;
    }

    @SuppressWarnings("unchecked")
    private <T extends Device> Map<String, Set<DeviceClassCacheEntry>> getDeviceClassCacheEntriesFor(Class<T> deviceClass) {
        Class<Device> clazz = (Class<Device>) deviceClass;
        if (!deviceClassCache.containsKey(clazz)) {
            deviceClassCache.put(clazz, initDeviceMethodCacheEntries(deviceClass));
            deviceClassCache.put(clazz, initDeviceMethodCacheEntries(deviceClass));
        }

        return deviceClassCache.get(clazz);
    }

    private <T extends Device> void invokeDeviceAttributeMethod(Map<String, Set<DeviceClassCacheEntry>> cache, T device, String key,
                                                                String value, NamedNodeMap attributes, String tagName) throws Exception {

        device.onChildItemRead(tagName, key, value, attributes);
        if (cache.containsKey(key)) {
            for (DeviceClassCacheEntry entry : cache.get(key)) {
                entry.invoke(device, attributes, tagName, value);
            }
        }
    }

    private <T extends Device> Map<String, Set<DeviceClassCacheEntry>> initDeviceMethodCacheEntries(Class<T> deviceClass) {
        Map<String, Set<DeviceClassCacheEntry>> cache = newHashMap();

        for (Method method : deviceClass.getMethods()) {
            if (method.isAnnotationPresent(XmllistAttribute.class)) {
                XmllistAttribute annotation = method.getAnnotation(XmllistAttribute.class);
                for (String value : annotation.value()) {
                    addToCache(cache, method, value.toUpperCase(Locale.getDefault()));
                }
            } else if (method.getName().startsWith("read")) {
                String attribute = method.getName().substring("read".length());
                addToCache(cache, method, attribute);
            }
        }

        for (Field field : deviceClass.getDeclaredFields()) {
            if (field.isAnnotationPresent(XmllistAttribute.class)) {
                XmllistAttribute annotation = field.getAnnotation(XmllistAttribute.class);
                checkArgument(annotation.value().length > 0);

                for (String value : annotation.value()) {
                    addToCache(cache, new DeviceClassFieldEntry(field, value.toUpperCase(Locale.getDefault())));
                }
            }
        }

        return cache;
    }

    private void addToCache(Map<String, Set<DeviceClassCacheEntry>> cache, Method method, String attribute) {
        addToCache(cache, new DeviceClassMethodEntry(method, attribute));
    }

    private void addToCache(Map<String, Set<DeviceClassCacheEntry>> cache, DeviceClassCacheEntry entry) {
        if (!cache.containsKey(entry.getAttribute())) {
            cache.put(entry.getAttribute(), Sets.<DeviceClassCacheEntry>newHashSet());
        }
        cache.get(entry.getAttribute()).add(entry);
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

            name = name.replaceAll("read", "").replaceAll("gcm", "").toUpperCase(Locale.getDefault());
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
        private Map<DeviceType, Integer> deviceTypeErrorCount = newHashMap();

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

            List<String> errorDeviceTypeNames = newArrayList();
            for (DeviceType deviceType : deviceTypeErrorCount.keySet()) {
                errorDeviceTypeNames.add(deviceType.name());
            }

            return errorDeviceTypeNames;
        }
    }

    private abstract class DeviceClassCacheEntry implements Serializable {
        private final String attribute;

        public DeviceClassCacheEntry(String attribute) {
            this.attribute = attribute;
        }

        public String getAttribute() {
            return attribute;
        }

        public abstract void invoke(Object object, NamedNodeMap attributes, String tagName, String value) throws Exception;
    }

    private class DeviceClassMethodEntry extends DeviceClassCacheEntry {

        private final Method method;

        public DeviceClassMethodEntry(Method method, String attribute) {
            super(attribute);
            this.method = method;
            method.setAccessible(true);
        }

        @Override
        public void invoke(Object object, NamedNodeMap attributes, String tagName, String value) throws Exception {
            Class<?>[] parameterTypes = method.getParameterTypes();
            if (parameterTypes.length == 1 && parameterTypes[0].equals(String.class)) {
                method.invoke(object, value);
            }

            if (attributes != null && parameterTypes.length == 2 && parameterTypes[0].equals(String.class) &&
                    parameterTypes[1].equals(NamedNodeMap.class)) {
                method.invoke(object, value, attributes);
            }

            if (tagName != null && attributes != null && parameterTypes.length == 3 &&
                    parameterTypes[0].equals(String.class) && parameterTypes[1].equals(NamedNodeMap.class) &&
                    parameterTypes[2].equals(String.class)) {
                method.invoke(object, tagName, attributes, value);
            }
        }
    }

    private class DeviceClassFieldEntry extends DeviceClassCacheEntry {
        private final Field field;

        public DeviceClassFieldEntry(Field field, String attribute) {
            super(attribute);
            this.field = field;

            checkArgument(field.getType().isAssignableFrom(String.class));
            field.setAccessible(true);
        }

        @Override
        public void invoke(Object object, NamedNodeMap attributes, String tagName, String value) throws Exception {
            field.set(object, value);
        }
    }
}
