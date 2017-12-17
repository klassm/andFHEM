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

package li.klass.fhem.behavior.dim

import android.content.Context
import com.google.common.base.Optional
import li.klass.fhem.adapter.uiservice.StateUiService
import li.klass.fhem.domain.core.FhemDevice
import li.klass.fhem.domain.setlist.typeEntry.SliderSetListEntry
import li.klass.fhem.update.backend.xmllist.XmlListDevice

class DimmableBehavior private constructor(
        val fhemDevice: FhemDevice,
        private val connectionId: String?,
        val behavior: DimmableTypeBehavior) {

    val currentDimPosition: Float
        get() = behavior.getCurrentDimPosition(fhemDevice)

    val dimLowerBound: Float
        get() = behavior.getDimLowerBound()

    val dimUpperBound: Float
        get() = behavior.getDimUpperBound()

    val dimStep: Float
        get() = behavior.getDimStep()


    fun getDimStateForPosition(position: Float): String =
            behavior.getDimStateForPosition(fhemDevice, position)

    fun switchTo(stateUiService: StateUiService, context: Context, state: Float) {
        behavior.switchTo(stateUiService, context, fhemDevice, connectionId, state)
    }

    companion object {

        fun supports(xmlListDevice: XmlListDevice) =
                ContinuousDimmableBehavior.supports(xmlListDevice)
                        || DiscreteDimmableBehavior.supports(xmlListDevice)

        fun behaviorFor(fhemDevice: FhemDevice, connectionId: String?): Optional<DimmableBehavior> {
            val setList = fhemDevice.xmlListDevice.setList
            if (isDimDisabled(fhemDevice)) {
                return Optional.absent()
            }

            val discrete = DiscreteDimmableBehavior.behaviorFor(setList)
            if (discrete.isPresent) {
                return Optional.of(DimmableBehavior(fhemDevice, connectionId, discrete.get()))
            }

            val continuous = ContinuousDimmableBehavior.behaviorFor(setList)
            if (continuous.isPresent) {
                val behavior = continuous.get()
                return Optional.of(DimmableBehavior(fhemDevice, connectionId, behavior))
            }

            return Optional.absent()
        }

        fun continuousBehaviorFor(device: FhemDevice, attribute: String, connectionId: String?): Optional<DimmableBehavior> {
            val setList = device.xmlListDevice.setList
            if (!setList.contains(attribute)) {
                return Optional.absent()
            }
            val setListSliderValue = setList[attribute, true] as SliderSetListEntry
            return Optional.of(DimmableBehavior(device, connectionId, ContinuousDimmableBehavior(setListSliderValue, attribute)))
        }


        fun isDimDisabled(device: FhemDevice): Boolean {
            val disableDim = device.xmlListDevice.attributes["disableDim"]
            return disableDim != null && "true".equals(disableDim.value, ignoreCase = true)
        }
    }
}
