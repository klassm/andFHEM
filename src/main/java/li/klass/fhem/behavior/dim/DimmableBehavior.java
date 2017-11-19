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

import li.klass.fhem.adapter.uiservice.StateUiService;
import li.klass.fhem.domain.core.FhemDevice;
import li.klass.fhem.domain.setlist.SetList;
import li.klass.fhem.domain.setlist.typeEntry.SliderSetListEntry;
import li.klass.fhem.update.backend.xmllist.DeviceNode;

public class DimmableBehavior {

    private final FhemDevice fhemDevice;
    private final String connectionId;
    private DimmableTypeBehavior behavior;

    private DimmableBehavior(FhemDevice fhemDevice, String connectionId, DimmableTypeBehavior dimmableTypeBehavior) {
        this.connectionId = connectionId;
        this.behavior = dimmableTypeBehavior;
        this.fhemDevice = fhemDevice;
    }

    public float getCurrentDimPosition() {
        return behavior.getCurrentDimPosition(fhemDevice);
    }

    float getDimUpPosition() {
        float currentPosition = getCurrentDimPosition();
        if (currentPosition + getDimStep() > behavior.getDimUpperBound()) {
            return behavior.getDimUpperBound();
        }
        return currentPosition + getDimStep();
    }

    float getDimDownPosition() {
        float currentPosition = getCurrentDimPosition();
        if (currentPosition - getDimStep() < behavior.getDimLowerBound()) {
            return behavior.getDimLowerBound();
        }
        return currentPosition - getDimStep();
    }


    public String getDimStateForPosition(float position) {
        return behavior.getDimStateForPosition(fhemDevice, position);
    }

    public float getPositionForDimState(String dimState) {
        return behavior.getPositionForDimState(dimState);
    }

    public float getDimLowerBound() {
        return behavior.getDimLowerBound();
    }

    public float getDimUpperBound() {
        return behavior.getDimUpperBound();
    }

    public float getDimStep() {
        return behavior.getDimStep();
    }

    public void switchTo(StateUiService stateUiService, Context context, float state) {
        behavior.switchTo(stateUiService, context, fhemDevice, connectionId, state);
    }

    public FhemDevice getFhemDevice() {
        return fhemDevice;
    }

    DimmableTypeBehavior getBehavior() {
        return behavior;
    }

    public static Optional<DimmableBehavior> behaviorFor(FhemDevice fhemDevice, String connectionId) {
        SetList setList = fhemDevice.getXmlListDevice().getSetList();

        Optional<DiscreteDimmableBehavior> discrete = DiscreteDimmableBehavior.behaviorFor(setList);
        if (discrete.isPresent()) {
            return Optional.of(new DimmableBehavior(fhemDevice, connectionId, discrete.get()));
        }

        Optional<ContinuousDimmableBehavior> continuous = ContinuousDimmableBehavior.behaviorFor(setList);
        if (continuous.isPresent()) {
            DimmableTypeBehavior behavior = continuous.get();
            return Optional.of(new DimmableBehavior(fhemDevice, connectionId, behavior));
        }

        return Optional.absent();
    }

    public static Optional<DimmableBehavior> continuousBehaviorFor(FhemDevice device, String attribute, String connectionId) {
        SetList setList = device.getXmlListDevice().getSetList();
        if (!setList.contains(attribute)) {
            return Optional.absent();
        }
        SliderSetListEntry setListSliderValue = (SliderSetListEntry) setList.get(attribute, true);
        return Optional.of(new DimmableBehavior(device, connectionId, new ContinuousDimmableBehavior(setListSliderValue, attribute)));
    }


    public static boolean isDimDisabled(FhemDevice device) {
        DeviceNode disableDim = device.getXmlListDevice().getAttributes().get("disableDim");
        return disableDim != null && "true".equalsIgnoreCase(disableDim.getValue());
    }
}
