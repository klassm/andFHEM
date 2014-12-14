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

import li.klass.fhem.R;

public enum DeviceFunctionality {
    SWITCH(R.string.functionalitySwitch),
    DIMMER(R.string.functionalityDimmer),
    WEATHER(R.string.functionalityWeather),
    HEATING(R.string.functionalityHeating),
    TEMPERATURE(R.string.functionalityTemperature),
    NETWORK(R.string.functionalityNetwork),
    USAGE(R.string.functionalityUsage),
    WINDOW(R.string.functionalityWindow),
    SMOKE_DETECTOR(R.string.functionalitySmokeDetector),
    FILL_STATE(R.string.functionalityFillState),
    MOTION_DETECTOR(R.string.functionalityMotionDetector),
    KEY(R.string.functionalityKey),
    DUMMY(R.string.functionalityDummy),
    CALL_MONITOR(R.string.functionalityCallMonitor),
    FHEM(R.string.functionalityFHEM),
    LOG(R.string.functionalityLog),
    FLOORPLAN(R.string.functionalityFloorplan),
    REMOTE_CONTROL(R.string.functionalityRemoteControl),
    WEB_LINK(R.string.functionalityWebView),
    UNKNOWN(R.string.functionalityUnknown),
    PRESENCE(R.string.functionalityPresence);

    private final int captionId;

    DeviceFunctionality(int captionId) {
        this.captionId = captionId;
    }

    public static DeviceFunctionality functionalityForDimmable(DimmableDevice device) {
        if (device.isSpecialButtonDevice()) return SWITCH;
        if (device.supportsDim()) {
            return DIMMER;
        }
        if (device.supportsToggle()) {
            return SWITCH;
        }
        return DUMMY;
    }

    public String getCaptionText(Context context) {
        return context.getString(captionId);
    }
}
