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

package li.klass.fhem.domain

import li.klass.fhem.domain.core.DeviceFunctionality

enum class CulHmSubType(val functionality: DeviceFunctionality) {
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

    companion object {

        fun subTypeFor(value: String): CulHmSubType? = when {
            "DIMMER".equals(value, ignoreCase = true) || "BLINDACTUATOR".equals(value, ignoreCase = true) -> DIMMER
            "SWITCH".equals(value, ignoreCase = true) -> SWITCH
            "SMOKEDETECTOR".equals(value, ignoreCase = true) -> CulHmSubType.SMOKE_DETECTOR
            "THREESTATESENSOR".equals(value, ignoreCase = true) -> CulHmSubType.THREE_STATE
            "THSensor".equals(value, ignoreCase = true) -> CulHmSubType.TH
            "KFM100".equals(value, ignoreCase = true) -> CulHmSubType.FILL_STATE
            "THERMOSTAT".equals(value, ignoreCase = true) -> THERMOSTAT
            "MOTIONDETECTOR".equals(value, ignoreCase = true) -> MOTION
            "KEYMATIC".equals(value, ignoreCase = true) -> KEYMATIC
            "powerMeter".equals(value, ignoreCase = true) -> POWERMETER
            "THPLSensor".equals(value, ignoreCase = true) -> THPL
            "powerSensor".equals(value, ignoreCase = true) -> POWERSENSOR
            else -> null
        }
    }
}
