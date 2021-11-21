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

package li.klass.fhem.devices.backend

import li.klass.fhem.behavior.toggle.OnOffBehavior
import li.klass.fhem.domain.core.FhemDevice
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ToggleableService @Inject
constructor(
        val genericDeviceService: GenericDeviceService,
        val onOffBehavior: OnOffBehavior
) {
    /**
     * Toggles the state of a toggleable device.
     *
     * @param device       concerned device
     * @param connectionId connectionId
     * @param context      context
     */
    fun toggleState(device: FhemDevice, connectionId: String?) {
        val targetState = when {
            onOffBehavior.isOnByState(device) -> onOffBehavior.getOffStateName(device)
            else -> onOffBehavior.getOnStateName(device)
        }
        if (targetState == null) {
            logger.error("toggleState(device=${device.name}) - cannot toggle as state cannot be found for current state '${device.state}'")
            return
        }
        genericDeviceService.setState(device.xmlListDevice, targetState, connectionId)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(ToggleableService::class.java)
    }
}
