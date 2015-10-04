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

import com.google.common.base.Optional;

import li.klass.fhem.domain.core.DeviceFunctionality;

public enum CulHmSubType {
    DIMMER(DeviceFunctionality.DIMMER),
    SWITCH(DeviceFunctionality.SWITCH),
    SMOKE_DETECTOR(DeviceFunctionality.SMOKE_DETECTOR),
    THREE_STATE(DeviceFunctionality.WINDOW),
    TH(DeviceFunctionality.TEMPERATURE),
    THPL(DeviceFunctionality.TEMPERATURE),
    THERMOSTAT(DeviceFunctionality.HEATING),
    FILL_STATE(DeviceFunctionality.FILL_STATE),
    MOTION(DeviceFunctionality.MOTION_DETECTOR),
    KEYMATIC(DeviceFunctionality.KEY),
    POWERMETER(DeviceFunctionality.SWITCH),
    POWERSENSOR(DeviceFunctionality.USAGE),
    SHUTTER(DeviceFunctionality.WINDOW);

    private final DeviceFunctionality functionality;

    CulHmSubType(DeviceFunctionality functionality) {
        this.functionality = functionality;
    }

    public DeviceFunctionality getFunctionality() {
        return functionality;
    }

    public static Optional<CulHmSubType> subTypeFor(String value) {
        if ("DIMMER".equalsIgnoreCase(value) || "BLINDACTUATOR".equalsIgnoreCase(value)) {
            return Optional.of(DIMMER);
        } else if ("SWITCH".equalsIgnoreCase(value)) {
            return Optional.of(SWITCH);
        } else if ("SMOKEDETECTOR".equalsIgnoreCase(value)) {
            return Optional.of(CulHmSubType.SMOKE_DETECTOR);
        } else if ("THREESTATESENSOR".equalsIgnoreCase(value)) {
            return Optional.of(CulHmSubType.THREE_STATE);
        } else if ("THSensor".equalsIgnoreCase(value)) {
            return Optional.of(CulHmSubType.TH);
        } else if ("KFM100".equalsIgnoreCase(value)) {
            return Optional.of(CulHmSubType.FILL_STATE);
        } else if ("THERMOSTAT".equalsIgnoreCase(value)) {
            return Optional.of(THERMOSTAT);
        } else if ("MOTIONDETECTOR".equalsIgnoreCase(value)) {
            return Optional.of(MOTION);
        } else if (("KEYMATIC").equalsIgnoreCase(value)) {
            return Optional.of(KEYMATIC);
        } else if ("powerMeter".equalsIgnoreCase(value)) {
            return Optional.of(POWERMETER);
        } else if ("THPLSensor".equalsIgnoreCase(value)) {
            return Optional.of(THPL);
        } else if ("powerSensor".equalsIgnoreCase(value)) {
            return Optional.of(POWERSENSOR);
        }
        return Optional.absent();
    }
}
