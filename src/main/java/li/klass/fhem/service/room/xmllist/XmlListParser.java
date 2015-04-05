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

package li.klass.fhem.service.room.xmllist;

import android.support.annotation.NonNull;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.ImmutableMap.copyOf;
import static com.google.common.collect.Lists.newArrayList;
import static li.klass.fhem.service.room.xmllist.DeviceNode.DeviceNodeType;

@Singleton
public class XmlListParser {
    private static final Function<MutableXmlListDevice, XmlListDevice> TO_XMLLIST_DEVICE = new Function<MutableXmlListDevice, XmlListDevice>() {
        @NonNull
        @Override
        public XmlListDevice apply(MutableXmlListDevice input) {
            return new XmlListDevice(input.type, copyOf(input.attributes), copyOf(input.states), copyOf(input.internals), copyOf(input.header));
        }
    };

    @Inject
    Sanitiser sanitiser;


    public Map<String, ImmutableList<XmlListDevice>> parse(String xmlList) throws Exception {
        Map<String, ImmutableList<XmlListDevice>> result = Maps.newHashMap();

        // replace device tag extensions
        xmlList = xmlList.replaceAll("_[0-9]+_LIST", "_LIST");
        xmlList = xmlList.replaceAll("(<[/]?[A-Z0-9]+)_[0-9]+([ >])", "$1$2");

        Document document = documentFromXmlList(xmlList);
        Node baseNode = findFHZINFONode(document);

        NodeList childNodes = baseNode.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node node = childNodes.item(i);
            if (node.getNodeName().endsWith("_LIST")) {
                ImmutableList<XmlListDevice> devices = handleListNode(node);
                if (devices.isEmpty()) {
                    continue;
                }
                String deviceType = devices.get(0).getType().toLowerCase(Locale.getDefault());
                if (result.containsKey(deviceType)) {
                    // In case we have two LISTs for the same device type, we need to merge
                    // existing lists. FHEM will not send out those lists, but we replace
                    // i.e. SWAP_123_LIST by SWAP_LIST, resulting in two same list names.
                    Iterable<XmlListDevice> existing = result.get(deviceType);
                    result.put(deviceType, ImmutableList.copyOf(Iterables.concat(existing, devices)));
                } else {
                    result.put(deviceType, devices);
                }
            }
        }

        return ImmutableMap.copyOf(result);
    }

    private Node findFHZINFONode(Document document) {
        NodeList childNodes = document.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node child = childNodes.item(i);
            if (child.getNodeName().equalsIgnoreCase("FHZINFO")) {
                return child;
            }
        }
        throw new IllegalArgumentException("cannot find FHZINFO");
    }

    protected Document documentFromXmlList(String xmlList) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
        return docBuilder.parse(new InputSource(new StringReader(xmlList)));
    }

    private ImmutableList<XmlListDevice> handleListNode(Node node) {
        List<MutableXmlListDevice> devices = newArrayList();

        NodeList childNodes = node.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            MutableXmlListDevice device = handleDeviceNode(childNodes.item(i));
            if (device != null && device.internals.containsKey("NAME")) {
                devices.add(device);
            }
        }

        return from(devices).transform(TO_XMLLIST_DEVICE).toList();
    }

    private MutableXmlListDevice handleDeviceNode(Node node) {
        NamedNodeMap attributes = node.getAttributes();
        if (attributes == null) return null;

        MutableXmlListDevice device = new MutableXmlListDevice(node.getNodeName());

        NodeList childNodes = node.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            DeviceNode deviceNode = handleDeviceNodeChild(node.getNodeName(), childNodes.item(i));
            if (deviceNode == null) continue;

            String key = deviceNode.getKey();
            switch (deviceNode.getType()) {
                case ATTR:
                    device.attributes.put(key, deviceNode);
                    break;
                case INT:
                    device.internals.put(key, deviceNode);
                    break;
                case STATE:
                    device.states.put(key, deviceNode);
                    break;
                default:
            }
        }
        addToHeaderIfPresent(attributes, device, "sets");
        addToHeaderIfPresent(attributes, device, "attrs");

        return device;
    }

    private void addToHeaderIfPresent(NamedNodeMap attributes, MutableXmlListDevice device, String attributeKey) {
        Node attribute = attributes.getNamedItem(attributeKey);
        if (attribute != null) {
            device.header.put(attributeKey, new DeviceNode(DeviceNodeType.HEADER, attributeKey, attribute.getNodeValue(), null));
        }
    }

    private DeviceNode handleDeviceNodeChild(String deviceType, Node item) {
        NamedNodeMap attributes = item.getAttributes();
        if (attributes == null) return null;

        String nodeName = item.getNodeName();

        DeviceNodeType nodeType = DeviceNodeType.valueOf(nodeName);
        String key = nodeValueToString(attributes.getNamedItem("key"));
        String value = nodeValueToString(attributes.getNamedItem("value"));
        String measured = nodeValueToString(attributes.getNamedItem("measured"));

        return sanitiser.sanitise(deviceType, new DeviceNode(nodeType, key, value, measured));
    }

    private String nodeValueToString(Node value) {
        return value == null ? null : value.getNodeValue();
    }
}
