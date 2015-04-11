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
import li.klass.fhem.appwidget.annotation.SupportsWidget;
import li.klass.fhem.appwidget.annotation.WidgetMediumLine1;
import li.klass.fhem.appwidget.annotation.WidgetMediumLine2;
import li.klass.fhem.appwidget.annotation.WidgetMediumLine3;
import li.klass.fhem.appwidget.view.widget.medium.MediumInformationWidgetView;
import li.klass.fhem.domain.core.ChartProvider;
import li.klass.fhem.domain.core.DeviceChart;
import li.klass.fhem.domain.core.DeviceFunctionality;
import li.klass.fhem.domain.core.FhemDevice;
import li.klass.fhem.domain.core.XmllistAttribute;
import li.klass.fhem.domain.genericview.ShowField;
import li.klass.fhem.resources.ResourceIdMapper;
import li.klass.fhem.service.graph.description.ChartSeriesDescription;

import static li.klass.fhem.service.graph.description.SeriesType.CURRENT_USAGE_KILOWATT;
import static li.klass.fhem.util.ValueDescriptionUtil.appendKWh;

@SupportsWidget(MediumInformationWidgetView.class)
@SuppressWarnings("unused")
public class CULEMDevice extends FhemDevice<CULEMDevice> {

    @ShowField(description = ResourceIdMapper.currentUsage, showInOverview = true)
    @WidgetMediumLine1
    @XmllistAttribute("current")
    private String currentUsage;

    @ShowField(description = ResourceIdMapper.dayUsage, showInOverview = true)
    @WidgetMediumLine2
    private String dayUsage;

    @ShowField(description = ResourceIdMapper.monthUsage, showInOverview = true)
    @WidgetMediumLine3
    private String monthUsage;

    @XmllistAttribute("SUM_GRAPH_DIVISION_FACTOR")
    private double sumGraphDivisionFactor = 1d;

    @ShowField(description = ResourceIdMapper.cumulativeUsage, showInOverview = true)
    @XmllistAttribute("total")
    private String cumulativeKwh;


    @XmllistAttribute("cum_day")
    public void setCumDay(String value) {
        dayUsage = appendKWh(extractCumUsage(value, "CUM_DAY"));
    }

    @XmllistAttribute("cum_month")
    public void setCumMonth(String value) {
        monthUsage = appendKWh(extractCumUsage(value, "CUM_MONTH"));
    }

    private String extractCumUsage(String cumString, String cumToken) {
        cumToken = cumToken + ": ";
        return cumString.substring(cumToken.length(), cumString.indexOf(" ", cumToken.length() + 1));
    }

    public String readCurrentUsage() {
        return currentUsage;
    }

    public String getDayUsage() {
        return dayUsage;
    }

    public String getMonthUsage() {
        return monthUsage;
    }

    public String getCurrentUsage() {
        return currentUsage;
    }

    public String getCumulativeKwh() {
        return cumulativeKwh;
    }

    @Override
    protected void fillDeviceCharts(List<DeviceChart> chartSeries, Context context, ChartProvider chartProvider) {
        super.fillDeviceCharts(chartSeries, context, chartProvider);

        addDeviceChartIfNotNull(new DeviceChart(R.string.usageGraph,
                new ChartSeriesDescription.Builder()
                        .withColumnName(R.string.currentUsage, context)
                        .withFileLogSpec("8::0:")
                        .withDbLogSpec("current")
                        .withSumDivisionFactor(getSumGraphDivisionFactor())
                        .withShowSum(true)
                        .withSeriesType(CURRENT_USAGE_KILOWATT)
                        .withYAxisMinMaxValue(getLogDevices().get(0).getYAxisMinMaxValueFor("current", 0, 0))
                        .build()
        ), currentUsage);
    }

    public double getSumGraphDivisionFactor() {
        return sumGraphDivisionFactor;
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
