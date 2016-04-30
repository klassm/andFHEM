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

import org.joda.time.DateTime;

import java.util.Locale;
import java.util.Map;

import li.klass.fhem.R;
import li.klass.fhem.adapter.uiservice.StateUiService;
import li.klass.fhem.domain.core.FhemDevice;
import li.klass.fhem.domain.setlist.SetList;
import li.klass.fhem.domain.setlist.SetListEntry;
import li.klass.fhem.domain.setlist.typeEntry.SliderSetListEntry;
import li.klass.fhem.service.deviceConfiguration.DeviceConfiguration;
import li.klass.fhem.service.deviceConfiguration.ViewItemConfig;
import li.klass.fhem.service.room.xmllist.DeviceNode;
import li.klass.fhem.util.DialogUtil;

import static li.klass.fhem.util.ValueExtractUtil.extractLeadingFloat;

public class ContinuousDimmableBehavior implements DimmableTypeBehavior {
    public static final ImmutableList<String> DIM_ATTRIBUTES = ImmutableList.of("state", "dim", "level", "pct", "position", "value");
    private static final ImmutableList<String> UPPER_BOUND_STATES = ImmutableList.of("on", "close", "closed");
    private static final ImmutableList<String> LOWER_BOUND_STATES = ImmutableList.of("off", "open", "opened");
    private SliderSetListEntry slider;
    private String setListAttribute;

    ContinuousDimmableBehavior(SliderSetListEntry sliderValue, String setListAttribute) {
        this.slider = sliderValue;
        this.setListAttribute = setListAttribute;
    }

    @Override
    public float getDimLowerBound() {
        return slider.getStart();
    }

    @Override
    public float getDimStep() {
        return slider.getStep();
    }

    @Override
    public float getCurrentDimPosition(FhemDevice device) {
        String value = getValue(device).getValue();
        return getPositionForDimState(value);
    }

    private DeviceNode getValue(FhemDevice device) {
        Map<String, DeviceNode> states = device.getXmlListDevice().getStates();
        DeviceNode value = states.containsKey(setListAttribute) ? states.get(setListAttribute) : states.get("state");
        return value == null ? new DeviceNode(DeviceNode.DeviceNodeType.STATE, "state", "", (DateTime) null) : value;
    }

    @Override
    public float getDimUpperBound() {
        return slider.getStop();
    }

    @Override
    public String getDimStateForPosition(FhemDevice fhemDevice, float position) {
        if (setListAttribute.equalsIgnoreCase("state")) {
            if (position == getDimLowerBound() && fhemDevice.getSetList().contains("off")) {
                return "off";
            } else if (position == getDimUpperBound() && fhemDevice.getSetList().contains("on")) {
                return "on";
            }
        }
        return (position + "").replace(".0", "");
    }

    @Override
    public float getPositionForDimState(String dimState) {
        dimState = dimState.toLowerCase(Locale.getDefault());
        if (UPPER_BOUND_STATES.contains(dimState)) {
            return getDimUpperBound();
        } else if (LOWER_BOUND_STATES.contains(dimState)) {
            return getDimLowerBound();
        }
        return extractLeadingFloat(dimState);
    }

    public SliderSetListEntry getSlider() {
        return slider;
    }

    @Override
    public String getStateName() {
        return setListAttribute;
    }

    @Override
    public void switchTo(StateUiService stateUiService, Context context, FhemDevice fhemDevice, float state) {
        stateUiService.setSubState(fhemDevice, setListAttribute, getDimStateForPosition(fhemDevice, state), context);

        Optional<DeviceConfiguration> deviceConfiguration = fhemDevice.getDeviceConfiguration();
        if (deviceConfiguration.isPresent()) {
            Optional<ViewItemConfig> stateConfig = deviceConfiguration.get().stateConfigFor(slider.getKey());
            if (stateConfig.isPresent() && stateConfig.get().isShowDelayNotificationOnSwitch()) {
                DialogUtil.showAlertDialog(context, fhemDevice.getName(), context.getString(R.string.switchDelayNotification));
            }
        }
    }

    static Optional<ContinuousDimmableBehavior> behaviorFor(SetList setList) {
        for (String dimAttribute : DIM_ATTRIBUTES) {
            if (!setList.contains(dimAttribute)) {
                continue;
            }
            SetListEntry setListEntry = setList.get(dimAttribute);
            if (setListEntry instanceof SliderSetListEntry) {
                return Optional.of(new ContinuousDimmableBehavior((SliderSetListEntry) setListEntry, dimAttribute));
            }
            return Optional.absent();
        }
        return Optional.absent();
    }
}
