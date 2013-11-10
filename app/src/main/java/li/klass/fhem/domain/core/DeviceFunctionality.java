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

public enum DeviceFunctionality {
    SWITCH,
    DIMMER,
    WEATHER,
    HEATING,
    TEMPERATURE,
    NETWORK,
    USAGE,
    WINDOW,
    SMOKE_DETECTOR,
    FILL_STATE,
    MOTION_DETECTOR,
    KEY,
    DUMMY,
    CALL_MONITOR,
    FHEM,
    LOG,
    FLOORPLAN,
    REMOTE_CONTROL,

    UNKNOWN;

    public static DeviceFunctionality functionalityForDimmable(DimmableDevice device) {
        if (device.supportsDim()) {
            return DeviceFunctionality.DIMMER;
        }
        if (device.supportsToggle()) {
            return DeviceFunctionality.SWITCH;
        }
        return DeviceFunctionality.DUMMY;
    }
}
