package li.klass.fhem.service.device

import android.content.Context
import com.google.common.base.Optional
import com.google.common.collect.ImmutableSet
import li.klass.fhem.domain.log.ConcernsDevicePredicate
import li.klass.fhem.service.graph.gplot.GPlotHolder
import li.klass.fhem.service.graph.gplot.SvgGraphDefinition
import li.klass.fhem.service.room.RoomListService
import li.klass.fhem.service.room.xmllist.XmlListDevice
import org.slf4j.LoggerFactory
import java.util.*
import javax.inject.Inject

class GraphDefinitionsForDeviceService @Inject constructor(
        private val roomListService: RoomListService,
        private val gPlotHolder: GPlotHolder) {

    fun graphDefinitionsFor(context: Context, device: XmlListDevice, connectionId: Optional<String>): Set<SvgGraphDefinition> {
        val allDevices = roomListService.getAllRoomsDeviceList(connectionId, context).allDevicesAsXmllistDevice

        LOGGER.info("graphDefinitionsFor(name={},connection={})", device.name, connectionId.or("--"))
        val graphDefinitions = getGraphDefinitionsFor(allDevices, device, context)
        for (svgGraphDefinition in graphDefinitions) {
            LOGGER.info("graphDefinitionsFor(name={},connection={}) - found SVG with name {}", device.name, connectionId.or("--"), svgGraphDefinition.name)
        }
        return graphDefinitions
    }

    private fun getGraphDefinitionsFor(allDevices: ImmutableSet<XmlListDevice>, device: XmlListDevice, context: Context): Set<SvgGraphDefinition> =
            when (device.type) {
                "SVG" -> setOf(toGraphDefinition(allDevices, context, device))
                else -> allDevices
                        .filter { it.type == "SVG" }
                        .filter { hasConcerningLogDevice(allDevices, device, it) }
                        .filter { gplotDefinitionExists(allDevices, context, it) }
                        .map { toGraphDefinition(allDevices, context, it) }
                        .toSet()
            }

    private fun toGraphDefinition(allDevices: ImmutableSet<XmlListDevice>, context: Context, currentDevice: XmlListDevice): SvgGraphDefinition {
        val logDeviceName = currentDevice.getInternal("LOGDEVICE").get()
        val gplotFileName = currentDevice.getInternal("GPLOTFILE").get()
        val gPlotDefinition = gPlotHolder.definitionFor(gplotFileName, isConfigDb(allDevices), context).get()

        val labels = Arrays.asList(*currentDevice.getAttribute("label")
                .or("").replace("\"".toRegex(), "").split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray())
        val title = currentDevice.getAttribute("title").or("")
        val plotfunction = Arrays.asList(*currentDevice.getAttribute("plotfunction").or("").trim { it <= ' ' }.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray())

        return SvgGraphDefinition(currentDevice.name, gPlotDefinition, logDeviceName, labels, title, plotfunction)
    }

    private fun gplotDefinitionExists(allDevices: ImmutableSet<XmlListDevice>, context: Context, currentDevice: XmlListDevice): Boolean {
        val gplotFileName = currentDevice.getInternal("GPLOTFILE")
        return gplotFileName.isPresent && gPlotHolder.definitionFor(gplotFileName.get(), isConfigDb(allDevices), context).isPresent
    }

    private fun hasConcerningLogDevice(allDevices: ImmutableSet<XmlListDevice>, inputDevice: XmlListDevice, currentDevice: XmlListDevice): Boolean {
        val logDeviceName = currentDevice.getInternal("LOGDEVICE")
        if (!logDeviceName.isPresent) {
            return false
        }

        val logDevice = allDevices.firstOrNull { it.name == logDeviceName.get() }
        logDevice ?: return false

        val logDeviceRegexp = logDevice.getInternal("REGEXP")
        return logDeviceRegexp.isPresent && ConcernsDevicePredicate.forPattern(logDeviceRegexp.get()).apply(inputDevice)
    }

    private fun isConfigDb(allDevices: ImmutableSet<XmlListDevice>): Boolean {
        return allDevices
                .any { it != null && "configDB" == it.getAttribute("configfile").orNull() }
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(GraphDefinitionsForDeviceService::class.java)
    }
}
