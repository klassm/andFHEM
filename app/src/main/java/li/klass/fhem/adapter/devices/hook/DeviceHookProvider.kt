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

import com.google.common.collect.ImmutableMap
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

        val HOOK_ON_OFF = "onOffDevice"
        val HOOK_ON = "onDevice"
        val HOOK_OFF = "offDevice"
        val HOOK_WEBCMD = "webcmdDevice"
        val WIDGET_OVERRIDE = "widgetOverride"
        val WIDGET_OVERRIDE_NOARG = ":noArg"
        val HOOK_TOGGLE = "toggleDevice"
        val ON_STATE_NAME = "onStateName"
        val OFF_STATE_NAME = "offStateName"
        private val INVERT_STATE = "invertState"

        private val HOOK_MAPPING = ImmutableMap.builder<String, ButtonHook>()
                .put(HOOK_ON_OFF, ButtonHook.ON_OFF_DEVICE)
                .put(HOOK_ON, ButtonHook.ON_DEVICE)
                .put(HOOK_OFF, ButtonHook.OFF_DEVICE)
                .put(HOOK_WEBCMD, ButtonHook.WEBCMD_DEVICE)
                .put(HOOK_TOGGLE, ButtonHook.TOGGLE_DEVICE)
                .build()
    }
}
