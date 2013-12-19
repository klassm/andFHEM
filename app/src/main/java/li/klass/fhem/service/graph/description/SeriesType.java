package li.klass.fhem.service.graph.description;

import android.graphics.Color;

import li.klass.fhem.AndFHEMApplication;
import li.klass.fhem.R;

public enum SeriesType {
    TEMPERATURE(Color.RED, R.string.yAxisTemperature),
    DESIRED_TEMPERATURE(Color.YELLOW, R.string.yAxisTemperature),
    HUMIDITY(Color.GREEN, R.string.yAxisHumidity),
    CURRENT_USAGE(Color.RED, R.string.yAxisCurrentUsage),
    DAY_USAGE(Color.BLUE, R.string.yAxisCumulativeUsage),
    CUMULATIVE_USAGE(Color.GREEN, R.string.yAxisCumulativeUsage),
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
    POWER(Color.RED, R.string.yAxisEnergy);

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
