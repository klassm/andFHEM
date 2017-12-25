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

import kotlinx.serialization.Optional
import kotlinx.serialization.SerialName
import li.klass.fhem.update.backend.device.configuration.sanitise.SanitiseConfiguration
import java.io.Serializable

@kotlinx.serialization.Serializable
data class DeviceConfiguration(
        @SerialName("defaultGroup")
        val defaultGroup: String,

        @SerialName("sensorDevice")
        @Optional
        val isSensorDevice: Boolean = false,

        @SerialName("supportedWidgets")
        @Optional
        val supportedWidgets: Set<String> = emptySet(),

        @SerialName("states")
        @Optional
        val states: Set<ViewItemConfig> = emptySet(),

        @SerialName("attributes")
        @Optional
        val attributes: Set<ViewItemConfig> = emptySet(),

        @SerialName("internals")
        @Optional
        val internals: Set<ViewItemConfig> = emptySet(),

        @SerialName("showStateInOverview")
        @Optional
        val isShowStateInOverview: Boolean = true,

        @SerialName("showMeasuredInOverview")
        @Optional
        val isShowMeasuredInOverview: Boolean = true,

        @SerialName("delayForUpdateAfterCommand")
        @Optional
        val delayForUpdateAfterCommand: Int = 0,

        @SerialName("player")
        @Optional
        val playerConfiguration: PlayerConfiguration? = null,

        @SerialName("sanitise")
        @Optional
        val sanitiseConfiguration: SanitiseConfiguration? = null

) : Serializable {
    fun stateConfigFor(key: String): ViewItemConfig? =
            states.firstOrNull { it.key.equals(key, ignoreCase = true) }

    operator fun plus(configuration: DeviceConfiguration): DeviceConfiguration {
        return copy(
                supportedWidgets = supportedWidgets + configuration.supportedWidgets,
                states = states + configuration.states,
                attributes = attributes + configuration.attributes,
                internals = internals + configuration.internals,
                playerConfiguration = playerConfiguration ?: configuration.playerConfiguration,
                sanitiseConfiguration = sanitiseConfiguration
                        ?.plus(configuration.sanitiseConfiguration)
                        ?: configuration.sanitiseConfiguration
        )
    }
}
