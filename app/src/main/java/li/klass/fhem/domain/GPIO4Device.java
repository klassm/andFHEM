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

    private enum SubType {
        TEMPERATURE
    }

    public void readMODEL(String value) {
        if (value.equals("DS1820") || value.equalsIgnoreCase("DS18B20")) {
            subType = SubType.TEMPERATURE;
        }
    }

    public void readTEMPERATURE(String value) {
        temperature = ValueDescriptionUtil.appendTemperature(value);
    }

    public String getTemperature() {
        return temperature;
    }

    @Override
    protected void fillDeviceCharts(List<DeviceChart> chartSeries) {
        super.fillDeviceCharts(chartSeries);

        if (subType == SubType.TEMPERATURE) {
            addDeviceChartIfNotNull(new DeviceChart(R.string.temperatureGraph,
                    ChartSeriesDescription.getRegressionValuesInstance(R.string.temperature, "4:T", TEMPERATURE)), temperature);
        }
    }

    @Override
    public boolean isSupported() {
        return subType != null;
    }

    @Override
    public DeviceFunctionality getDeviceFunctionality() {
        return DeviceFunctionality.TEMPERATURE;
    }
}
