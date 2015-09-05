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

package li.klass.fhem.domain;

import android.content.Context;

import java.util.Map;

import li.klass.fhem.adapter.devices.toggle.OnOffBehavior;
import li.klass.fhem.behavior.dim.DimmableBehavior;
import li.klass.fhem.domain.core.DeviceFunctionality;
import li.klass.fhem.domain.core.DimmableContinuousStatesDevice;
import li.klass.fhem.domain.genericview.OverviewViewSettings;
import li.klass.fhem.domain.genericview.OverviewViewSettingsCache;
import li.klass.fhem.domain.setlist.SetList;
import li.klass.fhem.domain.setlist.SetListSliderValue;
import li.klass.fhem.service.deviceConfiguration.DeviceConfiguration;
import li.klass.fhem.service.room.xmllist.DeviceNode;

import static li.klass.fhem.behavior.dim.DimmableBehavior.isDimDisabled;

@OverviewViewSettings(showState = true, showMeasured = true)
public class GenericDevice extends DimmableContinuousStatesDevice<GenericDevice> {
    @Override
    public DeviceFunctionality getDeviceGroup() {
        if (DimmableBehavior.behaviorFor(this).isPresent() && !isDimDisabled(this)) {
            return DeviceFunctionality.DIMMER;
        } else if (OnOffBehavior.supports(this)) {
            return DeviceFunctionality.SWITCH;
        } else if (deviceConfiguration.isPresent()) {
            return deviceConfiguration.get().getDefaultGroup();
        } else {
            return DeviceFunctionality.UNKNOWN;
        }
    }

    @Override
    public void afterDeviceXMLRead(Context context) {
        super.afterDeviceXMLRead(context);
        Map<String, DeviceNode> states = getXmlListDevice().getStates();


        DeviceNode node = states.containsKey("state") ? states.get("state") : getMostRecentlyMeasuredNode();
        if (node != null) {
            setMeasured(node.getMeasured());
        }
    }

    private DeviceNode getMostRecentlyMeasuredNode() {
        Map<String, DeviceNode> states = getXmlListDevice().getStates();
        if (states.isEmpty()) return null;

        DeviceNode mostRecent = null;
        for (DeviceNode node : states.values()) {
            if (mostRecent == null || (node.getMeasured() != null && node.getMeasured().isAfter(mostRecent.getMeasured()))) {
                mostRecent = node;
            }
        }
        return mostRecent;
    }

    @Override
    protected String getSetListDimStateAttributeName() {
        SetList setList = getSetList();
        if (setList.contains("dim") && setList.get("dim") instanceof SetListSliderValue) {
            return "dim";
        }
        return super.getSetListDimStateAttributeName();
    }

    @Override
    protected OverviewViewSettings getExplicitOverviewSettings() {
        boolean showState = true;
        boolean showMeasured = true;

        if (deviceConfiguration.isPresent()) {
            DeviceConfiguration conf = deviceConfiguration.get();
            showState = conf.isShowStateInOverview();
            showMeasured = conf.isShowMeasuredInOverview();
        }
        return new OverviewViewSettingsCache(showState, showMeasured);
    }


}
