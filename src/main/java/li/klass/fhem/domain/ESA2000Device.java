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
import li.klass.fhem.util.ValueExtractUtil;

import static li.klass.fhem.service.graph.description.SeriesType.CURRENT_USAGE_KILOWATT;
import static li.klass.fhem.service.graph.description.SeriesType.DAY_USAGE;

@SuppressWarnings("unused")
public class ESA2000Device extends Device<ESA2000Device> {
    @ShowField(description = ResourceIdMapper.currentUsage, showInOverview = true)
    private String current;
    @ShowField(description = ResourceIdMapper.dayUsage, showInOverview = true)
    private String day;
    @ShowField(description = ResourceIdMapper.monthUsage, showInOverview = true)
    private String month;
    @ShowField(description = ResourceIdMapper.yearUsage, showInOverview = true)
    private String year;
    @ShowField(description = ResourceIdMapper.dayLastUsage, showInOverview = false)
    private String dayLast;

    public void readACTUAL(String value) {
        double actual = ValueExtractUtil.extractLeadingDouble(value) * 1000;
        current = ValueDescriptionUtil.append((int) actual, "W");
    }

    public void readYEAR(String value) {
        year = tokWHDesc(value);
    }

    public void readMONTH(String value) {
        month = tokWHDesc(value);
    }

    public void readDAY(String value) {
        day = tokWHDesc(value);
    }

    public void readDAY_LAST(String value) {
        dayLast = tokWHDesc(value);
    }

    private String tokWHDesc(String value) {
        double number = ValueExtractUtil.extractLeadingDouble(value);
        double roundedNumber = ((int) (number * 100)) / 100d;

        return ValueDescriptionUtil.append(roundedNumber, "kWh");
    }

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
    protected void fillDeviceCharts(List<DeviceChart> chartSeries) {
        super.fillDeviceCharts(chartSeries);

        addDeviceChartIfNotNull(new DeviceChart(R.string.usageGraph,
                new ChartSeriesDescription.Builder().withColumnName(R.string.currentUsage)
                        .withFileLogSpec("8:CUR\\x3a\\s[0-9]::")
                        .withDbLogSpec("actual::int4")
                        .withSeriesType(CURRENT_USAGE_KILOWATT)
                        .withShowDiscreteValues(true)
                        .withYAxisMinMaxValue(getLogDevices().get(0).getYAxisMinMaxValueFor("actual", 0, 10))
                        .build(),
                new ChartSeriesDescription.Builder().withColumnName(R.string.dayUsage)
                        .withFileLogSpec("4:day\\x3a\\s[0-9]:0:")
                        .withDbLogSpec("day::int2")
                        .withSeriesType(DAY_USAGE)
                        .withShowDiscreteValues(true)
                        .withYAxisMinMaxValue(getLogDevices().get(0).getYAxisMinMaxValueFor("day", 0, 100))
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
