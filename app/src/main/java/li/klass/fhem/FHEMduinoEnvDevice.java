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

package li.klass.fhem;

import java.util.List;

import li.klass.fhem.appwidget.annotation.ResourceIdMapper;
import li.klass.fhem.domain.core.Device;
import li.klass.fhem.domain.core.DeviceChart;
import li.klass.fhem.domain.core.DeviceFunctionality;
import li.klass.fhem.domain.core.XmllistAttribute;
import li.klass.fhem.domain.genericview.OverviewViewSettings;
import li.klass.fhem.domain.genericview.ShowField;
import li.klass.fhem.domain.heating.TemperatureDevice;
import li.klass.fhem.service.graph.description.ChartSeriesDescription;

import static li.klass.fhem.service.graph.description.SeriesType.DEWPOINT;
import static li.klass.fhem.service.graph.description.SeriesType.HUMIDITY;
import static li.klass.fhem.service.graph.description.SeriesType.TEMPERATURE;
import static li.klass.fhem.util.ValueDescriptionUtil.appendPercent;
import static li.klass.fhem.util.ValueDescriptionUtil.appendTemperature;
import static li.klass.fhem.util.ValueExtractUtil.extractLeadingDouble;
import static li.klass.fhem.util.ValueExtractUtil.extractLeadingInt;

@OverviewViewSettings(showState = true, showMeasured = true)
public class FHEMduinoEnvDevice extends Device<FHEMduinoEnvDevice> implements TemperatureDevice {
    @XmllistAttribute("battery")
    @ShowField(description = ResourceIdMapper.battery)
    private String battery;

    @ShowField(description = ResourceIdMapper.temperature)
    private String temperature;

    @ShowField(description = ResourceIdMapper.humidity)
    private String humidity;

    @ShowField(description = ResourceIdMapper.dewpoint)
    private String dewpoint;


    @XmllistAttribute("IODev")
    @ShowField(description = ResourceIdMapper.ioDev)
    private String ioDev;

    @Override
    public DeviceFunctionality getDeviceGroup() {
        return DeviceFunctionality.TEMPERATURE;
    }

    @XmllistAttribute("temperature")
    public void setTemperature(String temperature) {
        this.temperature = appendTemperature(extractLeadingDouble(temperature));
    }

    @XmllistAttribute("taupunkttemp")
    public void setDewpoint(String dewpoint) {
        this.dewpoint = appendTemperature(extractLeadingDouble(dewpoint));
    }

    @XmllistAttribute("humidity")
    public void setHumidity(String humidity) {
        this.humidity = appendPercent(extractLeadingInt(humidity));
    }

    public String getBattery() {
        return battery;
    }

    @Override
    public String getTemperature() {
        return temperature;
    }

    public String getHumidity() {
        return humidity;
    }

    public String getDewpoint() {
        return dewpoint;
    }

    public String getIoDev() {
        return ioDev;
    }

    @Override
    protected void fillDeviceCharts(List<DeviceChart> chartSeries) {
        super.fillDeviceCharts(chartSeries);

        addDeviceChartIfNotNull(new DeviceChart(R.string.temperatureHumidityDewpointGraph,
                new ChartSeriesDescription.Builder()
                        .withColumnName(R.string.temperature)
                        .withFileLogSpec("4:temperature:0")
                        .withDbLogSpec("temperature")
                        .withSeriesType(TEMPERATURE)
                        .withShowRegression(true)
                        .withYAxisMinMaxValue(getLogDevices().get(0).getYAxisMinMaxValueFor("temperature", 0, 30))
                        .build(),
                new ChartSeriesDescription.Builder()
                        .withColumnName(R.string.humidity).withFileLogSpec("4:humidity:0")
                        .withDbLogSpec("humidity")
                        .withSeriesType(HUMIDITY)
                        .withYAxisMinMaxValue(getLogDevices().get(0).getYAxisMinMaxValueFor("humidity", 0, 100))
                        .build(),
                new ChartSeriesDescription.Builder()
                        .withColumnName(R.string.dewpoint).withFileLogSpec("4:taupunkttemp:0")
                        .withDbLogSpec("taupunkttemp")
                        .withSeriesType(DEWPOINT)
                        .withYAxisMinMaxValue(getLogDevices().get(0).getYAxisMinMaxValueFor("taupunkttemp", -10, 10))
                        .build()
        ), temperature, humidity, dewpoint);
    }
}
