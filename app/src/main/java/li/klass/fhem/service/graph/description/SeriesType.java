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
    RAIN_RATE(Color.RED, R.string.yAxisRainRate),
    RAIN_TOTAL(Color.RED, R.string.rainTotal),
    DEWPOINT(Color.GREEN, R.string.yAxisTemperature),
    POWER(Color.RED, R.string.yAxisEnergy),
    BRIGHTNESS(Color.RED, R.string.brightness),
    SUNSHINE(Color.BLUE, R.string.sunshine),
    IS_RAINING(Color.BLUE, R.string.isRaining)
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
