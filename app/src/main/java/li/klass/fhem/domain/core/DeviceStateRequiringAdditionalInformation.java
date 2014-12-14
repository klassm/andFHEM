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


import java.util.Locale;

import static li.klass.fhem.domain.core.DeviceStateAdditionalInformationType.ANY;
import static li.klass.fhem.domain.core.DeviceStateAdditionalInformationType.DEC_QUARTER;
import static li.klass.fhem.domain.core.DeviceStateAdditionalInformationType.NUMERIC;
import static li.klass.fhem.domain.core.DeviceStateAdditionalInformationType.TEMPERATURE;
import static li.klass.fhem.domain.core.DeviceStateAdditionalInformationType.TIME;
import static li.klass.fhem.domain.core.DeviceStateAdditionalInformationType.TIME_WITH_SECOND;

public enum DeviceStateRequiringAdditionalInformation {
    PCT("pct", NUMERIC),
    VALVE("valve", NUMERIC),
    FACTORS("factors", ANY),
    DESIRED("desired", NUMERIC),
    ON_FOR_TIMER("on-for-timer", DEC_QUARTER),
    OFF_FOR_TIMER("off-for-timer", DEC_QUARTER),
    ON_TILL("on-till", TIME, TIME_WITH_SECOND),
    OFF_TILL("off-till", TIME, TIME_WITH_SECOND),
    RAMP_ON_TIME("ramp-on-time", NUMERIC),
    RAMP_OFF_TIME("ramp-off-time", NUMERIC),
    DAY("day", NUMERIC),
    POWER("power", ANY),
    INPUT("input", ANY),
    AUDIO("audio", ANY),
    DAY_TEMP("day-temp", TEMPERATURE),
    DESIRED_TEMP("desired-temp", TEMPERATURE),
    GENERAL_TEMP(".*temperature", TEMPERATURE),
    FRI_FROM1("fri-from1", TIME),
    FRI_FROM2("fri-from2", TIME),
    FRI_TO1("fri-to1", TIME),
    FRI_TO2("fri-to2", TIME),
    HOLIDAY1("holiday1", NUMERIC),
    HOLIDAY2("holiday2"),
    HOUR("hour", NUMERIC),
    LOWTEMP_OFFSET("lowtemp-offset", TEMPERATURE),
    MANU_TEMP("manu-temp", TEMPERATURE),
    MINUTE("minute", NUMERIC),
    MODE("mode", ANY),
    MON_FROM1("mon-from1", TIME),
    MON_FROM2("mon-from2", TIME),
    MON_TO1("mon-to1", TIME),
    MON_TO2("mon-to2", TIME),
    MONTH("month", NUMERIC),
    NIGHT_TEMP("night-temp", TEMPERATURE),
    SAT_FROM1("sat-from1", TIME),
    SAT_FROM2("sat-from2", TIME),
    SAT_TO1("sat-to1", TIME),
    SAT_TO2("sat-to2", TIME),
    SUN_FROM1("sun-from1", TIME),
    SUN_FROM2("sun-from2", TIME),
    SUN_TO1("sun-to1", TIME),
    SUN_TO2("sun-to2", TIME),
    THU_FROM1("thu-from1", TIME),
    THU_FROM2("thu-from2", TIME),
    THU_TO1("thu-to1", TIME),
    THU_TO2("thu-to2", TIME),
    TUE_FROM1("tue-from1", TIME),
    TUE_FROM2("tue-from2", TIME),
    TUE_TO1("tue-to1", TIME),
    TUE_TO2("tue-to2", TIME),
    WED_FROM1("wed-from1", TIME),
    WED_FROM2("wed-from2", TIME),
    WED_TO1("wed-to1", TIME),
    WED_TO2("wed-to2", TIME),
    WINDOWOPEN_TEMP("windowopen-temp", TEMPERATURE),
    YEAR("year", NUMERIC);

    private String fhemStateRegexp = null;
    private DeviceStateAdditionalInformationType[] additionalInformationTypes;

    private DeviceStateRequiringAdditionalInformation(String fhemStateRegexp, DeviceStateAdditionalInformationType... additionalInformationTypes) {
        this.fhemStateRegexp = fhemStateRegexp;
        this.additionalInformationTypes = additionalInformationTypes;
    }

    public static DeviceStateRequiringAdditionalInformation deviceStateForFHEM(String state) {
        if (state == null) return null;
        state = state.toLowerCase(Locale.getDefault());
        for (DeviceStateRequiringAdditionalInformation deviceState : values()) {
            if (state.matches(deviceState.fhemStateRegexp)) {
                return deviceState;
            }
        }
        return null;
    }

    public DeviceStateAdditionalInformationType[] getAdditionalInformationTypes() {
        return additionalInformationTypes;
    }

    public static boolean requiresAdditionalInformation(String state) {
        return deviceStateForFHEM(state) != null;
    }

    public static boolean isValidAdditionalInformationValue(String value,
                                                            DeviceStateRequiringAdditionalInformation specialDeviceState) {

        for (DeviceStateAdditionalInformationType type : specialDeviceState.getAdditionalInformationTypes()) {
            if (type.matches(value.toLowerCase())) {
                return true;
            }
        }

        return false;
    }
}

