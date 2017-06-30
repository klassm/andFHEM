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

import li.klass.fhem.util.FloatUtils;

public abstract class DimmableDevice<D extends FhemDevice> extends ToggleableDevice<D> {
    public float getDimPosition() {
        float position = getPositionForDimStateInternal(getDimStateFieldValue());
        if (position == -1) {
            return 0;
        }
        return position;
    }

    public float getDimUpPosition() {
        float currentPosition = getDimPosition();
        if (currentPosition + getDimStep() > getDimUpperBound()) {
            return getDimUpperBound();
        }
        return currentPosition + getDimStep();
    }

    public float getDimDownPosition() {
        float currentPosition = getDimPosition();
        if (currentPosition - getDimStep() < getDimLowerBound()) {
            return getDimLowerBound();
        }
        return currentPosition - getDimStep();
    }

    public String getDimStateFieldValue() {
        return getState();
    }

    @Override
    public String formatTargetState(String targetState) {
        if (targetState.equals("dimup")) {
            return getDimStateNameForDimStateValue(getDimUpPosition());
        } else if (targetState.equals("dimdown")) {
            return getDimStateNameForDimStateValue(getDimDownPosition());
        }
        return super.formatTargetState(targetState);
    }

    @Override
    public String formatStateTextToSet(String stateToSet) {
        if (!supportsDim()) return super.formatStateTextToSet(stateToSet);

        float position = getPositionForDimStateInternal(stateToSet);
        if (FloatUtils.isEqual(position, getDimUpperBound())) {
            return "on";
        }
        if (FloatUtils.isEqual(position, getDimLowerBound())) {
            return "off";
        }
        return super.formatStateTextToSet(stateToSet);
    }

    public float getDimLowerBound() {
        return 0;
    }

    public abstract float getDimUpperBound();

    public float getDimStep() {
        return 1;
    }

    public float getPositionForDimStateInternal(String dimState) {
        if (dimState == null) return -1;
        if (dimState.equals("on")) return getDimUpperBound();
        if (dimState.equals("off")) return getDimLowerBound();

        return getPositionForDimState(dimState);
    }

    /**
     * Get the dim state for a given value. This is sent to FHEM within the set command!
     *
     * @param value value to look for
     * @return state for the given value.
     */
    public abstract String getDimStateNameForDimStateValue(float value);

    public abstract float getPositionForDimState(String dimState);

    public abstract boolean supportsDim();
}
