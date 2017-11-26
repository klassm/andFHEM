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

import li.klass.fhem.adapter.devices.hook.ButtonHook
import li.klass.fhem.adapter.devices.hook.DeviceHookProvider
import li.klass.fhem.domain.core.FhemDevice
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OnOffBehavior
@Inject constructor(val hookProvider: DeviceHookProvider) {

    fun isOnByState(device: FhemDevice): Boolean = !isOffByState(device)

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

    fun isOnConsideringHooks(device: FhemDevice): Boolean =
            when (hookProvider.buttonHookFor(device)) {
                ButtonHook.ON_DEVICE -> true
                ButtonHook.OFF_DEVICE -> false
                else -> isOn(device)
            }

    private fun getOffStateNames(device: FhemDevice): Set<String> {
        val offStateNameByHook = hookProvider.getOffStateName(device)
        val offStateNames = setOf("off", "OFF") + offStateNameByHook.toLowerCase(Locale.getDefault())

        return offStateNames + reverseEventMapNamesFor(offStateNames, device)
    }

    private fun getOnStateNames(device: FhemDevice): Set<String> {
        val onStateNameByHook = hookProvider.getOnStateName(device)
        val onStateNames = setOf("on", "ON") + onStateNameByHook.toLowerCase(Locale.getDefault())

        return onStateNames + reverseEventMapNamesFor(onStateNames, device)
    }

    fun getOnOffStateNames(device: FhemDevice): Set<String> =
            getOnStateNames(device) + getOffStateNames(device)

    private fun reverseEventMapNamesFor(stateNames: Set<String>, device: FhemDevice): List<String> {
        val reverseEventMapNames = stateNames
                .map { it to device.getReverseEventMapStateFor(it) }
                .filter { !it.first.equals(it.second, ignoreCase = true) && it.second != null }
                .map { it.second!! }
        return reverseEventMapNames
    }

    companion object {
        fun supports(device: FhemDevice): Boolean =
                device.xmlListDevice.setList.contains("on", "off")
                        || device.xmlListDevice.setList.contains("ON", "OFF")
                        || device.webCmd.containsAll(listOf("on", "off"))
    }
}
