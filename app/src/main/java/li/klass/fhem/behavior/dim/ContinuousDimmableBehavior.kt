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
import com.google.common.base.Joiner
import com.google.common.base.Optional
import com.google.common.collect.ImmutableList
import li.klass.fhem.adapter.uiservice.StateUiService
import li.klass.fhem.domain.core.FhemDevice
import li.klass.fhem.domain.setlist.SetList
import li.klass.fhem.domain.setlist.typeEntry.SliderSetListEntry
import li.klass.fhem.update.backend.xmllist.DeviceNode
import li.klass.fhem.util.ValueExtractUtil.extractLeadingFloat
import org.joda.time.DateTime
import java.util.*

class ContinuousDimmableBehavior internal constructor(val slider: SliderSetListEntry, private val setListAttribute: String) : DimmableTypeBehavior {

    override fun getDimLowerBound(): Float = slider.start

    override fun getDimStep(): Float = slider.step

    override fun getCurrentDimPosition(device: FhemDevice): Float {
        val value = getValue(device).value
        return getPositionForDimState(value)
    }

    private fun getValue(device: FhemDevice): DeviceNode {
        val states = device.xmlListDevice.states
        val value = if (states.containsKey(setListAttribute)) states[setListAttribute] else states["state"]
        return value ?: DeviceNode(DeviceNode.DeviceNodeType.STATE, "state", "", null as DateTime?)
    }

    override fun getDimUpperBound(): Float = slider.stop

    override fun getDimStateForPosition(fhemDevice: FhemDevice, position: Float): String {
        if (setListAttribute.equals("state", ignoreCase = true)) {
            val setList = fhemDevice.setList
            if (position == getDimLowerBound() && setList.contains("off")) {
                return "off"
            } else if (position == getDimUpperBound() && setList.contains("on")) {
                return "on"
            }
        }
        val positionAsText = position.toString() + ""
        return if (positionAsText.endsWith(".0")) {
            positionAsText.replace(".0", "")
        } else positionAsText

    }

    override fun getPositionForDimState(dimState: String): Float {
        val state = dimState.toLowerCase(Locale.getDefault())
                .replace(Joiner.on("|").join(DIM_ATTRIBUTES).toRegex(), "")
                .replace("%".toRegex(), "")
                .trim { it <= ' ' }
        if (UPPER_BOUND_STATES.contains(state)) {
            return getDimLowerBound()
        } else if (LOWER_BOUND_STATES.contains(state)) {
            return getDimUpperBound()
        }
        return extractLeadingFloat(state)
    }

    override fun getStateName(): String = setListAttribute

    override fun switchTo(stateUiService: StateUiService, context: Context, fhemDevice: FhemDevice, connectionId: String?, state: Float) {
        stateUiService.setSubState(fhemDevice, connectionId, setListAttribute, getDimStateForPosition(fhemDevice, state), context)
    }

    companion object {
        private val DIM_ATTRIBUTES = ImmutableList.of("state", "dim", "level", "pct", "position", "value")
        private val UPPER_BOUND_STATES = ImmutableList.of("on", "close", "closed")
        private val LOWER_BOUND_STATES = ImmutableList.of("off", "open", "opened")

        fun supports(device: FhemDevice) = behaviorFor(device.setList).isPresent

        fun behaviorFor(setList: SetList): Optional<ContinuousDimmableBehavior> {
            return Optional.fromNullable(DIM_ATTRIBUTES
                    .filter { setList.contains(it) }
                    .map { setList[it, true] }
                    .map { if (it is SliderSetListEntry) ContinuousDimmableBehavior(it, it.key) else null }
                    .firstOrNull())
        }
    }
}
