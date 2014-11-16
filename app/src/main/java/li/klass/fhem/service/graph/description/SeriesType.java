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

package li.klass.fhem.service.graph.description;

import android.graphics.Color;

import li.klass.fhem.AndFHEMApplication;
import li.klass.fhem.R;

public enum SeriesType {
    TEMPERATURE(Color.RED, R.string.yAxisTemperature),
    DESIRED_TEMPERATURE(Color.YELLOW, R.string.yAxisTemperature),
    HUMIDITY(Color.GREEN, R.string.yAxisHumidity),
    CURRENT_USAGE_WATT(Color.RED, R.string.yAxisCurrentUsageW),
    CURRENT_USAGE_KILOWATT(Color.RED, R.string.yAxisCurrentUsagekW),
    CURRENT_POWER_WATT(Color.BLUE, R.string.yAxisCurrentUsagekW),
    DAY_USAGE(Color.BLUE, R.string.yAxisCumulativeUsageKWh),
    CUMULATIVE_USAGE_KWh(Color.GREEN, R.string.yAxisCumulativeUsageKWh),
    CUMULATIVE_USAGE_Wh(Color.GREEN, R.string.yAxisCumulativeUsageWh),
    ACTUATOR(Color.BLUE, R.string.yAxisActuator),
    LITRE_CONTENT(Color.RED, R.string.yAxisLitreContent),
    RAW(Color.GREEN, R.string.yAxisRaw),
    TOGGLE_STATE(Color.RED, R.string.yAxisToggleState),
    WIND(Color.RED, R.string.yAxisWind),
    RAIN(Color.RED, R.string.yAxisRain),
    PRESSURE(Color.RED, R.string.yAxisPressure),
    PRESSURE_NN(Color.BLUE, R.string.yAxisPressureNN),
    RAIN_RATE(Color.RED, R.string.yAxisRainRate),
    RAIN_TOTAL(Color.RED, R.string.rainTotal),
    DEWPOINT(Color.YELLOW, R.string.yAxisTemperature),
    POWER(Color.RED, R.string.yAxisEnergy),
    BRIGHTNESS(Color.RED, R.string.brightness),
    SUNSHINE(Color.BLUE, R.string.sunshine),
    IS_RAINING(Color.BLUE, R.string.isRaining),
    CO2(Color.RED, R.string.yAxisCO2),
    WEIGHT(Color.RED, R.string.yAxisWeight),
    FAT_RATIO(Color.BLUE, R.string.yAxisFatRatio),
    WINDOW_OPEN(Color.BLUE, R.string.windowOpen),
    AVERAGE_HOUR(Color.CYAN, R.string.average),
    AVERAGE_DAY(Color.BLUE, R.string.average),
    AVERAGE_MONTH(Color.RED, R.string.average),
    AVERAGE_YEAR(Color.GREEN, R.string.average)
    ;

    private final int color;
    private final int yAxis;

    SeriesType(int color, int yAxis) {
        this.color = color;
        this.yAxis = yAxis;
    }

    public int getColor() {
        return color;
    }

    public String getYAxisName() {
        return AndFHEMApplication.getContext().getString(yAxis);
    }
}
