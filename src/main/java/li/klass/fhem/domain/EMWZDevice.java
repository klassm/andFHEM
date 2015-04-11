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
import li.klass.fhem.domain.core.ChartProvider;
import li.klass.fhem.domain.core.DeviceChart;
import li.klass.fhem.domain.core.DeviceFunctionality;
import li.klass.fhem.domain.core.FhemDevice;
import li.klass.fhem.domain.core.XmllistAttribute;
import li.klass.fhem.domain.genericview.ShowField;
import li.klass.fhem.resources.ResourceIdMapper;
import li.klass.fhem.service.graph.description.ChartSeriesDescription;

import static li.klass.fhem.service.graph.description.SeriesType.POWER;

public class EMWZDevice extends FhemDevice<EMWZDevice> {
    @ShowField(description = ResourceIdMapper.cumulativeUsage, showInOverview = true)
    @XmllistAttribute("cum_kWh")
    private String cumulativeKwh;

    @ShowField(description = ResourceIdMapper.energy)
    @XmllistAttribute("energy")
    private String energy;

    @ShowField(description = ResourceIdMapper.power)
    @XmllistAttribute("power")
    private String power;

    @ShowField(description = ResourceIdMapper.price)
    @XmllistAttribute("price_CF")
    private String price;

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
    protected void fillDeviceCharts(List<DeviceChart> chartSeries, Context context, ChartProvider chartProvider) {
        super.fillDeviceCharts(chartSeries, context, chartProvider);

        addDeviceChartIfNotNull(
                new DeviceChart(R.string.powerGraph,
                        new ChartSeriesDescription.Builder()
                                .withColumnName(R.string.power, context)
                                .withFileLogSpec("4:")
                                .withDbLogSpec("energy::int3")
                                .withSeriesType(POWER)
                                .withShowRegression(true)
                                .withYAxisMinMaxValue(getLogDevices().get(0).getYAxisMinMaxValueFor("energy", 0, 0))
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
