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
import li.klass.fhem.appwidget.annotation.*;
import li.klass.fhem.appwidget.view.widget.medium.MediumInformationWidgetView;
import li.klass.fhem.appwidget.view.widget.medium.TemperatureWidgetView;
import li.klass.fhem.domain.core.Device;
import li.klass.fhem.domain.core.DeviceChart;
import li.klass.fhem.domain.genericview.FloorplanViewSettings;
import li.klass.fhem.domain.genericview.ShowField;
import li.klass.fhem.service.graph.description.ChartSeriesDescription;
import li.klass.fhem.util.ValueDescriptionUtil;

import java.io.Serializable;
import java.util.List;

@SuppressWarnings("unused")
@FloorplanViewSettings(showState = true)
@SupportsWidget({TemperatureWidgetView.class, MediumInformationWidgetView.class})
public class KS300Device extends Device<KS300Device> implements Serializable {

    @ShowField(description = "R.string.temperature", showInOverview = true)
    @WidgetTemperatureField
    @WidgetMediumLine1
    private String temperature;

    @ShowField(description = "R.string.wind", showInOverview = true)
    @WidgetMediumLine2
    private String wind;

    @ShowField(description = "R.string.humidity", showInOverview = true)
    @WidgetMediumLine3
    @WidgetTemperatureAdditionalField
    private String humidity;

    @ShowField(description = "R.string.rain", showInOverview = true)
    private String rain;

    @ShowField(description = "R.string.avgDay")
    private String averageDay;

    @ShowField(description = "R.string.avgMonth")
    private String averageMonth;

    @ShowField(description = "R.string.isRaining")
    private String isRaining;

    @Override
    public int compareTo(KS300Device ks300Device) {
        return getName().compareTo(ks300Device.getName());
    }


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
    protected void fillDeviceCharts(List<DeviceChart> chartSeries) {
        addDeviceChartIfNotNull(temperature, new DeviceChart(R.string.temperatureGraph, R.string.yAxisTemperature,
                ChartSeriesDescription.getRegressionValuesInstance(R.string.temperature, "4:IR:")));
        addDeviceChartIfNotNull(humidity, new DeviceChart(R.string.humidityGraph, R.string.yAxisHumidity,
                new ChartSeriesDescription(R.string.humidity, "6:IR:")));
        addDeviceChartIfNotNull(wind, new DeviceChart(R.string.windGraph, R.string.yAxisWind,
                new ChartSeriesDescription(R.string.wind, "8:IR:")));
        addDeviceChartIfNotNull(rain, new DeviceChart(R.string.rainGraph, R.string.yAxisRain,
                new ChartSeriesDescription(R.string.rain, "10:IR::delta-h")));
    }
}
