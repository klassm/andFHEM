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

package li.klass.fhem.update.backend.xmllist

import com.google.common.collect.ImmutableList
import com.google.common.collect.Iterables
import com.google.common.collect.Lists.newArrayList
import com.google.common.collect.Maps
import li.klass.fhem.update.backend.device.configuration.Sanitiser
import li.klass.fhem.update.backend.xmllist.DeviceNode.DeviceNodeType
import org.joda.time.DateTime
import org.w3c.dom.Document
import org.w3c.dom.NamedNodeMap
import org.w3c.dom.Node
import org.xml.sax.InputSource
import org.xml.sax.SAXException
import java.io.IOException
import java.io.StringReader
import javax.inject.Inject
import javax.inject.Singleton
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.parsers.ParserConfigurationException

@Singleton
class XmlListParser @Inject constructor(
        val sanitiser: Sanitiser
) {

    @Throws(Exception::class)
    fun parse(xmlList: String): Map<String, List<XmlListDevice>> {
        val result = Maps.newHashMap<String, List<XmlListDevice>>()

        // replace device tag extensions
        val list = xmlList
                .replace("_[0-9]+_LIST".toRegex(), "_LIST")
                .replace("(<[/]?[A-Z0-9]+)_[0-9]+([ >])".toRegex(), "$1$2")
                .replace("< [^>]*>".toRegex(), "")
                .replace("</>".toRegex(), "")
                .replace("< name=[a-zA-Z\"=0-9 ]+>".toRegex(), "")
                .replace("\\\\B0".toRegex(), "°")

        val document = documentFromXmlList(list)
        val baseNode = findFHZINFONode(document)

        val childNodes = baseNode.childNodes
        for (i in 0 until childNodes.length) {
            val node = childNodes.item(i)
            if (node.nodeName.endsWith("_LIST")) {
                val devices = handleListNode(node)
                if (devices.isEmpty()) {
                    continue
                }
                val deviceType = devices[0].type
                if (result.containsKey(deviceType)) {
                    // In case we have two LISTs for the same device type, we need to merge
                    // existing lists. FHEM will not send out those lists, but we replace
                    // i.e. SWAP_123_LIST by SWAP_LIST, resulting in two same list names.
                    val existing = result[deviceType]
                    result.put(deviceType, ImmutableList.copyOf(Iterables.concat(existing, devices)))
                } else {
                    result.put(deviceType, devices)
                }
            }
        }

        return result
    }

    private fun findFHZINFONode(document: Document): Node {
        val childNodes = document.childNodes
        for (i in 0 until childNodes.length) {
            val child = childNodes.item(i)
            if (child.nodeName.equals("FHZINFO", ignoreCase = true)) {
                return child
            }
        }
        throw IllegalArgumentException("cannot find FHZINFO")
    }

    @Throws(ParserConfigurationException::class, SAXException::class, IOException::class)
    protected fun documentFromXmlList(xmlList: String): Document {
        val docBuilderFactory = DocumentBuilderFactory.newInstance()
        val docBuilder = docBuilderFactory.newDocumentBuilder()
        return docBuilder.parse(InputSource(StringReader(xmlList)))
    }

    private fun handleListNode(node: Node): List<XmlListDevice> {
        val devices = newArrayList<XmlListDevice>()

        val childNodes = node.childNodes
        for (i in 0 until childNodes.length) {
            val device = handleDeviceNode(childNodes.item(i))
            if (device != null && device.internals.containsKey("NAME")) {
                devices.add(device)
            }
        }

        return devices
    }

    private fun handleDeviceNode(node: Node): XmlListDevice? {
        val nodeAttributes = node.attributes ?: return null


        val attributes = mutableMapOf<String, DeviceNode>()
        val internals = mutableMapOf<String, DeviceNode>()
        val states = mutableMapOf<String, DeviceNode>()
        val headers = mutableMapOf<String, DeviceNode>()

        val childNodes = node.childNodes
        for (i in 0 until childNodes.length) {
            val deviceNode = handleDeviceNodeChild(node.nodeName, childNodes.item(i)) ?: continue

            val key = deviceNode.key
            when (deviceNode.type) {
                DeviceNode.DeviceNodeType.ATTR -> attributes.put(key, deviceNode)
                DeviceNode.DeviceNodeType.INT -> internals.put(key, deviceNode)
                DeviceNode.DeviceNodeType.STATE -> states.put(key, deviceNode)
                else -> {
                }
            }
        }
        addToHeaderIfPresent(nodeAttributes, headers, "sets", node.nodeName)
        addToHeaderIfPresent(nodeAttributes, headers, "attrs", node.nodeName)

        val device = XmlListDevice(
                type = node.nodeName,
                attributes = attributes,
                states = states,
                internals = internals,
                headers = headers)
        sanitiser.sanitise(node.nodeName, device)

        return device
    }

    private fun addToHeaderIfPresent(attributes: NamedNodeMap, headers: MutableMap<String, DeviceNode>, attributeKey: String, deviceType: String) {
        val attribute = attributes.getNamedItem(attributeKey)
        if (attribute != null) {
            headers.put(attributeKey,
                    sanitiser.sanitise(deviceType, DeviceNode(DeviceNodeType.HEADER, attributeKey, attribute.nodeValue, null as DateTime?)))
        }
    }

    private fun handleDeviceNodeChild(deviceType: String, item: Node): DeviceNode? {
        val attributes = item.attributes ?: return null

        val nodeName = item.nodeName

        val nodeType = DeviceNodeType.valueOf(nodeName)
        val key = nodeValueToString(attributes.getNamedItem("key"))
        val value = nodeValueToString(attributes.getNamedItem("value"))
        val measured = nodeValueToString(attributes.getNamedItem("measured"))

        if (key == null || value == null) {
            return null
        }

        return sanitiser.sanitise(deviceType, DeviceNode(nodeType, key, value, measured))
    }

    private fun nodeValueToString(value: Node?): String? = value?.nodeValue?.trim { it <= ' ' }
}
