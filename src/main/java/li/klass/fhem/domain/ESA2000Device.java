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

import static li.klass.fhem.service.graph.description.SeriesType.CURRENT_USAGE_KILOWATT;
import static li.klass.fhem.service.graph.description.SeriesType.DAY_USAGE;

@SuppressWarnings("unused")
public class ESA2000Device extends FhemDevice<ESA2000Device> {
    @ShowField(description = ResourceIdMapper.currentUsage, showInOverview = true)
    @XmllistAttribute("actual")
    private String current;

    @ShowField(description = ResourceIdMapper.dayUsage, showInOverview = true)
    @XmllistAttribute("day")
    private String day;

    @ShowField(description = ResourceIdMapper.monthUsage, showInOverview = true)
    @XmllistAttribute("month")
    private String month;

    @ShowField(description = ResourceIdMapper.yearUsage, showInOverview = true)
    @XmllistAttribute("year")
    private String year;

    @ShowField(description = ResourceIdMapper.dayLastUsage, showInOverview = false)
    @XmllistAttribute("day_last")
    private String dayLast;

    public String getCurrent() {
        return current;
    }

    public String getDay() {
        return day;
    }

    public String getMonth() {
        return month;
    }

    public String getYear() {
        return year;
    }

    public String getDayLast() {
        return dayLast;
    }

    @Override
    protected void fillDeviceCharts(List<DeviceChart> chartSeries, Context context, ChartProvider chartProvider) {
        super.fillDeviceCharts(chartSeries, context, chartProvider);

        addDeviceChartIfNotNull(new DeviceChart(R.string.usageGraph,
                new ChartSeriesDescription.Builder().withColumnName(R.string.currentUsage, context)
                        .withFileLogSpec("8:CUR\\x3a\\s[0-9]::")
                        .withDbLogSpec("actual::int4")
                        .withSeriesType(CURRENT_USAGE_KILOWATT)
                        .withShowDiscreteValues(true)
                        .withYAxisMinMaxValue(getLogDevices().get(0).getYAxisMinMaxValueFor("actual", 0, 0))
                        .build(),
                new ChartSeriesDescription.Builder().withColumnName(R.string.dayUsage, context)
                        .withFileLogSpec("4:day\\x3a\\s[0-9]:0:")
                        .withDbLogSpec("day::int2")
                        .withSeriesType(DAY_USAGE)
                        .withShowDiscreteValues(true)
                        .withYAxisMinMaxValue(getLogDevices().get(0).getYAxisMinMaxValueFor("day", 0, 0))
                        .build()
        ), current);
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
