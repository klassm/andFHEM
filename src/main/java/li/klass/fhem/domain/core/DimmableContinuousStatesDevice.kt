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

package li.klass.fhem.domain.core

import li.klass.fhem.domain.core.DeviceFunctionality.functionalityForDimmable
import li.klass.fhem.domain.setlist.typeEntry.SliderSetListEntry
import li.klass.fhem.util.FloatUtils
import li.klass.fhem.util.NumberUtil.isDecimalNumber
import li.klass.fhem.util.ValueExtractUtil.extractLeadingFloat

abstract class DimmableContinuousStatesDevice<D : FhemDevice> : DimmableDevice<D>() {
    override fun getDimStateNameForDimStateValue(value: Float): String {
        if (supportsOnOffDimMapping()) {
            if (FloatUtils.isEqual(value, dimUpperBound)) return getEventMapStateFor("on")
            if (FloatUtils.isEqual(value, dimLowerBound)) return getEventMapStateFor("off")
        }

        val prefix = if (getSetListDimStateAttributeName() == "state") "" else getSetListDimStateAttributeName() + " "
        return prefix + value
    }

    override fun getPositionForDimState(dimState: String): Float {
        var dimState = dimState
        dimState = dimState.replace(getSetListDimStateAttributeName().toRegex(), "").replace("[\\(\\)% ]".toRegex(), "")
        if (dimState == getEventMapStateFor("on") || "on" == dimState || onStateName == dimState)
            return dimUpperBound
        if (dimState == getEventMapStateFor("off") || "off" == dimState || offStateName == dimState)
            return dimLowerBound
        if (!isDecimalNumber(dimState)) return 0f

        return extractLeadingFloat(dimState)
    }

    override fun supportsDim(): Boolean {
        return stateSliderValue != null
    }

    protected val stateSliderValue: SliderSetListEntry?
        get() {
            val stateEntry = getSetList()[getSetListDimStateAttributeName()]
            if (stateEntry is SliderSetListEntry) {
                return stateEntry
            }
            return null
        }

    protected open fun getSetListDimStateAttributeName(): String = "state"

    override fun getDimLowerBound(): Float {
        val stateSliderValue = stateSliderValue ?: return 0f

        return stateSliderValue.start
    }

    override fun getDimUpperBound(): Float {
        val stateSliderValue = stateSliderValue ?: return 100f

        return stateSliderValue.stop
    }

    override fun getDimStep(): Float {
        val stateSliderValue = stateSliderValue ?: return 1f

        return stateSliderValue.step
    }

    override fun getDeviceGroup(): DeviceFunctionality {
        if (!supportsToggle() && !supportsDim()) {
            return super.getDeviceGroup()
        }
        return functionalityForDimmable(this)
    }

    fun supportsOnOffDimMapping(): Boolean {
        return true
    }
}
