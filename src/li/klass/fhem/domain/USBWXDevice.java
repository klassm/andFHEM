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
import li.klass.fhem.service.graph.description.ChartSeriesDescription;
import li.klass.fhem.util.ValueDescriptionUtil;
import org.w3c.dom.NamedNodeMap;

import java.util.List;

public class USBWXDevice extends Device<USBWXDevice> {
    
    private String humidity;
    private String temperature;
    private String dewpoint;

    public static final Integer COLUMN_SPEC_TEMPERATURE = R.string.temperature;
    public static final Integer COLUMN_SPEC_HUMIDITY = R.string.humidity;

    
    @Override
    protected void onChildItemRead(String tagName, String keyValue, String nodeContent, NamedNodeMap attributes) {
        if (keyValue.equals("TEMPERATURE")) {
            this.temperature = ValueDescriptionUtil.appendTemperature(nodeContent);
        } else if (keyValue.equals("HUMIDITY")) {
            this.humidity = ValueDescriptionUtil.appendPercent(nodeContent);
        } else if (keyValue.equals("DEWPOINT")) {
            this.dewpoint = ValueDescriptionUtil.appendTemperature(nodeContent);
        } else if (keyValue.equals("TIME")) {
            measured = nodeContent;
        }
    }

    public String getHumidity() {
        return humidity;
    }

    public String getTemperature() {
        return temperature;
    }

    public String getDewpoint() {
        return dewpoint;
    }

    @Override
    protected void fillDeviceCharts(List<DeviceChart> chartSeries) {
        addDeviceChartIfNotNull(temperature, new DeviceChart(R.string.temperatureGraph, R.string.yAxisTemperature,
                ChartSeriesDescription.getRegressionValuesInstance(R.string.temperature, "4:temperature:0:")));
        addDeviceChartIfNotNull(humidity, new DeviceChart(R.string.humidityGraph, R.string.yAxisHumidity,
                new ChartSeriesDescription(R.string.temperature, "4:humidity:0:")));
    }
}
