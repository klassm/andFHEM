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
import li.klass.fhem.domain.core.Device;
import li.klass.fhem.domain.core.DeviceChart;
import li.klass.fhem.domain.core.DeviceFunctionality;
import li.klass.fhem.domain.genericview.ShowField;
import li.klass.fhem.service.graph.description.ChartSeriesDescription;
import li.klass.fhem.util.ValueDescriptionUtil;

import static li.klass.fhem.service.graph.description.SeriesType.TEMPERATURE;

@SuppressWarnings("unused")
public class GPIO4Device extends Device<GPIO4Device> {

    private SubType subType = null;

    @ShowField(description = ResourceIdMapper.temperature, showInOverview = true)
    private String temperature;

    @ShowField(description = ResourceIdMapper.avgDay)
    private String averageDay;

    @ShowField(description = ResourceIdMapper.avgMonth)
    private String averageMonth;

    @ShowField(description = ResourceIdMapper.maxDay)
    private String maxDay;

    @ShowField(description = ResourceIdMapper.maxMonth)
    private String maxMonth;

    @ShowField(description = ResourceIdMapper.minDay)
    private String minDay;

    @ShowField(description = ResourceIdMapper.minMonth)
    private String minMonth;

    public void readMODEL(String value) {
        if (value.equals("DS1820") || value.equalsIgnoreCase("DS18B20")) {
            subType = SubType.TEMPERATURE;
        }
    }

    public void readTEMPERATURE(String value) {
        temperature = ValueDescriptionUtil.appendTemperature(value);
    }

    public void readT_AVG_DAY(String value) {
        this.averageDay = ValueDescriptionUtil.appendTemperature(value);
    }

    public void readT_AVG_MONTH(String value) {
        this.averageMonth = ValueDescriptionUtil.appendTemperature(value);
    }

    public void readT_MIN_DAY(String value) {
        this.minDay = ValueDescriptionUtil.appendTemperature(value);
    }

    public void readT_MIN_MONTH(String value) {
        this.minMonth = ValueDescriptionUtil.appendTemperature(value);
    }

    public void readT_MAX_DAY(String value) {
        this.maxDay = ValueDescriptionUtil.appendTemperature(value);
    }

    public void readT_MAX_MONTH(String value) {
        this.maxMonth = ValueDescriptionUtil.appendTemperature(value);
    }

    public String getTemperature() {
        return temperature;
    }

    @Override
    protected void fillDeviceCharts(List<DeviceChart> chartSeries) {
        super.fillDeviceCharts(chartSeries);

        if (subType == SubType.TEMPERATURE) {
            addDeviceChartIfNotNull(new DeviceChart(R.string.temperatureGraph,
                    new ChartSeriesDescription.Builder()
                            .withColumnName(R.string.temperature)
                            .withFileLogSpec("4:T")
                            .withDbLogSpec("temperature::int2")
                            .withSeriesType(TEMPERATURE)
                            .withShowRegression(true)
                            .withYAxisMinMaxValue(getLogDevices().get(0).getYAxisMinMaxValueFor("temperature", 0, 30))
                            .build()
            ), temperature);
        }
    }

    @Override
    public boolean isSupported() {
        return super.isSupported() && subType != null;
    }

    public String getAverageDay() {
        return averageDay;
    }

    public String getAverageMonth() {
        return averageMonth;
    }

    public String getMaxDay() {
        return maxDay;
    }

    public String getMaxMonth() {
        return maxMonth;
    }

    public String getMinDay() {
        return minDay;
    }

    public String getMinMonth() {
        return minMonth;
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

    private enum SubType {
        TEMPERATURE
    }
}
