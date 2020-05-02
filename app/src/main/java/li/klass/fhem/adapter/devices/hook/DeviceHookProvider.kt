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

package li.klass.fhem.adapter.devices.hook

import li.klass.fhem.domain.core.FhemDevice
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeviceHookProvider @Inject
constructor() {

    fun buttonHookFor(device: FhemDevice): ButtonHook {
        val attributes = device.xmlListDevice.attributes
        if (attributes[WIDGET_OVERRIDE]?.value == WIDGET_OVERRIDE_NOARG) {
            return ButtonHook.DEVICE_VALUES
        }
        return HOOK_MAPPING
                .filterKeys { attributes.containsKey(it) }
                .filterKeys { "true".equals(attributes[it]?.value, ignoreCase = true) }
                .map { it.value }
                .firstOrNull() ?: ButtonHook.NORMAL
    }

    fun getOnStateName(device: FhemDevice): String? = device.xmlListDevice.attributeValueFor(ON_STATE_NAME)

    fun getOffStateName(device: FhemDevice): String? =
            device.xmlListDevice.attributeValueFor(OFF_STATE_NAME)

    fun invertState(device: FhemDevice): Boolean {
        val hookValue = device.xmlListDevice.attributeValueFor(INVERT_STATE)
        return hookValue?.equals("true", ignoreCase = true) ?: false
    }

    companion object {

        const val HOOK_ON_OFF = "onOffDevice"
        const val HOOK_ON = "onDevice"
        const val HOOK_OFF = "offDevice"
        const val HOOK_WEBCMD = "webcmdDevice"
        const val WIDGET_OVERRIDE = "widgetOverride"
        const val WIDGET_OVERRIDE_NOARG = ":noArg"
        const val HOOK_TOGGLE = "toggleDevice"
        const val ON_STATE_NAME = "onStateName"
        const val OFF_STATE_NAME = "offStateName"
        private const val INVERT_STATE = "invertState"

        private val HOOK_MAPPING = mapOf(
                HOOK_ON_OFF to ButtonHook.ON_OFF_DEVICE,
                HOOK_ON to ButtonHook.ON_DEVICE,
                HOOK_OFF to ButtonHook.OFF_DEVICE,
                HOOK_WEBCMD to ButtonHook.WEBCMD_DEVICE,
                HOOK_TOGGLE to ButtonHook.TOGGLE_DEVICE
        )
    }
}
