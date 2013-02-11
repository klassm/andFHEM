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
        addDeviceChartIfNotNull(new DeviceChart(R.string.temperatureGraph,
                ChartSeriesDescription.getRegressionValuesInstance(R.string.temperature, "4:temperature:", R.string.yAxisTemperature),
                new ChartSeriesDescription(R.string.dewpoint, "4:dewpoint:0:", R.string.yAxisTemperature)), temperature);
        addDeviceChartIfNotNull(new DeviceChart(R.string.humidityGraph,
                new ChartSeriesDescription(R.string.humidity, "4:humidity:0:", R.string.yAxisHumidity)), humidity);
    }
}
