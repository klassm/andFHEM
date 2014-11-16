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
import li.klass.fhem.domain.core.XmllistAttribute;
import li.klass.fhem.domain.genericview.ShowField;
import li.klass.fhem.service.graph.description.ChartSeriesDescription;
import li.klass.fhem.util.ValueDescriptionUtil;

import static li.klass.fhem.service.graph.description.SeriesType.CURRENT_POWER_WATT;
import static li.klass.fhem.service.graph.description.SeriesType.CURRENT_USAGE_KILOWATT;

public class RevoltDevice extends Device<RevoltDevice> {

    @ShowField(description = ResourceIdMapper.power, showInOverview = true)
    private String power;

    @ShowField(description = ResourceIdMapper.energyPower, showInOverview = true)
    private String energy;

    @ShowField(description = ResourceIdMapper.voltage)
    private String voltage;

    @ShowField(description = ResourceIdMapper.energyFrequency)
    private String frequency;

    @ShowField(description = ResourceIdMapper.energyPowerFactor)
    @XmllistAttribute("pf")
    private String powerFactor;

    @Override
    public DeviceFunctionality getDeviceGroup() {
        return DeviceFunctionality.USAGE;
    }

    public String getPower() {
        return power;
    }

    public String getEnergy() {
        return energy;
    }

    public String getVoltage() {
        return voltage;
    }

    public String getFrequency() {
        return frequency;
    }

    public String getPowerFactor() {
        return powerFactor;
    }

    @XmllistAttribute("power")
    public void setPower(String power) {
        this.power = ValueDescriptionUtil.appendW(power);
    }

    @XmllistAttribute("energy")
    public void setEnergy(String energy) {
        this.energy = ValueDescriptionUtil.appendKWh(energy);
    }

    @XmllistAttribute("voltage")
    public void setVoltage(String voltage) {
        this.voltage = ValueDescriptionUtil.appendV(voltage);
    }

    @XmllistAttribute("frequency")
    public void setFrequency(String frequency) {
        this.frequency = ValueDescriptionUtil.appendHz(frequency);
    }

    @Override
    protected void fillDeviceCharts(List<DeviceChart> chartSeries) {
        super.fillDeviceCharts(chartSeries);

        addDeviceChartIfNotNull(new DeviceChart(R.string.usageGraph,
                new ChartSeriesDescription.Builder()
                        .withColumnName(R.string.energy_power)
                        .withFileLogSpec("4::0:")
                        .withDbLogSpec("power")
                        .withSeriesType(CURRENT_POWER_WATT)
                        .withYAxisMinMaxValue(getLogDevices().get(0).getYAxisMinMaxValueFor("power", 0, 1))
                        .build(),
                new ChartSeriesDescription.Builder()
                        .withColumnName(R.string.currentUsage)
                        .withFileLogSpec("6::0:")
                        .withDbLogSpec("energy")
                        .withSeriesType(CURRENT_USAGE_KILOWATT)
                        .withYAxisMinMaxValue(getLogDevices().get(0).getYAxisMinMaxValueFor("energy", 0, 1))
                        .build()
        ), energy, power);
    }
}
