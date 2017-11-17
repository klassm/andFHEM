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

import com.google.common.base.Strings.isNullOrEmpty
import li.klass.fhem.update.backend.xmllist.DeviceNode
import li.klass.fhem.update.backend.xmllist.DeviceNode.DeviceNodeType.ATTR
import li.klass.fhem.update.backend.xmllist.DeviceNode.DeviceNodeType.STATE
import li.klass.fhem.update.backend.xmllist.XmlListDevice
import li.klass.fhem.util.ValueDescriptionUtil
import li.klass.fhem.util.ValueExtractUtil.extractLeadingDouble
import li.klass.fhem.util.ValueExtractUtil.extractLeadingInt
import org.joda.time.DateTime
import org.json.JSONException
import org.json.JSONObject
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Sanitiser @Inject constructor(
        val deviceConfigurationProvider: DeviceConfigurationProvider
) {
    fun sanitise(deviceType: String, deviceNode: DeviceNode): DeviceNode {
        return try {
            val deviceOptions = optionsFor(deviceType)
            sanitise(deviceNode, deviceOptions)
        } catch (e: Exception) {
            LOGGER.error("cannot sanitise {}", deviceNode)
            deviceNode
        }

    }

    fun sanitise(deviceType: String, xmlListDevice: XmlListDevice) {
        try {
            val typeOptions = optionsFor(deviceType) ?: return
            val generalOptions = typeOptions.optJSONObject("__general__") ?: return

            handleGeneral(xmlListDevice, generalOptions)
        } catch (e: JSONException) {
            throw RuntimeException(e)
        }
    }

    @Throws(JSONException::class)
    private fun handleGeneral(xmlListDevice: XmlListDevice, generalOptions: JSONObject) {
        handleGeneralAttributesIfNotPresent(generalOptions, xmlListDevice)
        handleGeneralStatesIfNotPresent(generalOptions, xmlListDevice)
        handleGeneralAttributeAddIfModelDoesNotMatch(xmlListDevice, generalOptions)
    }

    @Throws(JSONException::class)
    private fun handleGeneralAttributeAddIfModelDoesNotMatch(xmlListDevice: XmlListDevice, generalOptions: JSONObject) {
        val config = generalOptions.optJSONObject("addAttributeIfModelDoesNotMatch") ?: return

        val models = config.getJSONArray("models")
        val modelNode = xmlListDevice.attributes["model"]
        val model = modelNode?.value ?: return

        if ((0 until models.length())
                .map { models.getString(it) }
                .filter { it != null }
                .any { it.equals(model, ignoreCase = true) }) {
            return
        }
        xmlListDevice.setAttribute(config.getString("key"), config.getString("value"))
    }

    @Throws(JSONException::class)
    private fun handleGeneralAttributesIfNotPresent(generalOptions: JSONObject, xmlListDevice: XmlListDevice) {
        val attributes = generalOptions.optJSONArray("addAttributesIfNotPresent") ?: return

        for (i in 0 until attributes.length()) {
            val attribute = attributes.getJSONObject(i)
            val key = attribute.getString("key")
            val value = attribute.getString("value")

            if (!xmlListDevice.attributes.containsKey(key)) {
                xmlListDevice.attributes.put(key, DeviceNode(ATTR, key, value, null as DateTime?))
            }
        }
    }

    @Throws(JSONException::class)
    private fun handleGeneralStatesIfNotPresent(generalOptions: JSONObject, xmlListDevice: XmlListDevice) {
        val states = generalOptions.optJSONArray("addStatesIfNotPresent") ?: return

        for (i in 0 until states.length()) {
            val `object` = states.getJSONObject(i)
            val key = `object`.getString("key")
            val value = `object`.getString("value")

            if (!xmlListDevice.states.containsKey(key)) {
                xmlListDevice.states.put(key, DeviceNode(STATE, key, value, null as DateTime?))
            }
        }
    }

    private fun sanitise(deviceNode: DeviceNode, deviceOptions: JSONObject?): DeviceNode {
        val attributeOptions = deviceOptions!!.optJSONObject(deviceNode.key) ?: return deviceNode

        val key = deviceNode.key
        var value = deviceNode.value
        val measured = deviceNode.measured
        val type = deviceNode.type

        value = value.replace("&deg;".toRegex(), "°")

        value = handleReplaceAll(attributeOptions, value)
        value = handleReplace(attributeOptions, value)
        value = handleExtract(attributeOptions, value)
        value = handleAppend(attributeOptions, value)

        return DeviceNode(type, key, value, measured)
    }

    private fun handleReplaceAll(attributeOptions: JSONObject, value: String): String {
        var toReplace = value
        val replaceAll = attributeOptions.optJSONArray("replaceAll")
        if (replaceAll != null) {
            for (i in 0 until replaceAll.length()) {
                val conf = replaceAll.optJSONObject(i)
                val toSearch = conf.optString("search")
                val searchReplace = conf.optString("replace")

                toReplace = toReplace.replace(toSearch.toRegex(), searchReplace)
            }
        }
        return toReplace.trim { it <= ' ' }
    }

    private fun handleReplace(attributeOptions: JSONObject, value: String): String {
        var toReplace = value
        val replace = attributeOptions.optString("replace")
        var replaceBy: String? = attributeOptions.optString("replaceBy")
        replaceBy = if (replaceBy == null) "" else replaceBy

        if (!isNullOrEmpty(replace)) {
            toReplace = toReplace.replace(replace.toRegex(), replaceBy)
        }


        return toReplace.trim { it <= ' ' }
    }

    private fun handleAppend(attributeOptions: JSONObject, value: String): String {
        var toReplace = value
        val append = attributeOptions.optString("append")
        if (!isNullOrEmpty(append)) {
            toReplace = ValueDescriptionUtil.append(toReplace, append)
        }
        return toReplace
    }

    private fun handleExtract(attributeOptions: JSONObject, value: String): String {
        val extract = attributeOptions.optString("extract")
        if (!isNullOrEmpty(extract)) {
            when (extract) {
                "double" -> {
                    val extractDigits = attributeOptions.optInt("extractDigits", 0)
                    var result = if (extractDigits != 0)
                        extractLeadingDouble(value, extractDigits)
                    else
                        extractLeadingDouble(value)
                    val divFactor = attributeOptions.optInt("extractDivideBy", 0)
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

    private fun optionsFor(type: String): JSONObject? =
            deviceConfigurationProvider.sanitiseConfigurationFor(type).or(JSONObject())

    companion object {
        private val LOGGER = LoggerFactory.getLogger(Sanitiser::class.java)
    }
}
