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

import java.util.List;

import li.klass.fhem.R;
import li.klass.fhem.appwidget.annotation.ResourceIdMapper;
import li.klass.fhem.appwidget.annotation.SupportsWidget;
import li.klass.fhem.appwidget.annotation.WidgetTemperatureField;
import li.klass.fhem.appwidget.view.widget.medium.TemperatureWidgetView;
import li.klass.fhem.domain.core.Device;
import li.klass.fhem.domain.core.DeviceChart;
import li.klass.fhem.domain.core.DeviceFunctionality;
import li.klass.fhem.domain.genericview.ShowField;
import li.klass.fhem.service.graph.description.ChartSeriesDescription;
import li.klass.fhem.util.ValueDescriptionUtil;

import static li.klass.fhem.service.graph.description.SeriesType.DEWPOINT;
import static li.klass.fhem.service.graph.description.SeriesType.HUMIDITY;
import static li.klass.fhem.service.graph.description.SeriesType.PRESSURE;
import static li.klass.fhem.service.graph.description.SeriesType.TEMPERATURE;

@SupportsWidget(TemperatureWidgetView.class)
@SuppressWarnings("unused")
public class TRXWeatherDevice extends Device<TRXWeatherDevice> {

    @WidgetTemperatureField
    @ShowField(description = ResourceIdMapper.temperature, showInOverview = true)
    private String temperature;

    @ShowField(description = ResourceIdMapper.battery, showInOverview = true)
    private String battery;

    @ShowField(description = ResourceIdMapper.humidity, showInOverview = true)
    private String humidity;

    @ShowField(description = ResourceIdMapper.dewpoint, showInOverview = false)
    private String dewpoint;

    @ShowField(description = ResourceIdMapper.rain, showInOverview = false)
    private String rain;

    @ShowField(description = ResourceIdMapper.windSpeed, showInOverview = true)
    private String windSpeed;

    @ShowField(description = ResourceIdMapper.windDirection, showInOverview = true)
    private String windDirection;

    @ShowField(description = ResourceIdMapper.windAvgSpeed, showInOverview = false)
    private String windAverageSpeed;

    @ShowField(description = ResourceIdMapper.windchill, showInOverview = false)
    private String windchill;


    public void readTEMPERATURE(String value) {
        this.temperature = ValueDescriptionUtil.appendTemperature(value);
    }

    public void readBATTERY(String value) {
        this.battery = value;
    }

    public void readHUMIDITY(String value) {
        this.humidity = ValueDescriptionUtil.appendPercent(value);
    }

    public void readDEWPOINT(String value) {
        this.dewpoint = ValueDescriptionUtil.appendTemperature(value);
    }

    public void readRAIN_TOTAL(String value) {
        this.rain = ValueDescriptionUtil.appendLm2(value);
    }

    public void readWIND_SPEED(String value) {
        this.windSpeed = ValueDescriptionUtil.appendKmH(value);
    }

    public void readWIND_DIR(String value) {
        this.windDirection = value;
    }

    public void readWIND_AVSPEED(String value) {
        this.windAverageSpeed = ValueDescriptionUtil.appendKmH(value);
    }

    public void readWINDCHILL(String value) {
        this.windchill = ValueDescriptionUtil.appendTemperature(value);
    }

    public String getTemperature() {
        return temperature;
    }

    public String getBattery() {
        return battery;
    }

    public String getHumidity() {
        return humidity;
    }

    public String getDewpoint() {
        return dewpoint;
    }

    public String getRain() {
        return rain;
    }

    public String getWindSpeed() {
        return windSpeed;
    }

    public String getWindDirection() {
        return windDirection;
    }

    public String getWindAverageSpeed() {
        return windAverageSpeed;
    }

    public String getWindchill() {
        return windchill;
    }

    @Override
    protected void fillDeviceCharts(List<DeviceChart> chartSeries) {
        super.fillDeviceCharts(chartSeries);

        addDeviceChartIfNotNull(new DeviceChart(R.string.temperatureGraph,
                new ChartSeriesDescription.Builder()
                        .withColumnName(R.string.temperature)
                        .withFileLogSpec("4:temperature:")
                        .withDbLogSpec("temperature::int2")
                        .withSeriesType(TEMPERATURE)
                        .withShowRegression(true)
                        .withYAxisMinMaxValue(getLogDevices().get(0).getYAxisMinMaxValueFor("temperature", 0, 30))
                        .build(),
                new ChartSeriesDescription.Builder()
                        .withColumnName(R.string.dewpoint).withFileLogSpec("4:dewpoint:0:")
                        .withDbLogSpec("dewpoint::int1")
                        .withSeriesType(DEWPOINT)
                        .withYAxisMinMaxValue(getLogDevices().get(0).getYAxisMinMaxValueFor("dewpoint", -10, 10))
                        .build()
        ), temperature);

        addDeviceChartIfNotNull(new DeviceChart(R.string.humidityGraph,
                new ChartSeriesDescription.Builder()
                        .withColumnName(R.string.humidity).withFileLogSpec("4:humidity:0:")
                        .withDbLogSpec("humidity")
                        .withSeriesType(HUMIDITY)
                        .withYAxisMinMaxValue(getLogDevices().get(0).getYAxisMinMaxValueFor("humidity", 0, 100))
                        .build()
        ), humidity);

        addDeviceChartIfNotNull(new DeviceChart(R.string.pressureGraph,
                new ChartSeriesDescription.Builder()
                        .withColumnName(R.string.pressure).withFileLogSpec("4:pressure:0:")
                        .withDbLogSpec("pressure")
                        .withSeriesType(PRESSURE)
                        .withYAxisMinMaxValue(getLogDevices().get(0).getYAxisMinMaxValueFor("pressure", 700, 1200))
                        .build()
        ));
    }

    @Override
    public DeviceFunctionality getDeviceGroup() {
        return DeviceFunctionality.WEATHER;
    }

    @Override
    public boolean isSensorDevice() {
        return true;
    }

    @Override
    public long getTimeRequiredForStateError() {
        return OUTDATED_DATA_MS_DEFAULT;
    }
}
