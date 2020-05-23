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

package li.klass.fhem.adapter.devices.core.deviceItems

import android.content.Context
import li.klass.fhem.R
import li.klass.fhem.domain.core.FhemDevice
import li.klass.fhem.resources.ResourceIdMapper
import li.klass.fhem.update.backend.device.configuration.DeviceConfigurationProvider
import li.klass.fhem.update.backend.device.configuration.DeviceDescMapping
import li.klass.fhem.update.backend.device.configuration.ViewItemConfig
import li.klass.fhem.update.backend.xmllist.DeviceNode
import li.klass.fhem.util.DateFormatUtil
import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class XmlDeviceItemProvider @Inject constructor(
        private val deviceDescMapping: DeviceDescMapping,
        val deviceConfigurationProvider: DeviceConfigurationProvider
) {

    fun getDeviceOverviewItems(fhemDevice: FhemDevice, context: Context): Set<XmlDeviceViewItem> {
        val xmlListDevice = fhemDevice.xmlListDevice

        val configuration = deviceConfigurationProvider.configurationFor(fhemDevice)
        return itemsFor(configuration.states, xmlListDevice.states, false, context) +
                itemsFor(configuration.attributes, xmlListDevice.attributes, false, context) +
                itemsFor(configuration.internals, xmlListDevice.internals, false, context)

    }

    fun getStatesFor(device: FhemDevice, showUnknown: Boolean, context: Context): Set<XmlDeviceViewItem> {
        val configuration = deviceConfigurationProvider.configurationFor(device)
        return itemsFor(configuration.states, device.xmlListDevice.states, showUnknown, context)
    }

    fun getAttributesFor(device: FhemDevice, showUnknown: Boolean, context: Context): Set<XmlDeviceViewItem> {
        val configuration = deviceConfigurationProvider.configurationFor(device)
        return itemsFor(configuration.attributes, device.xmlListDevice.attributes, showUnknown, context)
    }

    fun getInternalsFor(device: FhemDevice, showUnknown: Boolean, context: Context): Set<XmlDeviceViewItem> {
        val configuration = deviceConfigurationProvider.configurationFor(device)
        return itemsFor(configuration.internals, device.xmlListDevice.internals, showUnknown, context)
    }


    private fun itemsFor(configs: Set<ViewItemConfig>, nodes: Map<String, DeviceNode>, showUnknown: Boolean, context: Context): Set<XmlDeviceViewItem> {
        if (showUnknown) {
            return nodes.map { genericItemFor(it.value, context) }.toSet()
        }

        val foundValues = nodes.map { configFor(configs, it.key) to it.value }
                .filter { it.first != null }
                .map { it.first!! to it.second }
        if (foundValues.isEmpty()) {
            return itemsFor(configs, nodes, true, context)
        }
        return foundValues.map { itemFor(it.first, it.second, context) }.toSet()
    }

    private fun configFor(viewItemConfigs: Set<ViewItemConfig>, key: String): ViewItemConfig? {
        return viewItemConfigs
                .firstOrNull { it.key.equals(key, ignoreCase = true) }
    }

    private fun genericItemFor(deviceNode: DeviceNode, context: Context): XmlDeviceViewItem {
        val desc = deviceDescMapping.descFor(deviceNode.key, context)

        return XmlDeviceViewItem(
                deviceNode.key,
                desc,
                deviceNode.value,
                null,
                isShowInDetail = true,
                isShowInOverview = false
        )
    }

    private fun itemFor(config: ViewItemConfig, deviceNode: DeviceNode, context: Context): XmlDeviceViewItem {
        val jsonDesc = StringUtils.trimToNull(config.desc)
        val desc = when (val resource = getResourceIdFor(jsonDesc)) {
            null -> deviceDescMapping.descFor(deviceNode.key, context)
            else -> deviceDescMapping.descFor(resource, context)
        }

        val showAfter = config.showAfter ?: XmlDeviceViewItem.FIRST
        return XmlDeviceViewItem(config.key, desc,
                deviceNode.value, showAfter, config.isShowInDetail, config.isShowInOverview)
    }

    private fun getResourceIdFor(jsonDesc: String?): ResourceIdMapper? {
        return try {
            if (jsonDesc == null) {
                null
            } else ResourceIdMapper.valueOf(jsonDesc)
        } catch (e: Exception) {
            LOGGER.error("getResourceIdFor(jsonDesc=$jsonDesc): cannot find jsonDesc", e)
            null
        }
    }

    private fun mostRecentlyMeasuredNode(fhemDevice: FhemDevice, context: Context): XmlDeviceViewItem? {
        val states = fhemDevice.xmlListDevice.states
        if (states.isEmpty()) return null

        var mostRecent: DeviceNode? = null
        for (node in states.values) {
            if (mostRecent == null || node.measured != null && node.measured.isAfter(mostRecent.measured)) {
                mostRecent = node
            }
        }
        return mostRecent?.measured?.let { XmlDeviceViewItem("measured", context.getString(R.string.measured), DateFormatUtil.ANDFHEM_DATE_TIME_FORMAT.print(it), null, isShowInDetail = false, isShowInOverview = false) }
    }

    fun getMostRecentMeasureTime(fhemDevice: FhemDevice, context: Context): String? =
            mostRecentlyMeasuredNode(fhemDevice, context)
                    ?.value

    companion object {
        private val LOGGER = LoggerFactory.getLogger(XmlDeviceItemProvider::class.java)
    }
}
