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

import static li.klass.fhem.service.graph.description.SeriesType.PRESSURE;
import static li.klass.fhem.service.graph.description.SeriesType.PRESSURE_NN;
import static li.klass.fhem.service.graph.description.SeriesType.TEMPERATURE;
import static li.klass.fhem.util.ValueDescriptionUtil.append;
import static li.klass.fhem.util.ValueDescriptionUtil.appendTemperature;

@SuppressWarnings("unused")
public class BMP180Device extends Device<BMP180Device> {
    @ShowField(description = ResourceIdMapper.pressure, showInOverview = true)
    private String pressure;
    @ShowField(description = ResourceIdMapper.pressureNN, showInOverview = true)
    private String pressureNN;
    @ShowField(description = ResourceIdMapper.temperature, showInOverview = true)
    private String temperature;

    public void readPRESSURE(String value) {
        pressure = append(value, "hPa");
    }

    public void readPRESSURE_NN(String value) {
        pressureNN = append(value, "hPa");
    }

    public void readTEMPERATURE(String value) {
        temperature = appendTemperature(value);
    }

    @Override
    public DeviceFunctionality getDeviceGroup() {
        return DeviceFunctionality.WEATHER;
    }

    @Override
    protected void fillDeviceCharts(List<DeviceChart> chartSeries) {
        super.fillDeviceCharts(chartSeries);

        addDeviceChartIfNotNull(new DeviceChart(R.string.temperatureGraph,
                new ChartSeriesDescription.Builder()
                        .withColumnName(R.string.temperature)
                        .withFileLogSpec("4::")
                        .withDbLogSpec("temperature::int1")
                        .withSeriesType(TEMPERATURE)
                        .withShowRegression(true)
                        .withYAxisMinMaxValue(getLogDevices().get(0).getYAxisMinMaxValueFor("temperature", 0, 30))
                        .build()
        ), temperature);

        addDeviceChartIfNotNull(new DeviceChart(R.string.pressureGraph,
                new ChartSeriesDescription.Builder()
                        .withColumnName(R.string.pressure).withFileLogSpec("6::")
                        .withDbLogSpec("pressure::int1")
                        .withSeriesType(PRESSURE)
                        .withYAxisMinMaxValue(getLogDevices().get(0).getYAxisMinMaxValueFor("pressure", 700, 1200))
                        .build(),
                new ChartSeriesDescription.Builder()
                        .withColumnName(R.string.pressure).withFileLogSpec("8::")
                        .withDbLogSpec("pressure-nn::int1")
                        .withYAxisMinMaxValue(getLogDevices().get(0).getYAxisMinMaxValueFor("pressure-nn", 700, 1200))
                        .withSeriesType(PRESSURE_NN)
                        .build()
        ), pressure, pressureNN);
    }

    public String getPressure() {
        return pressure;
    }

    public String getPressureNN() {
        return pressureNN;
    }

    public String getTemperature() {
        return temperature;
    }
}
