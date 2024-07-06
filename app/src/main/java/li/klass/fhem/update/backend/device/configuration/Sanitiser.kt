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

package li.klass.fhem.update.backend.device.configuration

import li.klass.fhem.update.backend.device.configuration.sanitise.SanitiseConfiguration
import li.klass.fhem.update.backend.device.configuration.sanitise.SanitiseGeneral
import li.klass.fhem.update.backend.device.configuration.sanitise.SanitiseToAdd
import li.klass.fhem.update.backend.device.configuration.sanitise.SanitiseValue
import li.klass.fhem.update.backend.xmllist.DeviceNode
import li.klass.fhem.update.backend.xmllist.DeviceNode.DeviceNodeType.ATTR
import li.klass.fhem.update.backend.xmllist.DeviceNode.DeviceNodeType.INT
import li.klass.fhem.update.backend.xmllist.DeviceNode.DeviceNodeType.STATE
import li.klass.fhem.update.backend.xmllist.XmlListDevice
import li.klass.fhem.util.ValueDescriptionUtil
import li.klass.fhem.util.ValueExtractUtil.extractLeadingDouble
import li.klass.fhem.util.ValueExtractUtil.extractLeadingInt
import org.apache.commons.lang3.StringUtils
import org.joda.time.DateTime
import org.json.JSONException
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Sanitiser @Inject constructor(
        val deviceConfigurationProvider: DeviceConfigurationProvider
) {
    fun sanitise(deviceType: String, deviceNode: DeviceNode): DeviceNode {
        return try {
            val sanitiseConfiguration = sanitiseConfigurationFor(deviceType) ?: return deviceNode
            sanitise(deviceNode, sanitiseConfiguration)
        } catch (e: Exception) {
            LOGGER.error("cannot sanitise {}", deviceNode, e)
            deviceNode
        }
    }

    fun sanitise(deviceType: String, xmlListDevice: XmlListDevice) {
        try {
            val typeOptions = sanitiseConfigurationFor(deviceType) ?: return
            val generalOptions = typeOptions.general ?: return

            handleGeneral(xmlListDevice, generalOptions)
        } catch (e: JSONException) {
            throw RuntimeException(e)
        }
    }

    private fun handleGeneral(xmlListDevice: XmlListDevice, generalOptions: SanitiseGeneral) {
        addValueIfNotPresent(generalOptions.addAttributesIfNotPresent, ATTR, xmlListDevice.attributes)
        addValueIfNotPresent(generalOptions.addStatesIfNotPresent, STATE, xmlListDevice.states)
        addValueIfNotPresent(generalOptions.addInternalsIfNotPresent, INT, xmlListDevice.internals)
        handleGeneralAttributeAddIfModelDoesNotMatch(xmlListDevice, generalOptions)
    }

    private fun handleGeneralAttributeAddIfModelDoesNotMatch(xmlListDevice: XmlListDevice, generalOptions: SanitiseGeneral) {
        val config = generalOptions.addAttributeIfModelDoesNotMatch ?: return

        val models = config.models
        val model = xmlListDevice.attributes["model"]?.value ?: ""

        if (models.any { it.equals(model, ignoreCase = true) }) {
            return
        }
        xmlListDevice.setAttribute(config.key, config.value)
    }

    private fun addValueIfNotPresent(options: Set<SanitiseToAdd>, nodeType: DeviceNode.DeviceNodeType, deviceValues: MutableMap<String, DeviceNode>) {
        options
                .filter { !deviceValues.containsKey(it.key) }
                .forEach { config ->
                    val toSet = config.value ?: config.withValueOf?.let { deviceValues[it]?.value } ?: ""
                    deviceValues[config.key] = DeviceNode(nodeType, config.key, toSet, null as DateTime?)
                }
    }

    private fun sanitise(deviceNode: DeviceNode, deviceOptions: SanitiseConfiguration): DeviceNode {

        val key = deviceNode.key
        var value = deviceNode.value
        val measured = deviceNode.measured
        val type = deviceNode.type

        val attributeOptions = deviceOptions.values[key]
        attributeOptions ?: return deviceNode

        value = value.replace("&deg;".toRegex(), "°")

        value = handleReplaceAll(attributeOptions, value)
        value = handleExtract(attributeOptions, value)
        value = handleAppend(attributeOptions, value)

        return DeviceNode(type, key, value, measured)
    }

    private fun handleReplaceAll(attributeOptions: SanitiseValue, value: String): String {
        return attributeOptions.replaceAll.fold(value, { acc, replacement ->
            acc.replace(replacement.search.toRegex(), replacement.replaceBy)
        }).trim { it <= ' ' }
    }

    private fun handleAppend(attributeOptions: SanitiseValue, value: String): String {
        return StringUtils.trimToNull(attributeOptions.append)
                ?.let { ValueDescriptionUtil.append(value, it) }
                ?: value
    }

    private fun handleExtract(attributeOptions: SanitiseValue, value: String): String {
        val extract = attributeOptions.extract
        if (!extract.isNullOrEmpty()) {
            when (extract) {
                "double" -> {
                    val extractDigits = attributeOptions.extractDigits ?: 0
                    var result = if (extractDigits != 0)
                        extractLeadingDouble(value, extractDigits)
                    else
                        extractLeadingDouble(value)
                    val divFactor = attributeOptions.extractDivideBy ?: 0
                    if (divFactor != 0) {
                        result = Math.round(result / 1000.0).toDouble()
                    }
                    return result.toString()
                }
                "int" -> return extractLeadingInt(value).toString()
            }
        }
        return value
    }

    private fun sanitiseConfigurationFor(type: String): SanitiseConfiguration? =
            deviceConfigurationProvider.configurationFor(type).sanitiseConfiguration

    companion object {
        private val LOGGER = LoggerFactory.getLogger(Sanitiser::class.java)
    }
}
