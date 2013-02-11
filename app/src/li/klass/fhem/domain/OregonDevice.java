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
import li.klass.fhem.appwidget.annotation.ResourceIdMapper;
import li.klass.fhem.appwidget.annotation.SupportsWidget;
import li.klass.fhem.appwidget.annotation.WidgetTemperatureField;
import li.klass.fhem.appwidget.view.widget.medium.TemperatureWidgetView;
import li.klass.fhem.domain.core.Device;
import li.klass.fhem.domain.core.DeviceChart;
import li.klass.fhem.domain.genericview.FloorplanViewSettings;
import li.klass.fhem.domain.genericview.ShowField;
import li.klass.fhem.service.graph.description.ChartSeriesDescription;
import li.klass.fhem.util.ValueDescriptionUtil;

import java.util.List;

@FloorplanViewSettings(showState = true)
@SupportsWidget(TemperatureWidgetView.class)
@SuppressWarnings("unused")
public class OregonDevice extends Device<OregonDevice> {

    @ShowField(description = ResourceIdMapper.humidity, showInOverview = true)
    private String humidity;
    @WidgetTemperatureField
    @ShowField(description = ResourceIdMapper.temperature, showInOverview = true)
    private String temperature;
    @ShowField(description = ResourceIdMapper.forecast, showInOverview = true)
    private String forecast;
    @ShowField(description = ResourceIdMapper.dewpoint)
    private String dewpoint;
    @ShowField(description = ResourceIdMapper.pressure)
    private String pressure;
    @ShowField(description = ResourceIdMapper.battery)
    private String battery;
    @ShowField(description = ResourceIdMapper.rainRate, showInOverview = true)
    private String rainRate;
    @ShowField(description = ResourceIdMapper.rainTotal, showInOverview = true)
    private String rainTotal;
    @ShowField(description = ResourceIdMapper.windAvgSpeed, showInOverview = true)
    private String windAvgSpeed;
    @ShowField(description = ResourceIdMapper.windDirection, showInOverview = true)
    private String windDirection;
    @ShowField(description = ResourceIdMapper.windSpeed, showInOverview = true)
    private String windSpeed;
    @ShowField(description = ResourceIdMapper.uvValue, showInOverview = true)
    private String uvValue;
    @ShowField(description = ResourceIdMapper.uvRisk, showInOverview = true)
    private String uvRisk;

    public void readTEMPERATURE(String value) {
        this.temperature = ValueDescriptionUtil.appendTemperature(value);
    }

    public void readHUMIDITY(String value) {
        this.humidity = ValueDescriptionUtil.appendPercent(value);
    }

    public void readFORECAST(String value) {
        this.forecast = value;
    }

    public void readDEWPOINT(String value) {
        this.dewpoint = ValueDescriptionUtil.appendTemperature(value);
    }

    public void readPRESSURE(String value) {
        this.pressure = ValueDescriptionUtil.append(value, "hPa");
    }

    public void readBATTERY(String value) {
        this.battery = ValueDescriptionUtil.appendPercent(value);
    }

    public void readRAIN_RATE(String value) {
        this.rainRate = ValueDescriptionUtil.append(value, "mm/h");
    }

    public void readRAIN_TOTAL(String value) {
        this.rainTotal = ValueDescriptionUtil.append(value, "l/m2");
    }

    public void readWIND_AVSPEED(String value) {
        this.windAvgSpeed = ValueDescriptionUtil.appendKmH(value);
    }

    public void readWIND_DIR(String value) {
        this.windDirection = value;
    }

    public void readWIND_SPEED(String value) {
        this.windSpeed = ValueDescriptionUtil.appendKmH(value);
    }

    public void readUV_VAL(String value) {
        this.uvValue = value;
    }

    public void readUV_RISK(String value) {
        this.uvRisk = value;
    }

    public void readTIME(String value) {
        this.measured = value;
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
        addDeviceChartIfNotNull(new DeviceChart(R.string.temperatureGraph,
                ChartSeriesDescription.getRegressionValuesInstance(R.string.temperature, "4:temperature:0:", R.string.yAxisTemperature)), temperature);
        addDeviceChartIfNotNull(new DeviceChart(R.string.humidityGraph,
                new ChartSeriesDescription(R.string.temperature, "4:humidity:0:", R.string.yAxisHumidity)), humidity);
        addDeviceChartIfNotNull(new DeviceChart(R.string.pressureGraph,
                new ChartSeriesDescription(R.string.pressure, "4:pressure:0:", R.string.yAxisPressure)), pressure);
        addDeviceChartIfNotNull(new DeviceChart(R.string.rainRate,
                new ChartSeriesDescription(R.string.rainRate, "4:rain_rate:0:", R.string.yAxisRainRate)), rainRate);
        addDeviceChartIfNotNull(new DeviceChart(R.string.rainTotal,
                new ChartSeriesDescription(R.string.rainRate, "4:rain_total:0:", R.string.yAxisRainTotal)), rainTotal);
        addDeviceChartIfNotNull(new DeviceChart(R.string.windSpeed,
                new ChartSeriesDescription(R.string.rainRate, "4:wind_speed:0:", R.string.yAxisWindSpeed)), windSpeed);
    }
}
