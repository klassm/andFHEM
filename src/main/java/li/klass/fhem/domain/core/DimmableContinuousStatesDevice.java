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

package li.klass.fhem.domain.core;

import li.klass.fhem.domain.setlist.SetListEntry;
import li.klass.fhem.domain.setlist.typeEntry.SliderSetListEntry;
import li.klass.fhem.util.FloatUtils;

import static li.klass.fhem.domain.core.DeviceFunctionality.functionalityForDimmable;
import static li.klass.fhem.util.NumberUtil.isDecimalNumber;
import static li.klass.fhem.util.ValueExtractUtil.extractLeadingFloat;

public abstract class DimmableContinuousStatesDevice<D extends FhemDevice<D>> extends DimmableDevice<D> {
    @Override
    public String getDimStateNameForDimStateValue(float value) {
        if (supportsOnOffDimMapping()) {
            if (FloatUtils.isEqual(value, getDimUpperBound())) return getEventMapStateFor("on");
            if (FloatUtils.isEqual(value, getDimLowerBound())) return getEventMapStateFor("off");
        }

        String prefix = getSetListDimStateAttributeName().equals("state") ? "" : getSetListDimStateAttributeName() + " ";
        return prefix + value;
    }

    @Override
    public float getPositionForDimState(String dimState) {
        dimState = dimState.replaceAll(getSetListDimStateAttributeName(), "").replaceAll("[\\(\\)% ]", "");
        if (dimState.equals(getEventMapStateFor("on")) || "on".equals(dimState) || getOnStateName().equals(dimState))
            return getDimUpperBound();
        if (dimState.equals(getEventMapStateFor("off")) || "off".equals(dimState) || getOffStateName().equals(dimState))
            return getDimLowerBound();
        if (!isDecimalNumber(dimState)) return 0;

        return extractLeadingFloat(dimState);
    }

    @Override
    public boolean supportsDim() {
        return getStateSliderValue() != null;
    }

    protected SliderSetListEntry getStateSliderValue() {
        SetListEntry stateEntry = getSetList().get(getSetListDimStateAttributeName());
        if (stateEntry instanceof SliderSetListEntry) {
            return (SliderSetListEntry) stateEntry;
        }
        return null;
    }

    protected String getSetListDimStateAttributeName() {
        return "state";
    }

    @Override
    public float getDimLowerBound() {
        SliderSetListEntry stateSliderValue = getStateSliderValue();
        if (stateSliderValue == null) return 0;

        return stateSliderValue.getStart();
    }

    @Override
    public float getDimUpperBound() {
        SliderSetListEntry stateSliderValue = getStateSliderValue();
        if (stateSliderValue == null) return 100;

        return stateSliderValue.getStop();
    }

    @Override
    public float getDimStep() {
        SliderSetListEntry stateSliderValue = getStateSliderValue();
        if (stateSliderValue == null) return 1;

        return stateSliderValue.getStep();
    }

    @Override
    public DeviceFunctionality getDeviceGroup() {
        if (!supportsToggle() && !supportsDim()) {
            return super.getDeviceGroup();
        }
        return functionalityForDimmable(this);
    }

    public boolean supportsOnOffDimMapping() {
        return true;
    }
}
