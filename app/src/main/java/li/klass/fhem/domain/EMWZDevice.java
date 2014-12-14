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

import static li.klass.fhem.service.graph.description.SeriesType.POWER;

@SuppressWarnings("unused")
public class EMWZDevice extends Device<EMWZDevice> {
    @ShowField(description = ResourceIdMapper.cumulativeUsage, showInOverview = true)
    private String cumulativeKwh;

    @ShowField(description = ResourceIdMapper.energy)
    private String energy;

    @ShowField(description = ResourceIdMapper.power)
    private String power;

    @ShowField(description = ResourceIdMapper.price)
    private String price;

    public void readCUM_KWH(String value) {
        cumulativeKwh = ValueDescriptionUtil.append(value, "kWh");
    }

    public void readENERGY(String value) {
        this.energy = ValueDescriptionUtil.append(value, "Wh");
    }

    public void readPOWER(String value) {
        this.power = ValueDescriptionUtil.append(value, "W");
    }

    public void readPRICE_CF(String value) {
        this.price = ValueDescriptionUtil.append(value, "EUR/W");
    }

    public String getCumulativeKwh() {
        return cumulativeKwh;
    }

    public String getEnergy() {
        return energy;
    }

    public String getPower() {
        return power;
    }

    public String getPrice() {
        return price;
    }

    @Override
    protected void fillDeviceCharts(List<DeviceChart> chartSeries) {
        super.fillDeviceCharts(chartSeries);

        addDeviceChartIfNotNull(
                new DeviceChart(R.string.powerGraph,
                        new ChartSeriesDescription.Builder()
                                .withColumnName(R.string.power)
                                .withFileLogSpec("4:")
                                .withDbLogSpec("energy::int3")
                                .withSeriesType(POWER)
                                .withShowRegression(true)
                                .withYAxisMinMaxValue(getLogDevices().get(0).getYAxisMinMaxValueFor("energy", 0, 100))
                                .build()
                )
        );
    }

    @Override
    public DeviceFunctionality getDeviceGroup() {
        return DeviceFunctionality.USAGE;
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
