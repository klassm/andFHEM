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
import li.klass.fhem.appwidget.annotation.SupportsWidget;
import li.klass.fhem.appwidget.annotation.WidgetMediumLine1;
import li.klass.fhem.appwidget.annotation.WidgetMediumLine2;
import li.klass.fhem.appwidget.annotation.WidgetMediumLine3;
import li.klass.fhem.appwidget.view.widget.medium.MediumInformationWidgetView;
import li.klass.fhem.domain.core.Device;
import li.klass.fhem.domain.core.DeviceChart;
import li.klass.fhem.domain.genericview.FloorplanViewSettings;
import li.klass.fhem.domain.genericview.ShowField;
import li.klass.fhem.service.graph.description.ChartSeriesDescription;
import li.klass.fhem.util.ValueDescriptionUtil;

@FloorplanViewSettings(showState = true)
@SupportsWidget(MediumInformationWidgetView.class)
@SuppressWarnings("unused")
public class CULEMDevice extends Device<CULEMDevice> {

    @ShowField(description = ResourceIdMapper.currentUsage, showInOverview = true)
    @WidgetMediumLine1
    private String currentUsage;
    @ShowField(description = ResourceIdMapper.dayUsage, showInOverview = true)
    @WidgetMediumLine2
    private String dayUsage;
    @ShowField(description = ResourceIdMapper.monthUsage, showInOverview = true)
    @WidgetMediumLine3
    private String monthUsage;
    private double sumGraphDivisionFactor = 1d;

    public void readCURRENT(String value) {
        currentUsage = ValueDescriptionUtil.appendKwh(value);
    }

    public void readCUM_DAY(String value) {
        dayUsage = ValueDescriptionUtil.appendKwh(extractCumUsage(value, "CUM_DAY"));
    }

    public void readCUM_MONTH(String value) {
        monthUsage = ValueDescriptionUtil.appendKwh(extractCumUsage(value, "CUM_MONTH"));
    }

    public void readSUM_GRAPH_DIVISION_FACTOR(String value) {
        sumGraphDivisionFactor = Double.valueOf(value);
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

    public double getSumGraphDivisionFactor() {
        return sumGraphDivisionFactor;
    }

    public String getCurrentUsage() {
        return currentUsage;
    }

    private String extractCumUsage(String cumString, String cumToken) {
        cumToken = cumToken + ": ";
        return cumString.substring(cumToken.length(), cumString.indexOf(" ", cumToken.length() + 1));
    }

    @Override
    protected void fillDeviceCharts(List<DeviceChart> chartSeries) {
        addDeviceChartIfNotNull(currentUsage, new DeviceChart(R.string.usageGraph, R.string.yAxisUsage,
                ChartSeriesDescription.getSumInstance(R.string.currentUsage, "8::0:", getSumGraphDivisionFactor())));
    }
}
