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
import li.klass.fhem.update.backend.device.configuration.DeviceConfigurationProvider
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OnOffBehavior
@Inject constructor(private val hookProvider: DeviceHookProvider,
                    private val deviceConfigurationProvider: DeviceConfigurationProvider
) {

    fun isOnByState(device: FhemDevice): Boolean = !isOffByState(device)

    fun isOffByState(device: FhemDevice): Boolean {
        val stateAttributeName =
                deviceConfigurationProvider.configurationFor(device).stateAttributeName
        val readingsState = device.xmlListDevice.getState(stateAttributeName, true) ?: ""

        val states = setOf(device.internalState, readingsState).flatMap {
            val additionalState = if (it.contains("-for-timer")) {
                it.split(" ")[0] // important for on / off-for-timer
            } else it

            setOf(additionalState, it)
        }

        if (states.any { it.equals("???", ignoreCase = true) }) {
            return true
        }

        val offStateNames = getOffStateNames(device) + "off"
        return offStateNames.any { offName -> states.any { it.equals(offName, ignoreCase = true) } }
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

    private fun getOffStateNames(device: FhemDevice): List<String> {
        val offStateNameByHook = hookProvider.getOffStateName(device)
        val offStateNames = availableOffStateNames + eventMapNamesFor(availableOffStateNames, device)
        val existingOffStateNames = existingStatesOfIncludingEventMap(device, offStateNames)

        return listOfNotNull(offStateNameByHook) +
                existingOffStateNames +
                deviceConfigurationProvider.configurationFor(device).additionalOffStateNames
    }

    fun getOffStateName(device: FhemDevice): String? = hookProvider.getOffStateName(device)
            ?: getOffStateNames(device).firstOrNull()
            ?: device.eventMap.getFirstResolvingTo(device.webCmd, "off")
            ?: if (device.webCmd.contains("off")) "off" else null

    private fun getOnStateNames(device: FhemDevice): List<String> {
        val onStateNameByHook = hookProvider.getOnStateName(device)
        val onStateNames = availableOnStateNames + eventMapNamesFor(availableOnStateNames, device)
        val existingOnStateNames = existingStatesOfIncludingEventMap(device, onStateNames)
        return listOfNotNull(onStateNameByHook) +
                existingOnStateNames +
                deviceConfigurationProvider.configurationFor(device).additionalOnStateNames
    }

    private fun existingStatesOfIncludingEventMap(device: FhemDevice, offStateNames: Set<String>): List<String> {
        val states = device.setList.existingStatesOf(offStateNames)
        val reverseEventMapStates = states.mapNotNull { device.getReverseEventMapStateFor(it) }
        val comparator =
                (compareBy<String>({ it.equals("on", true) }, { it.equals("off", true) }, { it }))
        return (states + reverseEventMapStates).sortedWith(comparator).asReversed()
    }

    fun getOnStateName(device: FhemDevice): String? = hookProvider.getOnStateName(device)
            ?: getOnStateNames(device).firstOrNull()
            ?: device.eventMap.getFirstResolvingTo(device.webCmd, "on")
            ?: if (device.webCmd.contains("on")) "on" else null

    fun getOnOffStateNames(device: FhemDevice): List<String> =
            getOnStateNames(device) + getOffStateNames(device)

    private fun eventMapNamesFor(stateNames: Set<String>, device: FhemDevice): List<String> {
        return stateNames
                .map { it to device.getEventMapStateFor(it) }
                .filter { !it.first.equals(it.second, ignoreCase = true) }
                .map { it.second }
    }

    fun supports(device: FhemDevice): Boolean = hookProvider.buttonHookFor(device) != ButtonHook.DEVICE_VALUES && getOnStateName(device) != null && getOffStateName(device) != null

    companion object {
        val availableOnStateNames = setOf("on", "ON", "ein", "on-for-timer", "on-till")
        val availableOffStateNames = setOf("off", "OFF", "aus", "off-for-timer", "off-till")
    }
}
