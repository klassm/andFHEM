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

import java.util.List;

import li.klass.fhem.R;
import li.klass.fhem.domain.core.DeviceChart;
import li.klass.fhem.domain.core.DeviceFunctionality;
import li.klass.fhem.domain.core.FhemDevice;
import li.klass.fhem.domain.genericview.ShowField;
import li.klass.fhem.domain.heating.TemperatureDevice;
import li.klass.fhem.resources.ResourceIdMapper;
import li.klass.fhem.service.graph.description.ChartSeriesDescription;

import static li.klass.fhem.service.graph.description.SeriesType.DEWPOINT;
import static li.klass.fhem.service.graph.description.SeriesType.HUMIDITY;
import static li.klass.fhem.service.graph.description.SeriesType.TEMPERATURE;
import static li.klass.fhem.util.ValueDescriptionUtil.appendPercent;
import static li.klass.fhem.util.ValueDescriptionUtil.appendTemperature;

@SuppressWarnings("unused")
public class SHT21Device extends FhemDevice<SHT21Device> implements TemperatureDevice {
    @ShowField(description = ResourceIdMapper.humidity, showInOverview = true)
    public String humidity;
    @ShowField(description = ResourceIdMapper.temperature, showInOverview = true)
    public String temperature;
    @ShowField(description = ResourceIdMapper.dewpoint, showInOverview = true)
    public String dewpoint;

    public void readHUMIDITY(String value) {
        humidity = appendPercent(value);
    }

    public void readTEMPERATURE(String value) {
        temperature = appendTemperature(value);
    }

    public void readDEWPOINT(String value) {
        dewpoint = appendTemperature(value);
    }

    @Override
    public DeviceFunctionality getDeviceGroup() {
        return DeviceFunctionality.TEMPERATURE;
    }

    @Override
    protected void fillDeviceCharts(List<DeviceChart> chartSeries, Context context) {
        super.fillDeviceCharts(chartSeries, context);

        addDeviceChartIfNotNull(new DeviceChart(R.string.temperatureGraph,
                new ChartSeriesDescription.Builder()
                        .withColumnName(R.string.temperature, context)
                        .withFileLogSpec("4::")
                        .withDbLogSpec("temperature::int1")
                        .withSeriesType(TEMPERATURE)
                        .withShowRegression(true)
                        .build()
        ), temperature);

        addDeviceChartIfNotNull(new DeviceChart(R.string.humidityGraph,
                new ChartSeriesDescription.Builder()
                        .withColumnName(R.string.humidity, context).withFileLogSpec("6::")
                        .withDbLogSpec("humidity::int1")
                        .withSeriesType(HUMIDITY)
                        .withYAxisMinMaxValue(getLogDevices().get(0).getYAxisMinMaxValueFor("humidity", 0, 100))
                        .build()
        ), humidity);

        addDeviceChartIfNotNull(new DeviceChart(R.string.dewpointGraph,
                new ChartSeriesDescription.Builder()
                        .withColumnName(R.string.dewpoint, context).withFileLogSpec("8::")
                        .withDbLogSpec("humidity::int1")
                        .withSeriesType(DEWPOINT)
                        .withYAxisMinMaxValue(getLogDevices().get(0).getYAxisMinMaxValueFor("humidity", 0, 100))
                        .build()
        ), dewpoint);
    }

    public String getHumidity() {
        return humidity;
    }

    public String getTemperature() {
        return temperature;
    }

    public String getDewpoint() {
        return dewpoint;
    }
}
