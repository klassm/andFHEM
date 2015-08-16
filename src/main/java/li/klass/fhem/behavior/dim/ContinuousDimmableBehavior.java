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

import android.content.Context;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import java.util.Map;

import li.klass.fhem.adapter.devices.toggle.OnOffBehavior;
import li.klass.fhem.adapter.uiservice.StateUiService;
import li.klass.fhem.domain.core.FhemDevice;
import li.klass.fhem.domain.setlist.SetList;
import li.klass.fhem.domain.setlist.SetListSliderValue;
import li.klass.fhem.domain.setlist.SetListValue;
import li.klass.fhem.service.room.xmllist.DeviceNode;

import static li.klass.fhem.util.ValueExtractUtil.extractLeadingInt;

class ContinuousDimmableBehavior implements DimmableTypeBehavior {
    private static final ImmutableList<String> DIM_ATTRIBUTES = ImmutableList.of("state", "dim", "level", "pct", "position", "value");
    private SetListSliderValue slider;
    private String setListAttribute;

    ContinuousDimmableBehavior(SetListSliderValue sliderValue, String setListAttribute) {
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
        String value = getValue(device).getValue();
        return getPositionForDimState(value);
    }

    private DeviceNode getValue(FhemDevice device) {
        Map<String, DeviceNode> states = device.getXmlListDevice().getStates();
        return states.containsKey(setListAttribute) ? states.get(setListAttribute) : states.get("state");
    }

    @Override
    public int getDimUpperBound() {
        return slider.getStop();
    }

    @Override
    public String getDimStateForPosition(FhemDevice fhemDevice, int position) {
        if (setListAttribute.equalsIgnoreCase("state")) {
            if (position == getDimLowerBound()) {
                return "off";
            } else if (position == getDimUpperBound()) {
                return "on";
            }
        }
        return position + "";
    }

    @Override
    public int getPositionForDimState(String dimState) {
        if ("on".equalsIgnoreCase(dimState)) {
            return getDimUpperBound();
        } else if ("off".equalsIgnoreCase(dimState)) {
            return getDimLowerBound();
        }
        return extractLeadingInt(dimState);
    }

    public SetListSliderValue getSlider() {
        return slider;
    }

    @Override
    public String getStateName() {
        return setListAttribute;
    }

    @Override
    public void switchTo(StateUiService stateUiService, Context context, FhemDevice fhemDevice, int state) {
        stateUiService.setSubState(fhemDevice, setListAttribute, getDimStateForPosition(fhemDevice, state), context);
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
