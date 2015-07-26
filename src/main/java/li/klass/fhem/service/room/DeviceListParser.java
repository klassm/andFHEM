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

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import li.klass.fhem.R;
import li.klass.fhem.constants.Actions;
import li.klass.fhem.constants.BundleExtraKeys;
import li.klass.fhem.domain.StatisticsDevice;
import li.klass.fhem.domain.core.DeviceType;
import li.klass.fhem.domain.core.FhemDevice;
import li.klass.fhem.domain.core.RoomDeviceList;
import li.klass.fhem.domain.core.XmllistAttribute;
import li.klass.fhem.domain.log.LogDevice;
import li.klass.fhem.error.ErrorHolder;
import li.klass.fhem.fhem.RequestResult;
import li.klass.fhem.fhem.RequestResultError;
import li.klass.fhem.service.connection.ConnectionService;
import li.klass.fhem.service.deviceConfiguration.DeviceConfiguration;
import li.klass.fhem.service.deviceConfiguration.DeviceConfigurationProvider;
import li.klass.fhem.service.graph.gplot.GPlotDefinition;
import li.klass.fhem.service.graph.gplot.GPlotHolder;
import li.klass.fhem.service.graph.gplot.SvgGraphDefinition;
import li.klass.fhem.service.room.xmllist.DeviceNode;
import li.klass.fhem.service.room.xmllist.XmlListDevice;
import li.klass.fhem.service.room.xmllist.XmlListParser;
import li.klass.fhem.util.StringEscapeUtil;
import li.klass.fhem.util.ValueExtractUtil;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Predicates.notNull;
import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Iterables.concat;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static li.klass.fhem.domain.core.DeviceType.getDeviceTypeFor;
import static li.klass.fhem.util.ReflectionUtil.getAllDeclaredFields;
import static li.klass.fhem.util.ReflectionUtil.getAllDeclaredMethods;


/**
 * Class responsible for reading the current xml list from FHEM.
 */
public class DeviceListParser {

    @Inject
    ConnectionService connectionService;

    @Inject
    DeviceConfigurationProvider deviceConfigurationProvider;

    @Inject
    XmlListParser parser;

    @Inject
    GPlotHolder gPlotHolder;

    private static Logger LOG = LoggerFactory.getLogger(DeviceListParser.class);

    private Map<Class<?>, Map<String, Set<DeviceClassCacheEntry>>> deviceClassCache = newHashMap();

    public RoomDeviceList parseAndWrapExceptions(String xmlList, Context context) {
        try {
            return parseXMLListUnsafe(xmlList, context);
        } catch (Exception e) {
            LOG.error("cannot parse xmllist", e);
            ErrorHolder.setError(e, "cannot parse xmllist, xmllist was: \r\n" + xmlList
                    .replaceAll("<ATTR key=\"globalpassword\" value=\"[^\"]+\"/>", "")
                    .replaceAll("<ATTR key=\"basicAuth\" value=\"[^\"]+\"/>", ""));

            new RequestResult<String>(RequestResultError.DEVICE_LIST_PARSE).handleErrors();
            return null;
        }
    }

    public RoomDeviceList parseXMLListUnsafe(String xmlList, Context context) throws Exception {
        if (xmlList != null) {
            xmlList = xmlList.trim();
        }

        RoomDeviceList allDevicesRoom = new RoomDeviceList(RoomDeviceList.ALL_DEVICES_ROOM);

        if (xmlList == null || "".equals(xmlList)) {
            LOG.error("xmlList is null or blank");
            return allDevicesRoom;
        }

        gPlotHolder.reset();
        Map<String, ImmutableList<XmlListDevice>> parsedDevices = parser.parse(xmlList);

        ReadErrorHolder errorHolder = new ReadErrorHolder();

        Map<String, FhemDevice> allDevices = newHashMap();

        for (Map.Entry<String, ImmutableList<XmlListDevice>> entry : parsedDevices.entrySet()) {
            DeviceType deviceType = getDeviceTypeFor(entry.getKey());
            Optional<DeviceConfiguration> deviceConfiguration = deviceConfigurationProvider.configurationFor(entry.getKey());

            ImmutableList<XmlListDevice> xmlListDevices = parsedDevices.get(entry.getKey());
            if (xmlListDevices == null || xmlListDevices.isEmpty()) {
                continue;
            }

            if (connectionService.mayShowInCurrentConnectionType(deviceType, context)) {

                int localErrorCount = devicesFromDocument(deviceType.getDeviceClass(), xmlListDevices,
                        allDevices, context, deviceConfiguration);

                if (localErrorCount > 0) {
                    errorHolder.addErrors(deviceType, localErrorCount);
                }
            }
        }

        ImmutableSet<SvgGraphDefinition> svgGraphDefinitions = createSvgGraphDefinitions(parsedDevices.get("SVG"), allDevices);
        attachSvgGraphsToDevices(svgGraphDefinitions, allDevices);

        performAfterReadOperations(allDevices, errorHolder);
        RoomDeviceList roomDeviceList = buildRoomDeviceList(allDevices, context);

        handleErrors(errorHolder, context);

        LOG.info("loaded {} devices!", allDevices.size());

        return roomDeviceList;
    }

    private void attachSvgGraphsToDevices(ImmutableSet<SvgGraphDefinition> svgGraphDefinitions, Map<String, FhemDevice> allDevices) {
        for (Map.Entry<String, FhemDevice> entry : allDevices.entrySet()) {
            for (SvgGraphDefinition svgGraphDefinition : svgGraphDefinitions) {
                if (svgGraphDefinition.getLogDevice().concernsDevice(entry.getKey())) {
                    entry.getValue().addSvgGraphDefinition(svgGraphDefinition);
                }
            }
        }
    }

    private ImmutableSet<SvgGraphDefinition> createSvgGraphDefinitions(ImmutableList<XmlListDevice> svgDevices, final Map<String, FhemDevice> allDevices) {
        if (svgDevices == null) return ImmutableSet.of();
        return from(svgDevices).transform(new Function<XmlListDevice, SvgGraphDefinition>() {
            @Override
            public SvgGraphDefinition apply(XmlListDevice input) {
                String gplotFileName = input.getInternals().get("GPLOTFILE").getValue();
                Optional<GPlotDefinition> gPlotDefinitionOptional = gPlotHolder.definitionFor(gplotFileName);
                if (!gPlotDefinitionOptional.isPresent()) {
                    return null;
                }
                String name = input.getInternals().get("NAME").getValue();

                String logDeviceName = input.getInternals().get("LOGDEVICE").getValue();
                if (!allDevices.containsKey(logDeviceName)) {
                    return null;
                }

                // In rare cases we will find devices not being log devices, resulting in
                // ClassCastExceptions. We just want to make sure we only handle LogDevices here.
                FhemDevice logDeviceFhemDevice = allDevices.get(logDeviceName);
                if (!(logDeviceFhemDevice instanceof LogDevice)) {
                    return null;
                }

                LogDevice logDevice = (LogDevice) logDeviceFhemDevice;

                List<String> labels = newArrayList();
                DeviceNode labelsDef = input.getAttributes().get("label");
                if (labelsDef != null) {
                    labels = newArrayList(labelsDef.getValue().replaceAll("\"", "").split(","));
                }

                DeviceNode titleDef = input.getAttributes().get("title");
                String title = titleDef == null ? "" : titleDef.getValue();

                return new SvgGraphDefinition(name, gPlotDefinitionOptional.get(), logDevice, labels, title);
            }
        }).filter(notNull()).toSet();
    }

    private int devicesFromDocument(Class<? extends FhemDevice> deviceClass, ImmutableList<XmlListDevice> xmlListDevices,
                                    Map<String, FhemDevice> allDevices, Context context, Optional<DeviceConfiguration> deviceConfiguration) {

        int errorCount = 0;

        String errorText = "";

        for (XmlListDevice xmlListDevice : xmlListDevices) {
            if (!deviceFromXmlListDevice(deviceClass, xmlListDevice, allDevices, context, deviceConfiguration)) {
                errorCount++;
                errorText += xmlListDevice.toString() + "\r\n\r\n";
            }
        }

        if (errorCount > 0) {
            ErrorHolder.setError("Cannot parse xmlListDevices: \r\n {}" + errorText);
        }

        return errorCount;
    }

    private void performAfterReadOperations(Map<String, FhemDevice> allDevices, ReadErrorHolder errorHolder) {

        List<FhemDevice> allDevicesReadCallbacks = newArrayList();
        List<FhemDevice> deviceReadCallbacks = newArrayList();

        for (FhemDevice device : allDevices.values()) {
            try {
                device.afterAllXMLRead();
                if (device.getDeviceReadCallback() != null) deviceReadCallbacks.add(device);
                if (device.getAllDeviceReadCallback() != null) allDevicesReadCallbacks.add(device);
            } catch (Exception e) {
                allDevices.remove(device.getName());
                errorHolder.addError(getDeviceTypeFor(device));
                LOG.error("cannot perform after read operations", e);
            }
        }

        List<FhemDevice> callbackDevices = newArrayList();
        callbackDevices.addAll(deviceReadCallbacks);
        callbackDevices.addAll(allDevicesReadCallbacks);

        for (FhemDevice device : callbackDevices) {
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
                LOG.error("cannot handle associated devices callbacks", e);
            }
        }
    }

    private RoomDeviceList buildRoomDeviceList(Map<String, FhemDevice> allDevices, Context context) {
        RoomDeviceList roomDeviceList = new RoomDeviceList(RoomDeviceList.ALL_DEVICES_ROOM);
        for (FhemDevice device : allDevices.values()) {
            // We don't want to show log devices in any kind of view. Log devices
            // are already associated with their respective devices during after read
            // operations.
            if (!(device instanceof LogDevice) && !(device instanceof StatisticsDevice)) {
                roomDeviceList.addDevice(device, context);
            }
        }

        return roomDeviceList;
    }

    private void handleErrors(ReadErrorHolder errorHolder, Context context) {
        if (errorHolder.hasErrors()) {
            String errorMessage = context.getString(R.string.errorDeviceListLoad);
            String deviceTypesError = Joiner.on(",").join(errorHolder.getErrorDeviceTypeNames());
            errorMessage = String.format(errorMessage, "" + errorHolder.getErrorCount(), deviceTypesError);

            Intent intent = new Intent(Actions.SHOW_TOAST);
            intent.putExtra(BundleExtraKeys.CONTENT, errorMessage);
            context.sendBroadcast(intent);
        }
    }

    private <T extends FhemDevice> boolean deviceFromXmlListDevice(
            Class<T> deviceClass, XmlListDevice xmlListDevice, Map<String,
            FhemDevice> allDevices, Context context, Optional<DeviceConfiguration> deviceConfiguration) {

        try {
            T device = createAndFillDevice(deviceClass, xmlListDevice, deviceConfiguration);
            if (device == null) {
                return false;
            }

            device.setXmlListDevice(xmlListDevice);
            device.afterDeviceXMLRead(context);

            LOG.debug("loaded device with name " + device.getName());

            allDevices.put(device.getName(), device);

            return true;
        } catch (Exception e) {
            LOG.error("error parsing device", e);
            return false;
        }
    }

    private <T extends FhemDevice> T createAndFillDevice(Class<T> deviceClass, XmlListDevice node, Optional<DeviceConfiguration> deviceConfiguration) throws Exception {
        T device = deviceClass.newInstance();
        device.setDeviceConfiguration(deviceConfiguration.orNull());

        Map<String, Set<DeviceClassCacheEntry>> cache = getDeviceClassCacheEntriesFor(deviceClass);

        Iterable<DeviceNode> children = concat(node.getAttributes().values(), node.getInternals().values(), node.getStates().values(), node.getHeader().values());
        for (DeviceNode child : children) {
            if (child.getKey() == null) continue;

            String sanitisedKey = child.getKey().trim().replaceAll("[-\\.]", "_");
            if (!device.acceptXmlKey(sanitisedKey)) {
                continue;
            }

            String nodeContent = StringEscapeUtil.unescape(child.getValue());

            if (nodeContent.length() == 0) {
                continue;
            }

            invokeDeviceAttributeMethod(cache, device, sanitisedKey, nodeContent, child, child.getType());
        }

        if (device.getName() == null) {
            return null; // just to be sure we don't catch invalid devices ...
        }

        return device;
    }

    @SuppressWarnings("unchecked")
    private <T extends FhemDevice> Map<String, Set<DeviceClassCacheEntry>> getDeviceClassCacheEntriesFor(Class<T> deviceClass) {
        Class<FhemDevice> clazz = (Class<FhemDevice>) deviceClass;
        if (!deviceClassCache.containsKey(clazz)) {
            deviceClassCache.put(clazz, initDeviceCacheEntries(deviceClass));
        }

        return deviceClassCache.get(clazz);
    }

    private <T extends FhemDevice> void invokeDeviceAttributeMethod(Map<String, Set<DeviceClassCacheEntry>> cache, T device, String key,
                                                                    String value, DeviceNode deviceNode, DeviceNode.DeviceNodeType tagName) throws Exception {

        device.onChildItemRead(tagName, key, value, deviceNode);
        handleCacheEntryFor(cache, device, key, value, deviceNode);
    }

    private <T extends FhemDevice> void handleCacheEntryFor(Map<String, Set<DeviceClassCacheEntry>> cache, T device,
                                                            String key, String value, DeviceNode deviceNode) throws Exception {
        key = key.toLowerCase(Locale.getDefault());
        if (cache.containsKey(key)) {
            for (DeviceClassCacheEntry entry : cache.get(key)) {
                entry.invoke(device, deviceNode, value);
            }
        }
    }

    private <T extends FhemDevice> Map<String, Set<DeviceClassCacheEntry>> initDeviceCacheEntries(Class<T> deviceClass) {
        Map<String, Set<DeviceClassCacheEntry>> cache = newHashMap();

        for (Method method : getAllDeclaredMethods(deviceClass)) {
            if (method.isAnnotationPresent(XmllistAttribute.class)) {
                XmllistAttribute annotation = method.getAnnotation(XmllistAttribute.class);
                for (String value : annotation.value()) {
                    addToCache(cache, method, value.toLowerCase(Locale.getDefault()));
                }
            }
        }

        for (Field field : getAllDeclaredFields(deviceClass)) {
            if (field.isAnnotationPresent(XmllistAttribute.class)) {
                XmllistAttribute annotation = field.getAnnotation(XmllistAttribute.class);
                checkArgument(annotation.value().length > 0);

                for (String value : annotation.value()) {
                    addToCache(cache, new DeviceClassFieldEntry(field, value.toLowerCase(Locale.getDefault())));
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

    public void fillDeviceWith(FhemDevice device, Map<String, String> updates, Context context) {
        Map<String, Set<DeviceClassCacheEntry>> cache = getDeviceClassCacheEntriesFor(device.getClass());
        if (cache == null) return;

        for (Map.Entry<String, String> entry : updates.entrySet()) {
            try {
                handleCacheEntryFor(cache, device, entry.getKey(), entry.getValue(),
                        new DeviceNode(DeviceNode.DeviceNodeType.GCM_UPDATE, entry.getKey(), entry.getValue(), null));
                device.getXmlListDevice().getStates().put(entry.getKey(),
                        new DeviceNode(DeviceNode.DeviceNodeType.STATE, entry.getKey(), entry.getValue(), null));
            } catch (Exception e) {
                LOG.error("fillDeviceWith - handle " + entry, e);
            }
        }

        device.afterDeviceXMLRead(context);
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
            if (deviceType != null) {
                addErrors(deviceType, 1);
            }
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

        public abstract void invoke(Object object, DeviceNode node, String value) throws Exception;
    }

    private class DeviceClassMethodEntry extends DeviceClassCacheEntry {

        private final Method method;

        public DeviceClassMethodEntry(Method method, String attribute) {
            super(attribute);
            this.method = method;
            method.setAccessible(true);
        }

        @Override
        public void invoke(Object object, DeviceNode node, String value) throws Exception {
            Class<?>[] parameterTypes = method.getParameterTypes();

            if (parameterTypes.length == 1) {

                if (parameterTypes[0].equals(String.class)) {
                    method.invoke(object, value);
                }

                if ((parameterTypes[0].equals(double.class) || parameterTypes[0].equals(Double.class))) {
                    method.invoke(object, ValueExtractUtil.extractLeadingDouble(value));
                }

                if ((parameterTypes[0].equals(int.class) || parameterTypes[0].equals(Integer.class))) {
                    method.invoke(object, ValueExtractUtil.extractLeadingInt(value));
                }

            } else if (parameterTypes.length == 2
                    && parameterTypes[0].equals(String.class)
                    && parameterTypes[1].equals(DeviceNode.class)
                    && node != null) {
                method.invoke(object, value, node);
            }
        }
    }

    private class DeviceClassFieldEntry extends DeviceClassCacheEntry {
        private final Field field;

        public DeviceClassFieldEntry(Field field, String attribute) {
            super(attribute);
            this.field = field;
            field.setAccessible(true);
        }

        @Override
        public void invoke(Object object, DeviceNode node, String value) throws Exception {
            LOG.debug("setting {} to {}", field.getName(), value);

            if (field.getType().isAssignableFrom(Double.class) || field.getType().isAssignableFrom(double.class)) {
                field.set(object, ValueExtractUtil.extractLeadingDouble(value));
            } else if (field.getType().isAssignableFrom(Integer.class) || field.getType().isAssignableFrom(int.class)) {
                field.set(object, ValueExtractUtil.extractLeadingInt(value));
            } else {
                field.set(object, value);
            }
        }
    }
}
