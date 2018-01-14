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

import com.google.common.base.Charsets
import com.google.common.io.Resources
import kotlinx.serialization.json.JSON
import li.klass.fhem.domain.core.FhemDevice
import li.klass.fhem.update.backend.xmllist.XmlListDevice
import org.json.JSONException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeviceConfigurationProvider @Inject
constructor() {
    private val configurations: Map<String, DeviceConfiguration> by lazy {
        val jsonAsString = Resources.toString(Resources.getResource(
                DeviceConfigurationProvider::class.java, "/deviceConfiguration.json"), Charsets.UTF_8)

        JSON.parse(DevicesConfiguration.serializer(), jsonAsString).deviceConfigurations
    }

    fun configurationFor(device: FhemDevice): DeviceConfiguration =
            configurationFor(device.xmlListDevice)

    fun configurationFor(device: XmlListDevice): DeviceConfiguration {
        return try {
            configurationFor(device.type)
        } catch (e: JSONException) {
            throw RuntimeException(e)
        }
    }

    fun configurationFor(type: String): DeviceConfiguration {
        val default = configurations["defaults"]!!
        val forType = configurations[type]
        return forType?.plus(default) ?: default
    }
}
