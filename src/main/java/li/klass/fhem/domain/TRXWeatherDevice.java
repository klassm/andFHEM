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
import li.klass.fhem.appwidget.annotation.WidgetTemperatureField;
import li.klass.fhem.appwidget.view.widget.medium.TemperatureWidgetView;
import li.klass.fhem.domain.core.DeviceChart;
import li.klass.fhem.domain.core.DeviceFunctionality;
import li.klass.fhem.domain.core.FhemDevice;
import li.klass.fhem.domain.core.XmllistAttribute;
import li.klass.fhem.domain.genericview.ShowField;
import li.klass.fhem.domain.heating.TemperatureDevice;
import li.klass.fhem.resources.ResourceIdMapper;
import li.klass.fhem.service.graph.description.ChartSeriesDescription;

import static li.klass.fhem.service.graph.description.SeriesType.DEWPOINT;
import static li.klass.fhem.service.graph.description.SeriesType.HUMIDITY;
import static li.klass.fhem.service.graph.description.SeriesType.PRESSURE;
import static li.klass.fhem.service.graph.description.SeriesType.TEMPERATURE;

@SupportsWidget(TemperatureWidgetView.class)
public class TRXWeatherDevice extends FhemDevice<TRXWeatherDevice> implements TemperatureDevice {

    @WidgetTemperatureField
    @ShowField(description = ResourceIdMapper.temperature, showInOverview = true)
    @XmllistAttribute("temperature")
    private String temperature;

    @ShowField(description = ResourceIdMapper.battery, showInOverview = true)
    @XmllistAttribute("battery")
    private String battery;

    @ShowField(description = ResourceIdMapper.humidity, showInOverview = true)
    @XmllistAttribute("humidity")
    private String humidity;

    @ShowField(description = ResourceIdMapper.dewpoint, showInOverview = false)
    @XmllistAttribute("dewpoint")
    private String dewpoint;

    @ShowField(description = ResourceIdMapper.rain, showInOverview = false)
    @XmllistAttribute("rain_total")
    private String rain;

    @ShowField(description = ResourceIdMapper.windSpeed, showInOverview = true)
    @XmllistAttribute("wind_speed")
    private String windSpeed;

    @ShowField(description = ResourceIdMapper.windDirection, showInOverview = true)
    @XmllistAttribute("wind_dir")
    private String windDirection;

    @ShowField(description = ResourceIdMapper.windAvgSpeed, showInOverview = false)
    @XmllistAttribute("wind_avspeed")
    private String windAverageSpeed;

    @ShowField(description = ResourceIdMapper.windchill, showInOverview = false)
    @XmllistAttribute("windchill")
    private String windchill;

    public String getTemperature() {
        return temperature;
    }

    public String getBattery() {
        return battery;
    }

    public String getHumidity() {
        return humidity;
    }

    public String getDewpoint() {
        return dewpoint;
    }

    public String getRain() {
        return rain;
    }

    public String getWindSpeed() {
        return windSpeed;
    }

    public String getWindDirection() {
        return windDirection;
    }

    public String getWindAverageSpeed() {
        return windAverageSpeed;
    }

    public String getWindchill() {
        return windchill;
    }

    @Override
    protected void fillDeviceCharts(List<DeviceChart> chartSeries, Context context) {
        super.fillDeviceCharts(chartSeries, context);

        addDeviceChartIfNotNull(new DeviceChart(R.string.temperatureGraph,
                new ChartSeriesDescription.Builder()
                        .withColumnName(R.string.temperature, context)
                        .withFileLogSpec("4:temperature:")
                        .withDbLogSpec("temperature::int2")
                        .withSeriesType(TEMPERATURE)
                        .withShowRegression(true)
                        .build(),
                new ChartSeriesDescription.Builder()
                        .withColumnName(R.string.dewpoint, context).withFileLogSpec("4:dewpoint:0:")
                        .withDbLogSpec("dewpoint::int1")
                        .withSeriesType(DEWPOINT)
                        .withYAxisMinMaxValue(getLogDevices().get(0).getYAxisMinMaxValueFor("dewpoint", -10, 10))
                        .build()
        ), temperature);

        addDeviceChartIfNotNull(new DeviceChart(R.string.humidityGraph,
                new ChartSeriesDescription.Builder()
                        .withColumnName(R.string.humidity, context).withFileLogSpec("4:humidity:0:")
                        .withDbLogSpec("humidity")
                        .withSeriesType(HUMIDITY)
                        .withYAxisMinMaxValue(getLogDevices().get(0).getYAxisMinMaxValueFor("humidity", 0, 100))
                        .build()
        ), humidity);

        addDeviceChartIfNotNull(new DeviceChart(R.string.pressureGraph,
                new ChartSeriesDescription.Builder()
                        .withColumnName(R.string.pressure, context).withFileLogSpec("4:pressure:0:")
                        .withDbLogSpec("pressure")
                        .withSeriesType(PRESSURE)
                        .withYAxisMinMaxValue(getLogDevices().get(0).getYAxisMinMaxValueFor("pressure", 700, 1200))
                        .build()
        ));
    }

    @Override
    public DeviceFunctionality getDeviceGroup() {
        return DeviceFunctionality.WEATHER;
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
