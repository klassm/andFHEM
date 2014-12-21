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
import li.klass.fhem.appwidget.annotation.WidgetTemperatureAdditionalField;
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
import static li.klass.fhem.service.graph.description.SeriesType.TEMPERATURE;

@SupportsWidget(TemperatureWidgetView.class)
@SuppressWarnings("unused")
public class CULWSDevice extends Device<CULWSDevice> {

    @ShowField(description = ResourceIdMapper.humidity, showInOverview = true)
    @WidgetTemperatureAdditionalField
    private String humidity;
    @ShowField(description = ResourceIdMapper.temperature, showInOverview = true)
    @WidgetTemperatureField
    private String temperature;
    @ShowField(description = ResourceIdMapper.dewpoint)
    private String dewpoint;

    public void readTEMPERATURE(String value) {
        this.temperature = ValueDescriptionUtil.appendTemperature(value);
    }

    public void readHUMIDITY(String value) {
        this.humidity = ValueDescriptionUtil.appendPercent(value);
    }

    public void readDEWPOINT(String value) {
        this.dewpoint = ValueDescriptionUtil.appendTemperature(value);
    }

    public String getHumidity() {
        return humidity;
    }

    public String getTemperature() {
        return temperature;
    }

    @Override
    protected void fillDeviceCharts(List<DeviceChart> chartSeries) {
        super.fillDeviceCharts(chartSeries);

        if (temperature != null && humidity != null && dewpoint != null) {
            addDeviceChartIfNotNull(new DeviceChart(R.string.temperatureHumidityDewpointGraph,
                    new ChartSeriesDescription.Builder()
                            .withColumnName(R.string.temperature)
                            .withFileLogSpec("4:T:0:")
                            .withDbLogSpec("temperature")
                            .withSeriesType(TEMPERATURE)
                            .withShowRegression(true)
                            .withYAxisMinMaxValue(getLogDevices().get(0).getYAxisMinMaxValueFor("temperature", 0, 30))
                            .build(),
                    new ChartSeriesDescription.Builder()
                            .withColumnName(R.string.humidity).withFileLogSpec("6:H:0")
                            .withDbLogSpec("humidity")
                            .withSeriesType(HUMIDITY)
                            .withYAxisMinMaxValue(getLogDevices().get(0).getYAxisMinMaxValueFor("humidity", 0, 100))
                            .build(),
                    new ChartSeriesDescription.Builder()
                            .withColumnName(R.string.dewpoint).withFileLogSpec("8:D\\x3a:0:")
                            .withDbLogSpec("dewpoint")
                            .withSeriesType(DEWPOINT)
                            .withYAxisMinMaxValue(getLogDevices().get(0).getYAxisMinMaxValueFor("dewpoint", -10, 10))
                            .build()
            ), temperature, humidity, dewpoint);
        } else if (temperature != null && humidity != null) {
            addDeviceChartIfNotNull(new DeviceChart(R.string.temperatureHumidityGraph,
                    new ChartSeriesDescription.Builder()
                            .withColumnName(R.string.temperature)
                            .withFileLogSpec("4:T:0:")
                            .withDbLogSpec("temperature")
                            .withSeriesType(TEMPERATURE)
                            .withShowRegression(true)
                            .withYAxisMinMaxValue(getLogDevices().get(0).getYAxisMinMaxValueFor("temperature", 0, 30))
                            .build(),
                    new ChartSeriesDescription.Builder()
                            .withColumnName(R.string.humidity).withFileLogSpec("6:H:0")
                            .withDbLogSpec("humidity")
                            .withSeriesType(HUMIDITY)
                            .withYAxisMinMaxValue(getLogDevices().get(0).getYAxisMinMaxValueFor("humidity", 0, 30))
                            .build()
            ), temperature, humidity);
        } else {
            addDeviceChartIfNotNull(new DeviceChart(R.string.temperatureGraph,
                    new ChartSeriesDescription.Builder()
                            .withColumnName(R.string.temperature)
                            .withFileLogSpec("4:T:0:")
                            .withDbLogSpec("temperature")
                            .withSeriesType(TEMPERATURE)
                            .withShowRegression(true)
                            .withYAxisMinMaxValue(getLogDevices().get(0).getYAxisMinMaxValueFor("temperature", 0, 30))
                            .build()
            ), temperature);
        }
    }

    @Override
    public DeviceFunctionality getDeviceGroup() {
        return DeviceFunctionality.TEMPERATURE;
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
