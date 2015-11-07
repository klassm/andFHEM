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

package li.klass.fhem.adapter.devices.toggle;

import java.util.List;
import java.util.Locale;

import javax.inject.Inject;
import javax.inject.Singleton;

import li.klass.fhem.adapter.devices.hook.DeviceHookProvider;
import li.klass.fhem.domain.core.FhemDevice;

import static com.google.common.collect.Lists.newArrayList;

@Singleton
public class OnOffBehavior {
    @Inject
    DeviceHookProvider hookProvider;

    @Inject
    public OnOffBehavior() {
    }

    public boolean isOnByState(FhemDevice device) {
        return !isOffByState(device);
    }

    public boolean isOffByState(FhemDevice device) {
        String internalState = device.getInternalState().toLowerCase(Locale.getDefault());

        if (internalState.equalsIgnoreCase("???")) {
            return true;
        }

        for (String offState : getOffStateNames(device)) {
            if (internalState.contains(offState.toLowerCase(Locale.getDefault()))) {
                return true;
            }
        }
        return false;
    }

    public boolean isOn(FhemDevice device) {
        boolean isOn = isOnByState(device);
        if (hookProvider.invertState(device)) {
            isOn = !isOn;
        }

        return isOn;
    }

    private List<String> getOffStateNames(FhemDevice device) {
        List<String> offStateNames = newArrayList("off");
        String offStateName = hookProvider.getOffStateName(device);
        if (offStateName != null) {
            offStateNames.add(offStateName.toLowerCase(Locale.getDefault()));
        }

        for (String state : newArrayList(offStateNames)) {
            String reverseEventMapState = device.getReverseEventMapStateFor(state);
            if (!state.equalsIgnoreCase(reverseEventMapState)) {
                offStateNames.add(reverseEventMapState);
            }
        }

        return offStateNames;
    }

    public static boolean supports(FhemDevice device) {
        return device.getSetList().contains("on", "off");
    }
}
