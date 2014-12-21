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

import static li.klass.fhem.service.graph.description.SeriesType.CUMULATIVE_USAGE_Wh;
import static li.klass.fhem.service.graph.description.SeriesType.POWER;
import static li.klass.fhem.util.ValueDescriptionUtil.appendKWh;
import static li.klass.fhem.util.ValueDescriptionUtil.appendW;

public class SMLUSBDevice extends Device<SMLUSBDevice> {

    @ShowField(showInOverview = true, description = ResourceIdMapper.currentUsage)
    private String power;
    @ShowField(showInOverview = true, description = ResourceIdMapper.counterReading)
    private String counterReading;
    private String counterReadingTariff1;

    @Override
    public DeviceFunctionality getDeviceGroup() {
        return DeviceFunctionality.USAGE;
    }

    public String getPower() {
        return power;
    }

    @XmllistAttribute("MOMENTANLEISTUNG")
    public void setPower(String power) {
        this.power = appendW(power);
    }

    public String getCounterReading() {
        return counterReading;
    }

    @XmllistAttribute("ZÄHLERSTAND_BEZUG_TOTAL")
    public void setCounterReading(String counterReading) {
        this.counterReading = appendKWh(counterReading);
    }

    public String getCounterReadingTariff1() {
        return counterReadingTariff1;
    }

    @XmllistAttribute("ZÄHLERSTAND_TARIF_1_BEZUG")
    public void setCounterReadingTariff1(String counterReadingTariff1) {
        this.counterReadingTariff1 = appendKWh(counterReadingTariff1);
    }

    @Override
    protected void fillDeviceCharts(List<DeviceChart> chartSeries) {
        super.fillDeviceCharts(chartSeries);

        addDeviceChartIfNotNull(new DeviceChart(R.string.powerGraph,
                new ChartSeriesDescription.Builder()
                        .withColumnName(R.string.power)
                        .withFileLogSpec("4:Momentanleistung:")
                        .withDbLogSpec("Momentanleistung::int1")
                        .withSeriesType(POWER)
                        .withShowRegression(true)
                        .withYAxisMinMaxValue(getLogDevices().get(0).getYAxisMinMaxValueFor("Momentanleistung", 0, 0))
                        .build()
        ), power);

        addDeviceChartIfNotNull(new DeviceChart(R.string.usageGraph,
                new ChartSeriesDescription.Builder()
                        .withColumnName(R.string.usageGraph).withFileLogSpec("4:Zählerstand-Tarif-1-Bezug:")
                        .withDbLogSpec("ZÄHLERSTAND_BEZUG_TOTAL::int")
                        .withSeriesType(CUMULATIVE_USAGE_Wh)
                        .withYAxisMinMaxValue(getLogDevices().get(0).getYAxisMinMaxValueFor("ZÄHLERSTAND_BEZUG_TOTAL", 0, 0))
                        .build()
        ), counterReading);
    }

}
