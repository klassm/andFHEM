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

import android.content.Context;

import static li.klass.fhem.R.string.functionalityCallMonitor;
import static li.klass.fhem.R.string.functionalityDimmer;
import static li.klass.fhem.R.string.functionalityDummy;
import static li.klass.fhem.R.string.functionalityFHEM;
import static li.klass.fhem.R.string.functionalityFillState;
import static li.klass.fhem.R.string.functionalityFloorplan;
import static li.klass.fhem.R.string.functionalityHeating;
import static li.klass.fhem.R.string.functionalityKey;
import static li.klass.fhem.R.string.functionalityLog;
import static li.klass.fhem.R.string.functionalityMotionDetector;
import static li.klass.fhem.R.string.functionalityNetwork;
import static li.klass.fhem.R.string.functionalityRemoteControl;
import static li.klass.fhem.R.string.functionalitySmokeDetector;
import static li.klass.fhem.R.string.functionalitySwitch;
import static li.klass.fhem.R.string.functionalityTemperature;
import static li.klass.fhem.R.string.functionalityUnknown;
import static li.klass.fhem.R.string.functionalityUsage;
import static li.klass.fhem.R.string.functionalityWeather;
import static li.klass.fhem.R.string.functionalityWindow;

public enum DeviceFunctionality {
    SWITCH(functionalitySwitch),
    DIMMER(functionalityDimmer),
    WEATHER(functionalityWeather),
    HEATING(functionalityHeating),
    TEMPERATURE(functionalityTemperature),
    NETWORK(functionalityNetwork),
    USAGE(functionalityUsage),
    WINDOW(functionalityWindow),
    SMOKE_DETECTOR(functionalitySmokeDetector),
    FILL_STATE(functionalityFillState),
    MOTION_DETECTOR(functionalityMotionDetector),
    KEY(functionalityKey),
    DUMMY(functionalityDummy),
    CALL_MONITOR(functionalityCallMonitor),
    FHEM(functionalityFHEM),
    LOG(functionalityLog),
    FLOORPLAN(functionalityFloorplan),
    REMOTE_CONTROL(functionalityRemoteControl),

    UNKNOWN(functionalityUnknown);

    private final int captionId;

    DeviceFunctionality(int captionId) {
        this.captionId = captionId;
    }

    public static DeviceFunctionality functionalityForDimmable(DimmableDevice device) {
        if (device.supportsDim()) {
            return DeviceFunctionality.DIMMER;
        }
        if (device.supportsToggle()) {
            return DeviceFunctionality.SWITCH;
        }
        return DeviceFunctionality.DUMMY;
    }

    public String getCaptionText(Context context) {
        return context.getString(captionId);
    }
}
