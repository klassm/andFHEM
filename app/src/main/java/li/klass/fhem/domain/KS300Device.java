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

import java.io.Serializable;
import java.util.List;

import li.klass.fhem.R;
import li.klass.fhem.appwidget.annotation.ResourceIdMapper;
import li.klass.fhem.appwidget.annotation.SupportsWidget;
import li.klass.fhem.appwidget.annotation.WidgetMediumLine1;
import li.klass.fhem.appwidget.annotation.WidgetMediumLine2;
import li.klass.fhem.appwidget.annotation.WidgetMediumLine3;
import li.klass.fhem.appwidget.annotation.WidgetTemperatureAdditionalField;
import li.klass.fhem.appwidget.annotation.WidgetTemperatureField;
import li.klass.fhem.appwidget.view.widget.medium.MediumInformationWidgetView;
import li.klass.fhem.appwidget.view.widget.medium.TemperatureWidgetView;
import li.klass.fhem.domain.core.Device;
import li.klass.fhem.domain.core.DeviceChart;
import li.klass.fhem.domain.core.DeviceFunctionality;
import li.klass.fhem.domain.genericview.ShowField;
import li.klass.fhem.service.graph.description.ChartSeriesDescription;
import li.klass.fhem.util.ValueDescriptionUtil;

import static li.klass.fhem.service.graph.description.SeriesType.HUMIDITY;
import static li.klass.fhem.service.graph.description.SeriesType.RAIN;
import static li.klass.fhem.service.graph.description.SeriesType.TEMPERATURE;
import static li.klass.fhem.service.graph.description.SeriesType.WIND;

@SuppressWarnings("unused")
@SupportsWidget({TemperatureWidgetView.class, MediumInformationWidgetView.class})
public class KS300Device extends Device<KS300Device> implements Serializable {

    @ShowField(description = ResourceIdMapper.temperature, showInOverview = true)
    @WidgetTemperatureField
    @WidgetMediumLine1
    private String temperature;

    @ShowField(description = ResourceIdMapper.wind, showInOverview = true)
    @WidgetMediumLine2
    private String wind;

    @ShowField(description = ResourceIdMapper.humidity, showInOverview = true)
    @WidgetMediumLine3
    @WidgetTemperatureAdditionalField
    private String humidity;

    @ShowField(description = ResourceIdMapper.rain, showInOverview = true)
    private String rain;

    @ShowField(description = ResourceIdMapper.avgDay)
    private String averageDay;

    @ShowField(description = ResourceIdMapper.avgMonth)
    private String averageMonth;

    @ShowField(description = ResourceIdMapper.isRaining)
    private String isRaining;

    public void readTEMPERATURE(String value) {
        this.temperature = ValueDescriptionUtil.appendTemperature(value);
    }

    public void readWIND(String value) {
        this.wind = ValueDescriptionUtil.appendKmH(value);
    }

    public void readHUMIDITY(String value) {
        this.humidity = ValueDescriptionUtil.appendPercent(value);
    }

    public void readRAIN(String value) {
        this.rain = ValueDescriptionUtil.appendLm2(value);
    }

    public void readAVG_DAY(String value) {
        this.averageDay = value;
    }

    public void readAVG_MONTH(String value) {
        this.averageMonth = value;
    }

    public void readISRAINING(String value) {
        this.isRaining = value;
    }

    public String getTemperature() {
        return temperature;
    }

    public String getWind() {
        return wind;
    }

    public String getHumidity() {
        return humidity;
    }

    public String getRain() {
        return rain;
    }

    public String getAverageDay() {
        return averageDay;
    }

    public String getAverageMonth() {
        return averageMonth;
    }

    public String getRaining() {
        return isRaining;
    }

    @Override
    public String toString() {
        return "KS300Device{" +
                "temperature='" + temperature + '\'' +
                ", wind='" + wind + '\'' +
                ", humidity='" + humidity + '\'' +
                ", rain='" + rain + '\'' +
                "} " + super.toString();
    }

    @Override
    public DeviceFunctionality getDeviceGroup() {
        return DeviceFunctionality.WEATHER;
    }

    @Override
    protected void fillDeviceCharts(List<DeviceChart> chartSeries) {
        super.fillDeviceCharts(chartSeries);

        addDeviceChartIfNotNull(new DeviceChart(R.string.temperatureHumidityGraph,
                new ChartSeriesDescription.Builder()
                        .withColumnName(R.string.temperature)
                        .withFileLogSpec("4::")
                        .withDbLogSpec("temperature::int1")
                        .withSeriesType(TEMPERATURE)
                        .withShowRegression(true)
                        .withYAxisMinMaxValue(getLogDevices().get(0).getYAxisMinMaxValueFor("temperature", 0, 30))
                        .build(),
                new ChartSeriesDescription.Builder()
                        .withColumnName(R.string.humidity).withFileLogSpec("6::")
                        .withDbLogSpec("humidity")
                        .withSeriesType(HUMIDITY)
                        .withYAxisMinMaxValue(getLogDevices().get(0).getYAxisMinMaxValueFor("humidity", 0, 100))
                        .build()
        ), temperature, humidity);

        addDeviceChartIfNotNull(new DeviceChart(R.string.windGraph,
                new ChartSeriesDescription.Builder()
                        .withColumnName(R.string.wind).withFileLogSpec("8::")
                        .withDbLogSpec("wind::int1")
                        .withSeriesType(WIND)
                        .withYAxisMinMaxValue(getLogDevices().get(0).getYAxisMinMaxValueFor("wind", 0, 100))
                        .build()
        ), wind);

        addDeviceChartIfNotNull(new DeviceChart(R.string.rainGraph,
                new ChartSeriesDescription.Builder()
                        .withColumnName(R.string.rain).withFileLogSpec("10::")
                        .withDbLogSpec("rain::int1")
                        .withSeriesType(RAIN)
                        .withYAxisMinMaxValue(getLogDevices().get(0).getYAxisMinMaxValueFor("rain", 0, 100))
                        .build()
        ), rain);
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
