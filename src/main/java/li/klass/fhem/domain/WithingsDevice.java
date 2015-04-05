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

import android.content.Context;
import android.util.Log;

import java.util.List;
import java.util.Locale;

import li.klass.fhem.R;
import li.klass.fhem.domain.core.DeviceChart;
import li.klass.fhem.domain.core.DeviceFunctionality;
import li.klass.fhem.domain.core.FhemDevice;
import li.klass.fhem.domain.core.XmllistAttribute;
import li.klass.fhem.domain.genericview.DetailViewSettings;
import li.klass.fhem.domain.genericview.ShowField;
import li.klass.fhem.domain.heating.TemperatureDevice;
import li.klass.fhem.resources.ResourceIdMapper;
import li.klass.fhem.service.graph.description.ChartSeriesDescription;
import li.klass.fhem.service.room.xmllist.DeviceNode;

import static li.klass.fhem.service.graph.description.SeriesType.CO2;
import static li.klass.fhem.service.graph.description.SeriesType.FAT_RATIO;
import static li.klass.fhem.service.graph.description.SeriesType.TEMPERATURE;
import static li.klass.fhem.service.graph.description.SeriesType.WEIGHT;
import static li.klass.fhem.service.room.xmllist.DeviceNode.DeviceNodeType.STATE;

@DetailViewSettings(showState = false)
public class WithingsDevice extends FhemDevice<WithingsDevice> implements TemperatureDevice {
    @ShowField(description = ResourceIdMapper.fatFreeMass)
    @XmllistAttribute("fatFreeMass")
    private String fatFreeMass;

    @ShowField(description = ResourceIdMapper.fatMass)
    @XmllistAttribute("fatMassWeight")
    private String fatMassWeight;

    @ShowField(description = ResourceIdMapper.fatRatio, showInOverview = true)
    @XmllistAttribute("fatRatio")
    private String fatRatio;

    @ShowField(description = ResourceIdMapper.heartPulse)
    @XmllistAttribute("heartPulse")
    private String heartPulse;

    @ShowField(description = ResourceIdMapper.height)
    @XmllistAttribute("height")
    private String height;

    @ShowField(description = ResourceIdMapper.weight, showInOverview = true)
    @XmllistAttribute("weight")
    private String weight;

    @ShowField(description = ResourceIdMapper.temperature, showInOverview = true)
    @XmllistAttribute("temperature")
    private String temperature;

    @ShowField(description = ResourceIdMapper.co2, showInOverview = true)
    @XmllistAttribute("co2")
    private String co2;

    @ShowField(description = ResourceIdMapper.battery)
    @XmllistAttribute("batteryLevel")
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
    public void onChildItemRead(DeviceNode.DeviceNodeType type, String key, String value, DeviceNode node) {
        super.onChildItemRead(type, key, value, node);

        if (node.getType() == STATE && ("BATTERY").equalsIgnoreCase(node.getKey()) || "WEIGHT".equalsIgnoreCase(node.getKey())) {
            setMeasured(node.getMeasured());
        }
    }

    @XmllistAttribute("subtype")
    public void setSubtype(String value) {
        try {
            subType = SubType.valueOf(value.toUpperCase(Locale.getDefault()));
        } catch (IllegalArgumentException e) {
            Log.d(WithingsDevice.class.getName(), "cannot find enum value for " + value);
        }
    }

    @Override
    protected void fillDeviceCharts(List<DeviceChart> chartSeries, Context context) {
        super.fillDeviceCharts(chartSeries, context);

        addDeviceChartIfNotNull(new DeviceChart(R.string.temperatureGraph,
                new ChartSeriesDescription.Builder()
                        .withColumnName(R.string.temperature, context)
                        .withFileLogSpec("4:temperature")
                        .withDbLogSpec("temperature")
                        .withSeriesType(TEMPERATURE)
                        .withShowRegression(true)
                        .build()
        ), temperature);

        addDeviceChartIfNotNull(new DeviceChart(R.string.co2Graph,
                new ChartSeriesDescription.Builder()
                        .withColumnName(R.string.co2, context)
                        .withFileLogSpec("4:co2")
                        .withDbLogSpec("co2")
                        .withSeriesType(CO2)
                        .withShowRegression(true)
                        .withYAxisMinMaxValue(getLogDevices().get(0).getYAxisMinMaxValueFor("co2", 0, 0))
                        .build()
        ), co2);

        addDeviceChartIfNotNull(new DeviceChart(R.string.weightGraph,
                        new ChartSeriesDescription.Builder()
                                .withColumnName(R.string.weight, context)
                                .withFileLogSpec("4:weight")
                                .withDbLogSpec("weight")
                                .withSeriesType(WEIGHT)
                                .withShowRegression(true)
                                .withYAxisMinMaxValue(getLogDevices().get(0).getYAxisMinMaxValueFor("weight", 0, 0))
                                .build(),
                        new ChartSeriesDescription.Builder()
                                .withColumnName(R.string.fatRatio, context)
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
