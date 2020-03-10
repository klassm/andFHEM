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

package li.klass.fhem.graph.backend

import com.google.common.base.Optional
import com.google.common.collect.ImmutableSet
import li.klass.fhem.domain.log.ConcernsDevicePredicate
import li.klass.fhem.graph.backend.gplot.GPlotHolder
import li.klass.fhem.graph.backend.gplot.SvgGraphDefinition
import li.klass.fhem.update.backend.DeviceListService
import li.klass.fhem.update.backend.xmllist.XmlListDevice
import org.joda.time.*
import org.slf4j.LoggerFactory
import java.lang.Integer.parseInt
import java.util.*
import java.util.regex.Pattern
import javax.inject.Inject

class GraphDefinitionsForDeviceService @Inject constructor(
        private val deviceListService: DeviceListService,
        private val gPlotHolder: GPlotHolder) {

    fun graphDefinitionsFor(device: XmlListDevice, connectionId: Optional<String>): Set<SvgGraphDefinition> {
        val allDevices = deviceListService.getAllRoomsDeviceList(connectionId.orNull()).allDevicesAsXmllistDevice

        LOGGER.info("graphDefinitionsFor(name={},connection={})", device.name, connectionId.or("--"))
        val graphDefinitions = getGraphDefinitionsFor(allDevices, device)
        for (svgGraphDefinition in graphDefinitions) {
            LOGGER.info("graphDefinitionsFor(name={},connection={}) - found SVG with name {}",
                    device.name, connectionId.or("--"), svgGraphDefinition.name)
        }
        return graphDefinitions
    }

    private fun getGraphDefinitionsFor(allDevices: ImmutableSet<XmlListDevice>, device: XmlListDevice): Set<SvgGraphDefinition> =
            when (device.type) {
                "SVG" -> setOf(toGraphDefinition(allDevices, device))
                else  -> allDevices.asSequence()
                        .filter { it.type == "SVG" }
                        .filter { isSvgForDevice(allDevices, device, it) }
                        .filter { gplotDefinitionExists(allDevices, it) }
                        .map { toGraphDefinition(allDevices, it) }
                        .toSet()
            }

    private fun toGraphDefinition(allDevices: ImmutableSet<XmlListDevice>, currentDevice: XmlListDevice): SvgGraphDefinition {
        val logDeviceName = currentDevice.getInternal("LOGDEVICE")!!
        val gplotFileName = currentDevice.getInternal("GPLOTFILE")!!
        val gPlotDefinition = gPlotHolder.definitionFor(gplotFileName, isConfigDb(allDevices)).get()

        val labels = (currentDevice.getAttribute("label") ?: "")
                .replace("\"".toRegex(), "")
                .split(",".toRegex())
                .dropLastWhile { it.isEmpty() }
                .toList()
        val title = currentDevice.getAttribute("title") ?: ""
        val fixedrange = fixedrangeFor(currentDevice)
        val plotReplace = plotReplaceMapFor(currentDevice)
        val plotfunction = plotfunctionListFor(currentDevice)

        return SvgGraphDefinition(currentDevice.name, gPlotDefinition, logDeviceName, labels, title, fixedrange, plotReplace, plotfunction)
    }

    private fun plotfunctionListFor(device: XmlListDevice): List<String> {
        return (device.getAttribute("plotfunction") ?: "").trim()
                .split(" ".toRegex())
                .filter { it.isNotEmpty() }
    }

    private fun gplotDefinitionExists(allDevices: ImmutableSet<XmlListDevice>, currentDevice: XmlListDevice): Boolean {
        val gplotFileName = currentDevice.getInternal("GPLOTFILE")
        return gplotFileName != null && gPlotHolder.definitionFor(gplotFileName, isConfigDb(allDevices)).isPresent
    }

    private fun isSvgForDevice(allDevices: ImmutableSet<XmlListDevice>, inputDevice: XmlListDevice, svgDevice: XmlListDevice) =
            isRelevantViaLogDevice(svgDevice, allDevices, inputDevice) || isRelevantViaPlotFunction(inputDevice, svgDevice)

    private fun isRelevantViaPlotFunction(inputDevice: XmlListDevice, svgDevice: XmlListDevice): Boolean {
        return plotfunctionListFor(svgDevice)
                .map { it.split(":") }
                .map { it.firstOrNull() }
                .filter { it != null }
                .contains(inputDevice.name)
    }

    private fun isRelevantViaLogDevice(svgDevice: XmlListDevice, allDevices: ImmutableSet<XmlListDevice>, inputDevice: XmlListDevice): Boolean {
        val logDeviceName = svgDevice.getInternal("LOGDEVICE")
        val logDevice = allDevices.firstOrNull { it.name == logDeviceName ?: "" }

        if (logDevice == null || logDevice.type != "FileLog") {
            return false
        }

        val logDeviceRegexp = logDevice.getInternal("REGEXP")
        return logDeviceRegexp != null && ConcernsDevicePredicate.forPattern(logDeviceRegexp).apply(inputDevice)
    }

    private fun isConfigDb(allDevices: ImmutableSet<XmlListDevice>): Boolean {
        return allDevices
                .any { it != null && "configDB" == it.getAttribute("configfile") }
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(GraphDefinitionsForDeviceService::class.java)

        fun fixedrangeFor(device : XmlListDevice): Pair<ReadablePeriod, ReadablePeriod>? {
            val attr = (device.getAttribute("fixedrange") ?: "").trim().split(" ")
            val range = attr[0]
            val offset = if (attr.size > 1 ) { parseInt(attr[1]) } else { 0 }
            val m = Pattern.compile("([0-9]*)(hour|day|week|month|year)s?").matcher(range)
            return if (m.matches()) {
                val count = when {
                    m.group(1).isEmpty() -> 1
                    else -> parseInt(m.group(1))
                }
                when (m.group(2)) {
                    "hour" -> Pair(Hours.hours(count), Hours.hours(count * offset))
                    "day" -> Pair(Days.days(count), Days.days(count * offset))
                    "week" -> Pair(Weeks.weeks(count), Weeks.weeks(count * offset))
                    "month" -> Pair(Months.months(count), Months.months(count * offset))
                    "year" -> Pair(Years.years(count), Years.years(count * offset))
                    else -> null
                }
            } else {
                null
            }
        }

        fun plotReplaceMapFor(device: XmlListDevice): Map<String, String> {
            val attr = (device.getAttribute("plotReplace") ?: "").trim()
            val plotReplace = HashMap<String, String>()
            // Split into single variable definitions respecting quotes and curly braces
            var text = ""
            var quoted = false;
            var braceDepth = 0;
            attr.forEach {
                if (it.isWhitespace() && !quoted && braceDepth == 0) {
                    val char = text.indexOf('=')
                    plotReplace[text.substring(0, char).trim()] = text.substring(char + 1)
                    text = ""
                } else if (it == '"' && braceDepth == 0) {
                    quoted = !quoted
                } else if (it == '{' && !quoted) {
                    braceDepth += 1
                } else if (it == '}' && !quoted) {
                    braceDepth -= 1
                } else {
                    text += it
                }
            }
            if (text.isNotEmpty()) {
                val char = text.indexOf('=')
                plotReplace[text.substring(0, char).trim()] = text.substring(char + 1)
            }
            return plotReplace
        }
    }
}
