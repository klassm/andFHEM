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

package li.klass.fhem.behavior.dim;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import li.klass.fhem.domain.core.FhemDevice;
import li.klass.fhem.domain.setlist.SetList;
import li.klass.fhem.domain.setlist.SetListSliderValue;
import li.klass.fhem.domain.setlist.SetListValue;

import static li.klass.fhem.util.ValueExtractUtil.extractLeadingInt;

class ContinuousDimmableBehavior implements DimmableTypeBehavior {
    private static final ImmutableList<String> DIM_ATTRIBUTES = ImmutableList.of("state", "dim", "level", "pct", "position", "value");
    private SetListSliderValue slider;
    private String setListAttribute;

    private ContinuousDimmableBehavior(SetListSliderValue sliderValue, String setListAttribute) {
        this.slider = sliderValue;
        this.setListAttribute = setListAttribute;
    }

    @Override
    public int getDimLowerBound() {
        return slider.getStart();
    }

    @Override
    public int getDimStep() {
        return slider.getStep();
    }

    @Override
    public int getCurrentDimPosition(FhemDevice device) {
        String value = device.getXmlListDevice().getStates().get(setListAttribute).getValue();
        return getPositionForDimState(value);
    }

    @Override
    public int getDimUpperBound() {
        return slider.getStop();
    }

    @Override
    public String getDimStateForPosition(int position) {
        return position + "";
    }

    @Override
    public int getPositionForDimState(String dimState) {
        return extractLeadingInt(dimState);
    }

    public SetListSliderValue getSlider() {
        return slider;
    }

    @Override
    public String getStateName() {
        return setListAttribute;
    }

    static Optional<ContinuousDimmableBehavior> behaviorFor(SetList setList) {
        for (String dimAttribute : DIM_ATTRIBUTES) {
            if (!setList.contains(dimAttribute)) {
                continue;
            }
            SetListValue setListValue = setList.get(dimAttribute);
            if (setListValue instanceof SetListSliderValue) {
                return Optional.of(new ContinuousDimmableBehavior((SetListSliderValue) setListValue, dimAttribute));
            }
            return Optional.absent();
        }
        return Optional.absent();
    }
}
