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
import com.google.common.base.Optional
import li.klass.fhem.domain.core.FhemDevice
import li.klass.fhem.resources.ResourceIdMapper
import li.klass.fhem.update.backend.device.configuration.DeviceDescMapping
import li.klass.fhem.update.backend.device.configuration.ViewItemConfig
import li.klass.fhem.update.backend.xmllist.DeviceNode
import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class XmlDeviceItemProvider @Inject
constructor(val deviceDescMapping: DeviceDescMapping) {

    fun getDeviceClassItems(fhemDevice: FhemDevice, context: Context): Set<DeviceViewItem> {
        val xmlListDevice = fhemDevice.xmlListDevice ?: return emptySet()

        val configuration = fhemDevice.deviceConfiguration

        return itemsFor(configuration.states, xmlListDevice.states, false, context) +
                itemsFor(configuration.attributes, xmlListDevice.attributes, false, context)
    }

    fun getStatesFor(device: FhemDevice, showUnknown: Boolean, context: Context): Set<DeviceViewItem> =
            itemsFor(device.deviceConfiguration.states, device.xmlListDevice.states, showUnknown, context)

    fun getAttributesFor(device: FhemDevice, showUnknown: Boolean, context: Context): Set<DeviceViewItem> =
            itemsFor(device.deviceConfiguration.attributes, device.xmlListDevice.attributes, showUnknown, context)

    fun getInternalsFor(device: FhemDevice, showUnknown: Boolean, context: Context): Set<DeviceViewItem> =
            itemsFor(device.deviceConfiguration.internals, device.xmlListDevice.internals, showUnknown, context)


    private fun itemsFor(configs: Set<ViewItemConfig>, nodes: Map<String, DeviceNode>, showUnknown: Boolean, context: Context): Set<DeviceViewItem> {
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

    private fun genericItemFor(deviceNode: DeviceNode, context: Context): DeviceViewItem {
        val desc = deviceDescMapping.descFor(deviceNode.key, context)

        return XmlDeviceViewItem(deviceNode.key, desc,
                deviceNode.value, null, true, false)
    }

    private fun itemFor(config: ViewItemConfig, deviceNode: DeviceNode, context: Context): DeviceViewItem {
        val jsonDesc = StringUtils.trimToNull(config.desc)
        val resource = getResourceIdFor(jsonDesc)
        val desc = when {
            resource.isPresent -> deviceDescMapping.descFor(resource.get(), context)
            else -> deviceDescMapping.descFor(deviceNode.key, context)
        }

        val showAfter = config.showAfter ?: DeviceViewItem.FIRST
        return XmlDeviceViewItem(config.key, desc,
                deviceNode.value, showAfter, config.isShowInDetail, config.isShowInOverview)
    }

    private fun getResourceIdFor(jsonDesc: String?): Optional<ResourceIdMapper> {
        try {
            return if (jsonDesc == null) {
                Optional.absent()
            } else Optional.of(ResourceIdMapper.valueOf(jsonDesc))
        } catch (e: Exception) {
            LOGGER.error("getResourceIdFor(jsonDesc=$jsonDesc): cannot find jsonDesc", e)
            return Optional.absent()
        }

    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(XmlDeviceItemProvider::class.java)
    }
}
