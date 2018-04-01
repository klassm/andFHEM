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

package li.klass.fhem.update.backend.group

import android.content.Context
import li.klass.fhem.adapter.devices.toggle.OnOffBehavior
import li.klass.fhem.behavior.dim.DimmableBehavior
import li.klass.fhem.domain.core.DeviceFunctionality
import li.klass.fhem.domain.core.FhemDevice
import li.klass.fhem.update.backend.device.configuration.DeviceConfigurationProvider
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GroupProvider @Inject
constructor(deviceGroupProviders: DeviceGroupProviders,
            private val onOffBehavior: OnOffBehavior,
            private val deviceConfigurationProvider: DeviceConfigurationProvider
) {
    private val providerMap: Map<String, DeviceGroupProvider> = deviceGroupProviders.providers.associate { it.deviceType to it }

    fun functionalityFor(device: FhemDevice, context: Context): String {
        val xmlListDevice = device.xmlListDevice
        val group = xmlListDevice.getAttribute("group")
        if (group != null) {
            return group
        }

        val providerGroup = providerMap[xmlListDevice.type]
                ?.groupFor(xmlListDevice, context)
        if (providerGroup != null) return providerGroup

        return when {
            DimmableBehavior.behaviorFor(device, null).isPresent -> DeviceFunctionality.DIMMER
            onOffBehavior.supports(device) -> DeviceFunctionality.SWITCH
            else -> DeviceFunctionality.valueOf(deviceConfigurationProvider.configurationFor(device).defaultGroup)
        }.getCaptionText(context)
    }
}
