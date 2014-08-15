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
import li.klass.fhem.domain.core.DeviceChart;
import li.klass.fhem.domain.core.DeviceFunctionality;
import li.klass.fhem.domain.core.ToggleableDevice;
import li.klass.fhem.domain.genericview.OverviewViewSettings;
import li.klass.fhem.domain.genericview.ShowField;
import li.klass.fhem.service.graph.description.ChartSeriesDescription;
import li.klass.fhem.util.ValueDescriptionUtil;
import li.klass.fhem.util.ValueExtractUtil;

import static li.klass.fhem.service.graph.description.SeriesType.POWER;

@SuppressWarnings("unused")
@OverviewViewSettings(showState = true, showMeasured = true)
public class FBDectDevice extends ToggleableDevice<FBDectDevice> {
    @ShowField(description = ResourceIdMapper.energy)
    private String energy;

    @ShowField(description = ResourceIdMapper.power, showInOverview = true)
    private String power;

    @ShowField(description = ResourceIdMapper.voltage)
    private String voltage;

    @ShowField(description = ResourceIdMapper.cumulativeUsage)
    private String current;


    public void readENERGY(String value) {
        int numValue = ValueExtractUtil.extractLeadingInt(value);
        this.energy = ValueDescriptionUtil.append(numValue, "Wh");
    }

    public void readPOWER(String value) {
        double numValue = ValueExtractUtil.extractLeadingDouble(value);
        this.power = ValueDescriptionUtil.append(numValue, "W");
    }

    public void readVOLTAGE(String value) {
        double numValue = ValueExtractUtil.extractLeadingDouble(value);
        this.voltage = ValueDescriptionUtil.append(numValue, "V");
    }

    public void readCURRENT(String value) {
        double numValue = ValueExtractUtil.extractLeadingDouble(value);
        this.current = ValueDescriptionUtil.append(numValue, "A");
    }

    public String getEnergy() {
        return energy;
    }

    public String getPower() {
        return power;
    }

    public String getVoltage() {
        return voltage;
    }

    public String getCurrent() {
        return current;
    }

    @Override
    protected void fillDeviceCharts(List<DeviceChart> chartSeries) {
        super.fillDeviceCharts(chartSeries);

        addDeviceChartIfNotNull(new DeviceChart(R.string.powerGraph,
                new ChartSeriesDescription.Builder()
                        .withColumnName(R.string.power).withFileLogSpec("4:power")
                        .withDbLogSpec("power::int2")
                        .withSeriesType(POWER)
                        .withYAxisMinMaxValue(getLogDevices().get(0).getYAxisMinMaxValueFor("power", 0, 100))
                        .build()
        ), energy);
    }

    @Override
    public DeviceFunctionality getDeviceGroup() {
        return DeviceFunctionality.USAGE;
    }

    @Override
    public boolean supportsToggle() {
        return true;
    }
}
