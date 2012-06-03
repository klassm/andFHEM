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

import li.klass.fhem.R;
import li.klass.fhem.domain.genericview.FloorplanViewSettings;
import li.klass.fhem.domain.genericview.ShowField;
import li.klass.fhem.service.graph.description.ChartSeriesDescription;
import li.klass.fhem.util.ValueDescriptionUtil;
import org.w3c.dom.NamedNodeMap;

import java.util.List;

@FloorplanViewSettings(showState = true)
public class OregonDevice extends Device<OregonDevice> {

    @ShowField(description = R.string.humidity, showInOverview = true)
    private String humidity;
    @ShowField(description = R.string.temperature, showInOverview = true)
    private String temperature;
    @ShowField(description = R.string.forecast, showInOverview = true)
    private String forecast;
    @ShowField(description = R.string.dewpoint)
    private String dewpoint;
    @ShowField(description = R.string.pressure)
    private String pressure;
    @ShowField(description = R.string.battery)
    private String battery;
    @ShowField(description = R.string.rainRate, showInOverview = true)
    private String rainRate;
    @ShowField(description = R.string.rainTotal, showInOverview = true)
    private String rainTotal;
    @ShowField(description = R.string.windAvgSpeed, showInOverview = true)
    private String windAvgSpeed;
    @ShowField(description = R.string.windDirection, showInOverview = true)
    private String windDirection;
    @ShowField(description = R.string.windSpeed, showInOverview = true)
    private String windSpeed;
    @ShowField(description = R.string.uvValue, showInOverview = true)
    private String uvValue;
    @ShowField(description = R.string.uvRisk, showInOverview = true)
    private String uvRisk;

    @Override
    protected void onChildItemRead(String tagName, String keyValue, String nodeContent, NamedNodeMap attributes) {
        if (keyValue.equals("TEMPERATURE")) {
            this.temperature = ValueDescriptionUtil.appendTemperature(nodeContent);
        } else if (keyValue.equals("HUMIDITY")) {
            this.humidity = ValueDescriptionUtil.appendPercent(nodeContent);
        } else if (keyValue.equals("FORECAST")) {
            this.forecast = nodeContent;
        } else if (keyValue.equals("DEWPOINT")) {
            this.dewpoint = nodeContent + " (Celsius)";
        } else if (keyValue.equals("PRESSURE")) {
            this.pressure = nodeContent + " (hPa)";
        } else if (keyValue.equals("BATTERY")) {
            this.battery = nodeContent + " (%)";
        } else if (keyValue.equals("RAIN_RATE")) {
            this.rainRate = nodeContent + " (mm/hr)";
        } else if (keyValue.equals("RAIN_TOTAL")) {
            this.rainTotal = nodeContent + " (l/m2)";
        } else if (keyValue.equals("WIND_AVSPEED")) {
            this.windAvgSpeed = nodeContent + " (km/h)";
        } else if (keyValue.equals("WIND_DIR")) {
            this.windDirection = nodeContent;
        } else if (keyValue.equals("WIND_SPEED")) {
            this.windSpeed = nodeContent + " (km/h)";
        } else if (keyValue.equals("UV_VAL")) {
            this.uvValue = nodeContent;
        } else if (keyValue.equals("UV_RISK")) {
            this.uvRisk = nodeContent;
        } else if (keyValue.equals("TIME")) {
            this.measured = nodeContent;
        }
    }

    public String getHumidity() {
        return humidity;
    }

    public String getTemperature() {
        return temperature;
    }

    public String getForecast() {
        return forecast;
    }

    public String getDewpoint() {
        return dewpoint;
    }

    public String getPressure() {
        return pressure;
    }

    public String getBattery() {
        return battery;
    }

    public String getRainRate() {
        return rainRate;
    }

    public String getRainTotal() {
        return rainTotal;
    }

    public String getWindAvgSpeed() {
        return windAvgSpeed;
    }

    public String getWindDirection() {
        return windDirection;
    }

    public String getWindSpeed() {
        return windSpeed;
    }

    public String getUvValue() {
        return uvValue;
    }

    public String getUvRisk() {
        return uvRisk;
    }

    @Override
    protected void fillDeviceCharts(List<DeviceChart> chartSeries) {
        addDeviceChartIfNotNull(temperature, new DeviceChart(R.string.temperatureGraph, R.string.yAxisTemperature,
                ChartSeriesDescription.getRegressionValuesInstance(R.string.temperature, "4:temperature:0:")));
        addDeviceChartIfNotNull(humidity, new DeviceChart(R.string.humidityGraph, R.string.yAxisHumidity,
                new ChartSeriesDescription(R.string.temperature, "4:humidity:0:")));
        addDeviceChartIfNotNull(pressure, new DeviceChart(R.string.pressureGraph, R.string.yAxisPressure,
                new ChartSeriesDescription(R.string.pressure, "4:pressure:0:")));
        addDeviceChartIfNotNull(rainRate, new DeviceChart(R.string.rainRate, R.string.yAxisRainRate,
                new ChartSeriesDescription(R.string.rainRate, "4:rain_rate:0:")));
        addDeviceChartIfNotNull(rainTotal, new DeviceChart(R.string.rainTotal, R.string.yAxisRainTotal,
                new ChartSeriesDescription(R.string.rainRate, "4:rain_total:0:")));
        addDeviceChartIfNotNull(windSpeed, new DeviceChart(R.string.windSpeed, R.string.yAxisWindSpeed,
                new ChartSeriesDescription(R.string.rainRate, "4:wind_speed:0:")));
    }
}
