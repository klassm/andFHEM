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
import org.slf4j.LoggerFactory
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
                else -> allDevices
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
        val plotfunction = plotfunctionListFor(currentDevice)

        return SvgGraphDefinition(currentDevice.name, gPlotDefinition, logDeviceName, labels, title, plotfunction)
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
    }
}
