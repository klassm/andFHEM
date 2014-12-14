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

import li.klass.fhem.domain.setlist.SetListSliderValue;
import li.klass.fhem.domain.setlist.SetListValue;

import static li.klass.fhem.domain.core.DeviceFunctionality.functionalityForDimmable;
import static li.klass.fhem.util.NumberUtil.isDecimalNumber;
import static li.klass.fhem.util.ValueExtractUtil.extractLeadingInt;

public abstract class DimmableContinuousStatesDevice<D extends Device<D>> extends DimmableDevice<D> {
    @Override
    public String getDimStateForPosition(int position) {
        if (supportsOnOffDimMapping()) {
            if (position == getDimUpperBound()) return getEventMapStateFor("on");
            if (position == getDimLowerBound()) return getEventMapStateFor("off");
        }

        String prefix = getSetListDimStateAttributeName().equals("state") ? "" : getSetListDimStateAttributeName() + " ";
        return prefix + position;
    }

    @Override
    public int getPositionForDimState(String dimState) {
        dimState = dimState.replaceAll(getSetListDimStateAttributeName(), "").replaceAll("[% ]", "");
        if (dimState.equals(getEventMapStateFor("on")) || "on".equals(dimState))
            return getDimUpperBound();
        if (dimState.equals(getEventMapStateFor("off")) || "off".equals(dimState))
            return getDimLowerBound();
        if (!isDecimalNumber(dimState)) return 0;

        return extractLeadingInt(dimState);
    }

    @Override
    public boolean supportsDim() {
        return getStateSliderValue() != null;
    }

    protected SetListSliderValue getStateSliderValue() {
        SetListValue stateEntry = getSetList().get(getSetListDimStateAttributeName());
        if (stateEntry instanceof SetListSliderValue) {
            return (SetListSliderValue) stateEntry;
        }
        return null;
    }

    protected String getSetListDimStateAttributeName() {
        return "state";
    }

    @Override
    public int getDimLowerBound() {
        SetListSliderValue stateSliderValue = getStateSliderValue();
        if (stateSliderValue == null) return 0;

        return stateSliderValue.getStart();
    }

    @Override
    public int getDimUpperBound() {
        SetListSliderValue stateSliderValue = getStateSliderValue();
        if (stateSliderValue == null) return 100;

        return stateSliderValue.getStop();
    }

    @Override
    public int getDimStep() {
        SetListSliderValue stateSliderValue = getStateSliderValue();
        if (stateSliderValue == null) return 1;

        return stateSliderValue.getStep();
    }

    @Override
    public DeviceFunctionality getDeviceGroup() {
        return functionalityForDimmable(this);
    }

    public boolean supportsOnOffDimMapping() {
        return true;
    }
}
