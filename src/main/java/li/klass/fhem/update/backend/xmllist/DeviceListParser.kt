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
import com.google.common.base.Joiner
import com.google.common.base.Optional
import com.google.common.base.Preconditions.checkArgument
import com.google.common.collect.Iterables.concat
import com.google.common.collect.Lists.newArrayList
import com.google.common.collect.Maps.newHashMap
import li.klass.fhem.R
import li.klass.fhem.connection.backend.ConnectionService
import li.klass.fhem.connection.backend.RequestResult
import li.klass.fhem.connection.backend.RequestResultError
import li.klass.fhem.constants.Actions
import li.klass.fhem.constants.BundleExtraKeys
import li.klass.fhem.domain.GenericDevice
import li.klass.fhem.domain.core.DeviceType
import li.klass.fhem.domain.core.DeviceType.getDeviceTypeFor
import li.klass.fhem.domain.core.FhemDevice
import li.klass.fhem.domain.core.RoomDeviceList
import li.klass.fhem.domain.core.XmllistAttribute
import li.klass.fhem.error.ErrorHolder
import li.klass.fhem.graph.backend.gplot.GPlotHolder
import li.klass.fhem.update.backend.device.configuration.DeviceConfiguration
import li.klass.fhem.update.backend.device.configuration.DeviceConfigurationProvider
import li.klass.fhem.update.backend.device.configuration.Sanitiser
import li.klass.fhem.update.backend.group.GroupProvider
import li.klass.fhem.util.ReflectionUtil.getAllDeclaredFields
import li.klass.fhem.util.ReflectionUtil.getAllDeclaredMethods
import li.klass.fhem.util.StringEscapeUtil
import li.klass.fhem.util.ValueExtractUtil
import org.joda.time.DateTime
import org.slf4j.LoggerFactory
import java.io.Serializable
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.util.*
import javax.inject.Inject


/**
 * Class responsible for reading the current xml list from FHEM.
 */
class DeviceListParser @Inject
constructor(
        private val connectionService: ConnectionService,
        private val deviceConfigurationProvider: DeviceConfigurationProvider,
        private val parser: XmlListParser,
        private val gPlotHolder: GPlotHolder,
        private val groupProvider: GroupProvider,
        private val sanitiser: Sanitiser
) {

    private val deviceClassCache = newHashMap<Class<*>, Map<String, Set<DeviceClassCacheEntry>>>()

    fun parseAndWrapExceptions(xmlList: String, context: Context): RoomDeviceList? {
        return try {
            parseXMLListUnsafe(xmlList, context)
        } catch (e: Exception) {
            LOG.error("cannot parse xmllist", e)
            ErrorHolder.setError(e, "cannot parse xmllist, xmllist was: \r\n" + xmlList
                    .replace("<ATTR key=\"globalpassword\" value=\"[^\"]+\"/>".toRegex(), "")
                    .replace("<ATTR key=\"basicAuth\" value=\"[^\"]+\"/>".toRegex(), ""))

            RequestResult<String>(RequestResultError.DEVICE_LIST_PARSE).handleErrors(context)
            null
        }

    }

    @Throws(Exception::class)
    fun parseXMLListUnsafe(xmlList: String?, context: Context): RoomDeviceList {
        val list = (xmlList ?: "").trim { it <= ' ' }
        val allDevicesRoom = RoomDeviceList(RoomDeviceList.ALL_DEVICES_ROOM)

        if ("" == list) {
            LOG.error("xmlList is null or blank")
            return allDevicesRoom
        }

        gPlotHolder.reset()
        val parsedDevices = parser.parse(list)

        val errorHolder = ReadErrorHolder()

        val allDevices = newHashMap<String, FhemDevice>()

        for ((key) in parsedDevices) {
            val deviceType = getDeviceTypeFor(key)
            val deviceConfiguration = deviceConfigurationProvider.configurationFor(key)

            val xmlListDevices = parsedDevices[key]
            if (xmlListDevices == null || xmlListDevices.isEmpty()) {
                continue
            }

            if (connectionService.mayShowInCurrentConnectionType(deviceType)) {

                val localErrorCount = devicesFromDocument(deviceType.deviceClass, xmlListDevices,
                        allDevices, context, deviceConfiguration.orNull())

                if (localErrorCount > 0) {
                    errorHolder.addErrors(deviceType, localErrorCount)
                }
            }
        }

        performAfterReadOperations(allDevices, errorHolder)
        val roomDeviceList = buildRoomDeviceList(allDevices)

        handleErrors(errorHolder, context)

        LOG.info("loaded {} devices!", allDevices.size)

        return roomDeviceList
    }

    private fun devicesFromDocument(deviceClass: Class<out FhemDevice>, xmlListDevices: List<XmlListDevice>,
                                    allDevices: MutableMap<String, FhemDevice>, context: Context, deviceConfiguration: DeviceConfiguration?): Int {

        var errorCount = 0
        val errorText = StringBuilder()

        for (xmlListDevice in xmlListDevices) {
            if (!deviceFromXmlListDevice(deviceClass, xmlListDevice, allDevices, context, deviceConfiguration)) {
                errorCount++
                errorText.append(xmlListDevice.toString()).append("\r\n\r\n")
            }
        }

        if (errorCount > 0) {
            ErrorHolder.setError("Cannot parse xmlListDevices: \r\n {}$errorText")
        }

        return errorCount
    }

    private fun performAfterReadOperations(allDevices: MutableMap<String, FhemDevice>, errorHolder: ReadErrorHolder) {

        val allDevicesReadCallbacks = newArrayList<FhemDevice>()
        val deviceReadCallbacks = newArrayList<FhemDevice>()

        for (device in allDevices.values) {
            try {
                device.afterAllXMLRead()
                if (device.deviceReadCallback != null) deviceReadCallbacks.add(device)
                if (device.allDeviceReadCallback != null) allDevicesReadCallbacks.add(device)
            } catch (e: Exception) {
                allDevices.remove(device.name)
                errorHolder.addError(getDeviceTypeFor(device))
                LOG.error("cannot perform after read operations", e)
            }

        }

        val callbackDevices = newArrayList<FhemDevice>()
        callbackDevices.addAll(deviceReadCallbacks)
        callbackDevices.addAll(allDevicesReadCallbacks)

        for (device in callbackDevices) {
            try {
                if (device.deviceReadCallback != null) {
                    device.deviceReadCallback.devicesRead(allDevices)
                }
                if (device.allDeviceReadCallback != null) {
                    device.allDeviceReadCallback.devicesRead(allDevices)
                }
            } catch (e: Exception) {
                allDevices.remove(device.name)
                errorHolder.addError(getDeviceTypeFor(device))
                LOG.error("cannot handle associated devices callbacks", e)
            }

        }
    }

    private fun buildRoomDeviceList(allDevices: Map<String, FhemDevice>): RoomDeviceList {
        val roomDeviceList = RoomDeviceList(RoomDeviceList.ALL_DEVICES_ROOM)
        for (device in allDevices.values) {
            roomDeviceList.addDevice(device)
        }
        return roomDeviceList
    }

    private fun handleErrors(errorHolder: ReadErrorHolder, context: Context) {
        if (errorHolder.hasErrors()) {
            var errorMessage = context.getString(R.string.errorDeviceListLoad)
            val deviceTypesError = Joiner.on(",").join(errorHolder.errorDeviceTypeNames)
            errorMessage = String.format(errorMessage, "" + errorHolder.errorCount, deviceTypesError)

            val intent = Intent(Actions.SHOW_TOAST)
            intent.putExtra(BundleExtraKeys.CONTENT, errorMessage)
            context.sendBroadcast(intent)
        }
    }

    private fun deviceFromXmlListDevice(
            deviceClass: Class<out FhemDevice>, xmlListDevice: XmlListDevice, allDevices: MutableMap<String, FhemDevice>, context: Context, deviceConfiguration: DeviceConfiguration?): Boolean {

        try {
            val device = createAndFillDevice(deviceClass, xmlListDevice, deviceConfiguration, context) ?: return false

            device.xmlListDevice = xmlListDevice
            device.afterDeviceXMLRead(context)

            LOG.debug("loaded device with name " + device.name!!)


            if (device is GenericDevice) {
                xmlListDevice.setAttribute("group", groupProvider.functionalityFor(device, context))
            }

            allDevices.put(device.name, device)

            return true
        } catch (e: Exception) {
            LOG.error("error parsing device", e)
            return false
        }

    }

    @Throws(Exception::class)
    private fun <T : FhemDevice> createAndFillDevice(deviceClass: Class<T>, xmlListDevice: XmlListDevice, deviceConfiguration: DeviceConfiguration?, context: Context): T? {
        val device = deviceClass.newInstance()
        device.xmlListDevice = xmlListDevice
        device.deviceConfiguration = Optional.fromNullable(deviceConfiguration)

        val cache = getDeviceClassCacheEntriesFor(deviceClass)

        val children = concat(xmlListDevice.attributes.values, xmlListDevice.internals.values,
                xmlListDevice.states.values, xmlListDevice.headers.values)
        for (child in newArrayList(children)) {
            if (child.key == null) continue

            val sanitisedKey = child.key.trim { it <= ' ' }.replace("[-.]".toRegex(), "_")
            if (!device.acceptXmlKey(sanitisedKey)) {
                continue
            }

            val nodeContent = StringEscapeUtil.unescape(child.value)

            if (nodeContent.isEmpty()) {
                continue
            }

            invokeDeviceAttributeMethod(cache, device, sanitisedKey, nodeContent, child, child.type, context)
        }

        return if (device.name == null) {
            null // just to be sure we don't catch invalid devices ...
        } else device

    }

    private fun <T : FhemDevice> getDeviceClassCacheEntriesFor(deviceClass: Class<T>): Map<String, Set<DeviceClassCacheEntry>>? {
        val clazz = deviceClass as Class<*>
        if (!deviceClassCache.containsKey(clazz)) {
            deviceClassCache.put(clazz, initDeviceCacheEntries(deviceClass))
        }

        return deviceClassCache[clazz]
    }

    @Throws(Exception::class)
    private fun <T : FhemDevice> invokeDeviceAttributeMethod(cache: Map<String, Set<DeviceClassCacheEntry>>?, device: T, key: String,
                                                             value: String, deviceNode: DeviceNode, tagName: DeviceNode.DeviceNodeType, context: Context) {

        device.onChildItemRead(tagName, key, value, deviceNode)
        if (cache != null) {
            handleCacheEntryFor(cache, device, key, value, deviceNode, context)
        }
    }

    @Throws(Exception::class)
    private fun <T : FhemDevice> handleCacheEntryFor(cache: Map<String, Set<DeviceClassCacheEntry>>, device: T,
                                                     key: String, value: String, deviceNode: DeviceNode, context: Context) {
        val lowercaseKey = key.toLowerCase(Locale.getDefault())
        (cache[lowercaseKey] ?: emptySet())
                .forEach { it.apply(device, deviceNode, value, context) }
    }

    private fun <T : FhemDevice> initDeviceCacheEntries(deviceClass: Class<T>): Map<String, Set<DeviceClassCacheEntry>> {
        val cache = newHashMap<String, Set<DeviceClassCacheEntry>>()

        for (method in getAllDeclaredMethods(deviceClass)) {
            if (method.isAnnotationPresent(XmllistAttribute::class.java)) {
                val annotation = method.getAnnotation(XmllistAttribute::class.java)
                for (value in annotation.value) {
                    addToCache(cache, method, value.toLowerCase(Locale.getDefault()))
                }
            }
        }

        for (field in getAllDeclaredFields(deviceClass)) {
            if (field.isAnnotationPresent(XmllistAttribute::class.java)) {
                val annotation = field.getAnnotation(XmllistAttribute::class.java)
                checkArgument(annotation.value.isNotEmpty())

                for (value in annotation.value) {
                    addToCache(cache, DeviceClassFieldEntry(field, value.toLowerCase(Locale.getDefault())))
                }
            }
        }

        return cache
    }

    private fun addToCache(cache: MutableMap<String, Set<DeviceClassCacheEntry>>, method: Method, attribute: String) {
        addToCache(cache, DeviceClassMethodEntry(method, attribute))
    }

    private fun addToCache(cache: MutableMap<String, Set<DeviceClassCacheEntry>>, entry: DeviceClassCacheEntry) {
        val cacheValue = (cache[entry.attribute] ?: emptySet()) + entry
        cache.put(entry.attribute, cacheValue)
    }

    fun fillDeviceWith(device: FhemDevice, updates: Map<String, String>, context: Context) {
        val cache = getDeviceClassCacheEntriesFor(device.javaClass) ?: return

        for (entry in updates.entries) {
            try {
                handleCacheEntryFor(cache, device, entry.key, entry.value,
                        DeviceNode(DeviceNode.DeviceNodeType.GCM_UPDATE, entry.key, entry.value, DateTime.now()), context)

                device.xmlListDevice.states.put(entry.key,
                        sanitiser.sanitise(device.xmlListDevice.type,
                                DeviceNode(DeviceNode.DeviceNodeType.STATE, entry.key, entry.value, DateTime.now())
                        )
                )

                if ("STATE".equals(entry.key, ignoreCase = true)) {
                    device.xmlListDevice.internals.put("STATE",
                            sanitiser.sanitise(device.xmlListDevice.type,
                                    DeviceNode(DeviceNode.DeviceNodeType.INT, "STATE", entry.value, DateTime.now())
                            )
                    )
                }
            } catch (e: Exception) {
                LOG.error("fillDeviceWith - handle " + entry, e)
            }

        }

        device.afterDeviceXMLRead(context)
    }

    private inner class ReadErrorHolder {
        private val deviceTypeErrorCount = newHashMap<DeviceType, Int>()

        internal val errorCount: Int
            get() = deviceTypeErrorCount.values.sumBy { it!! }

        internal val errorDeviceTypeNames: List<String>
            get() = deviceTypeErrorCount.keys.map { it.name }

        internal fun hasErrors(): Boolean = deviceTypeErrorCount.size != 0

        internal fun addError(deviceType: DeviceType?) {
            if (deviceType != null) {
                addErrors(deviceType, 1)
            }
        }

        internal fun addErrors(deviceType: DeviceType, errorCount: Int) {
            val count = deviceTypeErrorCount[deviceType] ?: 0
            deviceTypeErrorCount.put(deviceType, count + errorCount)
        }
    }

    private abstract inner class DeviceClassCacheEntry internal constructor(val attribute: String) : Serializable {

        @Throws(Exception::class)
        abstract fun apply(obj: Any, node: DeviceNode, value: String, context: Context)
    }

    private inner class DeviceClassMethodEntry internal constructor(private val method: Method, attribute: String) : DeviceClassCacheEntry(attribute) {

        init {
            method.isAccessible = true
        }

        @Throws(Exception::class)
        override fun apply(obj: Any, node: DeviceNode, value: String, context: Context) {
            val parameterTypes = method.parameterTypes

            if (parameterTypes.size == 1) {

                if (parameterTypes[0] == String::class.java) {
                    method.invoke(obj, value)
                }

                if (parameterTypes[0] == Double::class.javaPrimitiveType || parameterTypes[0] == Double::class.java) {
                    method.invoke(obj, ValueExtractUtil.extractLeadingDouble(value))
                }

                if (parameterTypes[0] == Int::class.javaPrimitiveType || parameterTypes[0] == Int::class.java) {
                    method.invoke(obj, ValueExtractUtil.extractLeadingInt(value))
                }

            } else if (parameterTypes.size == 2 && parameterTypes[0] == String::class.java && parameterTypes[1] == DeviceNode::class.java) {
                method.invoke(obj, value, node)
            } else if (parameterTypes.size == 2
                    && parameterTypes[0] == String::class.java
                    && parameterTypes[1] == Context::class.java) {
                method.invoke(obj, value, context)
            }
        }
    }

    private inner class DeviceClassFieldEntry internal constructor(private val field: Field, attribute: String) : DeviceClassCacheEntry(attribute) {

        init {
            field.isAccessible = true
        }

        @Throws(Exception::class)
        override fun apply(obj: Any, node: DeviceNode, value: String, context: Context) {
            LOG.debug("setting {} to {}", field.name, value)

            if (field.type.isAssignableFrom(Double::class.java) || field.type.isAssignableFrom(Double::class.javaPrimitiveType!!)) {
                field.set(obj, ValueExtractUtil.extractLeadingDouble(value))
            } else if (field.type.isAssignableFrom(Int::class.java) || field.type.isAssignableFrom(Int::class.javaPrimitiveType!!)) {
                field.set(obj, ValueExtractUtil.extractLeadingInt(value))
            } else {
                field.set(obj, value)
            }
        }
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(DeviceListParser::class.java)
    }
}
