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

import com.google.common.base.Optional
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

        return getOffStateNames(device).any { internalState.contains(it.toLowerCase(Locale.getDefault())) }
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
        val offStateNames = availableOffStateNames + reverseEventMapNamesFor(availableOffStateNames, device)
        val existingOffStateNames = device.setList.existingStatesOf(offStateNames)
        return Optional.fromNullable(offStateNameByHook).asSet() + existingOffStateNames
    }

    fun getOffStateName(device: FhemDevice): String? = hookProvider.getOffStateName(device)
            ?: getOffStateNames(device).firstOrNull()
            ?: if (device.webCmd.contains("off")) "off" else null

    private fun getOnStateNames(device: FhemDevice): Set<String> {
        val onStateNameByHook = hookProvider.getOnStateName(device)
        val onStateNames = availableOnStateNames + reverseEventMapNamesFor(availableOnStateNames, device)
        val existingOnStateNames = device.setList.existingStatesOf(onStateNames)
        return Optional.fromNullable(onStateNameByHook).asSet() + existingOnStateNames
    }

    fun getOnStateName(device: FhemDevice): String? = hookProvider.getOnStateName(device)
            ?: getOnStateNames(device).firstOrNull()
            ?: if (device.webCmd.contains("on")) "on" else null

    fun getOnOffStateNames(device: FhemDevice): Set<String> =
            getOnStateNames(device) + getOffStateNames(device)

    private fun reverseEventMapNamesFor(stateNames: Set<String>, device: FhemDevice): List<String> {
        return stateNames
                .map { it to device.getReverseEventMapStateFor(it) }
                .filter { !it.first.equals(it.second, ignoreCase = true) && it.second != null }
                .map { it.second!! }
    }

    fun supports(device: FhemDevice): Boolean = hookProvider.buttonHookFor(device) != ButtonHook.DEVICE_VALUES && getOnStateName(device) != null && getOffStateName(device) != null

    companion object {
        val availableOnStateNames = setOf("on", "ON", "ein")
        val availableOffStateNames = setOf("off", "OFF", "aus")
    }
}
