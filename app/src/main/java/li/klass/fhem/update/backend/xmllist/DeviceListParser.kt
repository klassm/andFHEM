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

import android.content.Context
import android.content.Intent
import li.klass.fhem.R
import li.klass.fhem.connection.backend.RequestResultError
import li.klass.fhem.constants.Actions
import li.klass.fhem.constants.BundleExtraKeys
import li.klass.fhem.domain.core.FhemDevice
import li.klass.fhem.domain.core.RoomDeviceList
import li.klass.fhem.error.ErrorHolder
import li.klass.fhem.graph.backend.gplot.GPlotHolder
import li.klass.fhem.update.backend.device.configuration.Sanitiser
import li.klass.fhem.update.backend.group.GroupProvider
import li.klass.fhem.util.stackTraceAsString
import org.joda.time.DateTime
import org.slf4j.LoggerFactory
import javax.inject.Inject


/**
 * Class responsible for reading the current xml list from FHEM.
 */
class DeviceListParser @Inject constructor(
        private val parser: XmlListParser,
        private val gPlotHolder: GPlotHolder,
        private val groupProvider: GroupProvider,
        private val sanitiser: Sanitiser
) {
    fun parseAndWrapExceptions(xmlList: String, context: Context, connectionId: String): RoomDeviceList? {
        return try {
            parseXMLListUnsafe(xmlList, context, connectionId)
        } catch (e: Exception) {
            LOG.error("cannot parse xmllist", e)
            ErrorHolder.setError(e, "cannot parse xmllist, xmllist was: \r\n" + xmlList
                    .replace("<ATTR key=\"globalpassword\" value=\"[^\"]+\"/>".toRegex(), "")
                    .replace("<ATTR key=\"basicAuth\" value=\"[^\"]+\"/>".toRegex(), ""))

            RequestResultError.DEVICE_LIST_PARSE.handleError(context)
            null
        }
    }

    @Throws(Exception::class)
    fun parseXMLListUnsafe(xmlList: String?, context: Context, connectionId: String): RoomDeviceList {
        val list = (xmlList ?: "").trim { it <= ' ' }
        val allDevicesRoom = RoomDeviceList(RoomDeviceList.ALL_DEVICES_ROOM)

        if ("" == list) {
            LOG.error("xmlList is null or blank")
            return allDevicesRoom
        }

        gPlotHolder.reset(connectionId)
        val parsedDevices = parser.parse(list)

        val allDevices = mutableMapOf<String, FhemDevice>()

        val parseErrors = mutableMapOf<XmlListDevice, Exception>()

        for ((key) in parsedDevices) {
            val xmlListDevices = parsedDevices[key]
            if (xmlListDevices == null || xmlListDevices.isEmpty()) {
                continue
            }

                val functionalityParseErrors = devicesFromDocument(xmlListDevices, allDevices,
                        context)

                parseErrors.putAll(functionalityParseErrors)
        }

        val roomDeviceList = buildRoomDeviceList(allDevices)



        handleErrors(context, parseErrors)

        LOG.info("loaded {} devices!", allDevices.size)

        return roomDeviceList
    }

    private fun devicesFromDocument(xmlListDevices: List<XmlListDevice>, allDevices: MutableMap<String, FhemDevice>,
                                    context: Context): Map<XmlListDevice, Exception> {

        val parseErrors = mutableMapOf<XmlListDevice, Exception>()

        for (xmlListDevice in xmlListDevices) {
            try {
                deviceFromXmlListDevice(xmlListDevice, context)?.let {
                    allDevices[it.name] = it
                }
            } catch (e: Exception) {
                LOG.error("error parsing device $xmlListDevice", e)
                parseErrors[xmlListDevice] = e
            }
        }

        return parseErrors.toMap()
    }

    private fun buildRoomDeviceList(allDevices: Map<String, FhemDevice>): RoomDeviceList {
        val roomDeviceList = RoomDeviceList(RoomDeviceList.ALL_DEVICES_ROOM)
        for (device in allDevices.values) {
            roomDeviceList.addDevice(device)
        }
        return roomDeviceList
    }

    private fun handleErrors(context: Context, parseErrors: MutableMap<XmlListDevice, Exception>) {
        if (parseErrors.isEmpty()) {
            return
        }
        val errorText = parseErrors.map { (device, exception) ->
            "$device =>\n${exception.message}\n${exception.stackTraceAsString()}"
        }.joinToString(separator = "\n\n")
        ErrorHolder.setError(errorText)

        val errorMessage = String.format(context.getString(R.string.errorDeviceListLoad), "${parseErrors.size}")

        context.sendBroadcast(Intent(Actions.SHOW_TOAST)
                .putExtra(BundleExtraKeys.CONTENT, errorMessage)
            .apply { setPackage(context.packageName) })
    }

    private fun deviceFromXmlListDevice(xmlListDevice: XmlListDevice,
                                        context: Context): FhemDevice? {

        if (xmlListDevice.getAttribute("always_hidden") == "true") {
            return null
        }

        val device = FhemDevice(xmlListDevice)

        xmlListDevice.setAttribute("group", groupProvider.functionalityFor(device, context))

        LOG.debug("loaded device with name " + device.name)
        return device
    }

    fun fillDeviceWith(device: FhemDevice, updates: Map<String, String>) {
        for (entry in updates.entries) {
            try {
                device.xmlListDevice.states[entry.key] = sanitiser.sanitise(device.xmlListDevice.type,
                        DeviceNode(DeviceNode.DeviceNodeType.STATE, entry.key, entry.value, DateTime.now())
                )

                if ("STATE".equals(entry.key, ignoreCase = true)) {
                    device.xmlListDevice.internals["STATE"] = sanitiser.sanitise(device.xmlListDevice.type,
                            DeviceNode(DeviceNode.DeviceNodeType.INT, "STATE", entry.value, DateTime.now())
                    )
                }
            } catch (e: Exception) {
                LOG.error("fillDeviceWith - handle $entry", e)
            }

        }
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(DeviceListParser::class.java)
    }
}
