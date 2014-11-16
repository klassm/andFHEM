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

package li.klass.fhem.util;

public class ValueDescriptionUtil {

    public static final String C = "°C";
    public static final String PERCENT = "%";
    public static final String KM_H = "km/h";
    public static final String M_S = "m/s";
    public static final String L_M2 = "l/m2";
    public static final String L = "l";
    public static final String KWH = "kWh";
    public static final String LUX = "lux";
    public static final String PPM = "ppm";
    public static final String DB = "dB";

    public static String appendPercent(Object text) {
        return append(text, PERCENT);
    }

    public static String append(Object text, String appendix) {
        return text + " (" + appendix + ")";
    }

    public static String appendKmH(Object text) {
        return append(text, KM_H);
    }

    public static String appendLm2(Object text) {
        return append(text, L_M2);
    }

    public static String appendL(Object text) {
        return append(text, L);
    }

    public static String appendKWh(Object text) {
        return append(text, KWH);
    }

    public static String appendPpm(Object text) {
        return append(text, PPM);
    }

    public static String appendDb(Object text) {
        return append(text, DB);
    }

    public static String appendHPa(Object text) {
        return append(text, "hPa");
    }

    public static String appendLm(String luminosity) {
        return append(luminosity, "lm");
    }

    public static String appendV(String voltage) {
        return append(voltage, "V");
    }

    public static String appendW(String power) {
        return append(power, "W");
    }

    public static String appendHz(String frequency) {
        return append(frequency, "Hz");
    }

    public static String desiredTemperatureToString(double temperature, double minTemp, double maxTemp) {
        if (temperature == minTemp) {
            return "off";
        } else if (temperature == maxTemp) {
            return "on";
        } else {
            return ValueDescriptionUtil.appendTemperature(temperature);
        }
    }

    public static String appendTemperature(Object text) {
        return append(text, C);
    }

    public static String secondsToTimeString(int seconds) {
        int hours;
        int minutes;

        hours = seconds / 3600;
        seconds -= (hours * 3600);

        minutes = seconds / 60;
        seconds -= (minutes * 60);

        String out = "";

        if (hours > 0) {
            out = appendToString(out, hours + " (h)");
        }

        if (minutes > 0) {
            out = appendToString(out, minutes + " (m)");
        }

        if (seconds > 0) {
            out = appendToString(out, seconds + " (s)");
        }

        return out;
    }

    private static String appendToString(String string, String toAppend) {
        if (! StringUtil.isBlank(string)) {
            string += " ";
        }

        return string + toAppend;
    }
}
