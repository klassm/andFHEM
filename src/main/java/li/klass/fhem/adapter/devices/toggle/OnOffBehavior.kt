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

package li.klass.fhem.adapter.devices.toggle

import com.google.common.collect.Lists.newArrayList
import li.klass.fhem.adapter.devices.hook.DeviceHookProvider
import li.klass.fhem.domain.core.FhemDevice
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OnOffBehavior
@Inject constructor(val hookProvider: DeviceHookProvider) {

    fun isOnByState(device: FhemDevice): Boolean {
        return !isOffByState(device)
    }

    fun isOffByState(device: FhemDevice): Boolean {
        val internalState = device.internalState.toLowerCase(Locale.getDefault())

        if (internalState.equals("???", ignoreCase = true)) {
            return true
        }

        for (offState in getOffStateNames(device)) {
            if (internalState.contains(offState.toLowerCase(Locale.getDefault()))) {
                return true
            }
        }
        return false
    }

    fun isOn(device: FhemDevice): Boolean {
        var isOn = isOnByState(device)
        if (hookProvider.invertState(device)) {
            isOn = !isOn
        }

        return isOn
    }

    private fun getOffStateNames(device: FhemDevice): List<String> {
        val offStateNames = newArrayList("off", "OFF")
        val offStateName = hookProvider.getOffStateName(device)
        if (offStateName != null) {
            offStateNames.add(offStateName.toLowerCase(Locale.getDefault()))
        }

        for (state in newArrayList(offStateNames)) {
            val reverseEventMapState = device.getReverseEventMapStateFor(state)
            if (!state.equals(reverseEventMapState, ignoreCase = true)) {
                offStateNames.add(reverseEventMapState)
            }
        }

        return offStateNames
    }

    companion object {

        fun supports(device: FhemDevice): Boolean {
            return device.setList.contains("on", "off") || device.setList.contains("ON", "OFF") || device.webCmd.containsAll(listOf("on", "off"))
        }
    }
}
