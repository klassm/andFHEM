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

import android.util.Log;

import org.w3c.dom.NamedNodeMap;

import java.util.List;
import java.util.Locale;

import li.klass.fhem.R;
import li.klass.fhem.appwidget.annotation.ResourceIdMapper;
import li.klass.fhem.domain.core.Device;
import li.klass.fhem.domain.core.DeviceChart;
import li.klass.fhem.domain.core.DeviceFunctionality;
import li.klass.fhem.domain.genericview.DetailViewSettings;
import li.klass.fhem.domain.genericview.ShowField;
import li.klass.fhem.service.graph.description.ChartSeriesDescription;
import li.klass.fhem.util.ValueDescriptionUtil;

import static li.klass.fhem.service.graph.description.SeriesType.CO2;
import static li.klass.fhem.service.graph.description.SeriesType.FAT_RATIO;
import static li.klass.fhem.service.graph.description.SeriesType.TEMPERATURE;
import static li.klass.fhem.service.graph.description.SeriesType.WEIGHT;
import static li.klass.fhem.util.ValueExtractUtil.extractLeadingDouble;
import static li.klass.fhem.util.ValueExtractUtil.extractLeadingInt;

@SuppressWarnings("unused")
@DetailViewSettings(showState = false)
public class WithingsDevice extends Device<WithingsDevice> {
    @ShowField(description = ResourceIdMapper.fatFreeMass)
    private String fatFreeMass;
    @ShowField(description = ResourceIdMapper.fatMass)
    private String fatMassWeight;
    @ShowField(description = ResourceIdMapper.fatRatio, showInOverview = true)
    private String fatRatio;
    @ShowField(description = ResourceIdMapper.heartPulse)
    private String heartPulse;
    @ShowField(description = ResourceIdMapper.height)
    private String height;
    @ShowField(description = ResourceIdMapper.weight, showInOverview = true)
    private String weight;
    @ShowField(description = ResourceIdMapper.temperature, showInOverview = true)
    private String temperature;
    @ShowField(description = ResourceIdMapper.co2, showInOverview = true)
    private String co2;
    @ShowField(description = ResourceIdMapper.battery)
    private String batteryLevel;
    private SubType subType;

    @Override
    public DeviceFunctionality getDeviceGroup() {
        return DeviceFunctionality.UNKNOWN;
    }

    public String getFatFreeMass() {
        return fatFreeMass;
    }

    public String getFatMassWeight() {
        return fatMassWeight;
    }

    public String getFatRatio() {
        return fatRatio;
    }

    public String getHeartPulse() {
        return heartPulse;
    }

    public String getHeight() {
        return height;
    }

    public String getWeight() {
        return weight;
    }

    public String getTemperature() {
        return temperature;
    }

    public String getCo2() {
        return co2;
    }

    public String getBatteryLevel() {
        return batteryLevel;
    }

    public SubType getSubType() {
        return subType;
    }

    @Override
    public void onChildItemRead(String tagName, String key, String value, NamedNodeMap attributes) {
        super.onChildItemRead(tagName, key, value, attributes);

        if (tagName.equalsIgnoreCase("STATE") &&
                (key.equalsIgnoreCase("BATTERY") ||
                        key.equalsIgnoreCase("WEIGHT"))) {
            setMeasured(attributes.getNamedItem("measured").getNodeValue());
        }
    }

    public void readSUBTYPE(String value) {
        try {
            subType = SubType.valueOf(value.toUpperCase(Locale.getDefault()));
        } catch (IllegalArgumentException e) {
            Log.d(WithingsDevice.class.getName(), "cannot find enum value for " + value);
        }
    }

    public void readFATFREEMASS(String value) {
        this.fatFreeMass = formatWeight(value);
    }

    public void readFATMASSWEIGHT(String value) {
        this.fatMassWeight = formatWeight(value);
    }

    public void readFATRATIO(String value) {
        this.fatRatio = "" + extractLeadingDouble(value, 1);
    }

    public void readHEARTPULSE(String value) {
        this.heartPulse = "" + extractLeadingInt(value);
    }

    public void readHEIGHT(String value) {
        this.height = ValueDescriptionUtil.append(value, "m");
    }

    public void readWEIGHT(String value) {
        this.weight = formatWeight(value);
    }

    public void readBATTERYLEVEL(String value) {
        this.batteryLevel = ValueDescriptionUtil.appendPercent(value);
    }

    public void readCO2(String value) {
        this.co2 = ValueDescriptionUtil.append(value, "ppm");
    }

    public void readTEMPERATURE(String value) {
        this.temperature = ValueDescriptionUtil.appendTemperature(value);
    }

    private String formatWeight(String value) {
        return ValueDescriptionUtil.append(extractLeadingDouble(value, 1), "kg");
    }

    @Override
    protected void fillDeviceCharts(List<DeviceChart> chartSeries) {
        super.fillDeviceCharts(chartSeries);

        addDeviceChartIfNotNull(new DeviceChart(R.string.temperatureGraph,
                new ChartSeriesDescription.Builder()
                        .withColumnName(R.string.temperature)
                        .withFileLogSpec("4:temperature")
                        .withDbLogSpec("temperature")
                        .withSeriesType(TEMPERATURE)
                        .withShowRegression(true)
                        .withYAxisMinMaxValue(getLogDevices().get(0).getYAxisMinMaxValueFor("temperature", 0, 30))
                        .build()
        ), temperature);

        addDeviceChartIfNotNull(new DeviceChart(R.string.co2Graph,
                new ChartSeriesDescription.Builder()
                        .withColumnName(R.string.co2)
                        .withFileLogSpec("4:co2")
                        .withDbLogSpec("co2")
                        .withSeriesType(CO2)
                        .withShowRegression(true)
                        .withYAxisMinMaxValue(getLogDevices().get(0).getYAxisMinMaxValueFor("co2", 300, 400))
                        .build()
        ), co2);

        addDeviceChartIfNotNull(new DeviceChart(R.string.weightGraph,
                        new ChartSeriesDescription.Builder()
                                .withColumnName(R.string.weight)
                                .withFileLogSpec("4:weight")
                                .withDbLogSpec("weight")
                                .withSeriesType(WEIGHT)
                                .withShowRegression(true)
                                .withYAxisMinMaxValue(getLogDevices().get(0).getYAxisMinMaxValueFor("weight", 0, 70))
                                .build(),
                        new ChartSeriesDescription.Builder()
                                .withColumnName(R.string.fatRatio)
                                .withFileLogSpec("4:fatRatio")
                                .withDbLogSpec("fatRatio")
                                .withSeriesType(FAT_RATIO)
                                .withShowRegression(true)
                                .withYAxisMinMaxValue(getLogDevices().get(0).getYAxisMinMaxValueFor("fatRatio", 0, 100))
                                .build()),
                fatRatio, weight
        );
    }

    @Override
    public boolean isSupported() {
        return subType != null;
    }

    public enum SubType {
        USER, DEVICE
    }
}
