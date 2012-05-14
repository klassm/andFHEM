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

import li.klass.fhem.R;
import li.klass.fhem.domain.genericview.DeviceChart;
import li.klass.fhem.domain.genericview.ShowInDetail;
import li.klass.fhem.domain.genericview.ShowInOverview;
import li.klass.fhem.service.graph.description.ChartSeriesDescription;
import li.klass.fhem.util.ValueUtil;
import org.w3c.dom.NamedNodeMap;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public class HMSDevice extends Device<HMSDevice> {
    @ShowInOverview(description = R.string.temperature)
    @ShowInDetail(description = R.string.temperature)
    private String temperature;

    @ShowInOverview(description = R.string.battery)
    @ShowInDetail(description = R.string.battery)
    private String battery;

    @ShowInOverview(description = R.string.humidity)
    @ShowInDetail(description = R.string.humidity)
    private String humidity;

    public static final Integer COLUMN_SPEC_TEMPERATURE = R.string.temperature;
    public static final Integer COLUMN_SPEC_HUMIDITY = R.string.humidity;

    @Override
    public void onChildItemRead(String tagName, String keyValue, String nodeContent, NamedNodeMap attributes) {
        if (keyValue.equals("TEMPERATURE")) {
            temperature = ValueUtil.formatTemperature(nodeContent);
        } else if (keyValue.equals("BATTERY")) {
            battery = nodeContent;
        } else if (keyValue.equals("HUMIDITY")) {
            humidity = nodeContent;
        }
    }

    @Override
    public List<DeviceChart> getFileLogColumnsListForGenericViews() {
        List<DeviceChart> charts = new ArrayList<DeviceChart>();
        if (temperature != null)
        charts.add(new DeviceChart(R.string.temperatureGraph, R.string.yAxisTemperature, ChartSeriesDescription.getRegressionValuesInstance(R.string.temperature), "4:T\\x3a:0:"));
        if (humidity != null)
            charts.add(new DeviceChart(R.string.humidityGraph, R.string.yAxisHumidity, new ChartSeriesDescription(R.string.humidity), "6:H\\x3a:0:"));
        return charts;
    }
}
