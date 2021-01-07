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
package li.klass.fhem.domain.core

import android.content.Context
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import li.klass.fhem.AndFHEMApplication
import li.klass.fhem.R
import li.klass.fhem.connection.backend.ConnectionService
import li.klass.fhem.dagger.ApplicationComponent
import li.klass.fhem.dagger.DaggerApplicationComponent
import li.klass.fhem.dagger.DatabaseModule
import li.klass.fhem.testsuite.category.DeviceTestBase
import li.klass.fhem.testutil.MockRule
import li.klass.fhem.update.backend.xmllist.DeviceListParser
import li.klass.fhem.update.backend.xmllist.DeviceNode
import org.junit.Before
import org.junit.Rule
import org.junit.experimental.categories.Category
import java.io.File
import java.util.regex.Pattern

@Category(DeviceTestBase::class)
abstract class DeviceXMLParsingBase {
    @JvmField
    protected var roomDeviceList: RoomDeviceList? = null

    @MockK
    lateinit var context: Context

    @MockK
    lateinit var connectionService: ConnectionService

    @get:Rule
    var mockitoRule = MockRule()

    protected lateinit var applicationComponent: ApplicationComponent

    @Before
    @Throws(Exception::class)
    open fun before() {
        val application: AndFHEMApplication = mockk()
        every { application.applicationContext } returns context
        applicationComponent = DaggerApplicationComponent.builder()
                .application(application)
                .databaseModule(DatabaseModule(application)).build()
        val deviceListParser = DeviceListParser(
                applicationComponent.xmllistParser,
                applicationComponent.gPlotHolder, applicationComponent.groupProvider,
                applicationComponent.sanitiser
        )
        every { connectionService.mayShowInCurrentConnectionType(any(), null) } returns true
        mockStrings()

        val content = testFileBaseClass.getResource(getFileName())?.readText(Charsets.UTF_8)!!
        roomDeviceList = deviceListParser.parseXMLListUnsafe(content, context, "abc")
    }

    private fun mockStrings() {
        try {
            val content: String = File("src/main/res/values/strings.xml").readText(Charsets.UTF_8)
            val pattern = Pattern.compile("<string name=\"([^\"]+)\">([^<]+)</string>")
            val matcher = pattern.matcher(content)
            val values: MutableMap<String, String> = mutableMapOf()
            while (matcher.find()) {
                values[matcher.group(1)] = matcher.group(2)
            }
            for (field in R.string::class.java.declaredFields) {
                val value = field[R.string::class.java] as Int
                every { context.getString(value) } returns (values[field.name] ?: "")
            }
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

    /**
     * Base class used as context for loading the input file.
     *
     * @return base class
     */
    protected open val testFileBaseClass: Class<*>
        protected get() = javaClass

    protected abstract fun getFileName(): String
    protected val defaultDevice: FhemDevice?
        protected get() = getDeviceFor(DEFAULT_TEST_DEVICE_NAME)

    // Careful: The Java-Compiler needs some class instance of <T> here to infer the type correctly!
    protected fun getDeviceFor(deviceName: String): FhemDevice? {
        return roomDeviceList!!.getDeviceFor(deviceName)
    }

    protected fun stateValueFor(device: FhemDevice, key: String): String? {
        return xmllistValueFor(key, device.xmlListDevice.states)
    }

    protected fun attributeValueFor(device: FhemDevice, key: String): String? {
        return xmllistValueFor(key, device.xmlListDevice.attributes)
    }

    protected fun internalValueFor(device: FhemDevice, key: String): String? {
        return xmllistValueFor(key, device.xmlListDevice.internals)
    }

    private fun xmllistValueFor(key: String, map: Map<String, DeviceNode>): String? {
        val (_, _, value) = map[key] ?: return null
        return value
    }

    companion object {
        const val DEFAULT_TEST_ROOM_NAME = "room"
        const val DEFAULT_TEST_DEVICE_NAME = "device"
    }
}