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

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.google.common.base.Charsets
import com.google.common.base.Optional
import com.google.common.io.Resources
import li.klass.fhem.domain.core.FhemDevice
import li.klass.fhem.update.backend.xmllist.XmlListDevice
import org.json.JSONException
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeviceConfigurationProvider @Inject
constructor() {
    private val configurations: Map<String, DeviceConfiguration>

    init {
        try {
            val jsonAsString = Resources.toString(Resources.getResource(
                    DeviceConfigurationProvider::class.java, "deviceConfiguration.json"), Charsets.UTF_8)
            val mapper = ObjectMapper().registerModule(KotlinModule())
            val typeRef = object : TypeReference<HashMap<String, DeviceConfiguration>>() {}
            configurations = mapper.readValue(jsonAsString, typeRef)
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }


    fun configurationFor(device: FhemDevice): Optional<DeviceConfiguration> =
            configurationFor(device.xmlListDevice)

    private fun configurationFor(device: XmlListDevice): Optional<DeviceConfiguration> {
        try {
            return configurationFor(device.type)
        } catch (e: JSONException) {
            return Optional.absent()
        }

    }

    @Throws(JSONException::class)
    fun configurationFor(type: String): Optional<DeviceConfiguration> = Optional.fromNullable(configurations[type])
}
