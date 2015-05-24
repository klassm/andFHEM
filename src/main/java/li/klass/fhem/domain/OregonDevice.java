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

import li.klass.fhem.appwidget.annotation.SupportsWidget;
import li.klass.fhem.appwidget.annotation.WidgetTemperatureField;
import li.klass.fhem.appwidget.view.widget.medium.TemperatureWidgetView;
import li.klass.fhem.domain.core.DeviceFunctionality;
import li.klass.fhem.domain.core.FhemDevice;
import li.klass.fhem.domain.core.XmllistAttribute;
import li.klass.fhem.domain.genericview.ShowField;
import li.klass.fhem.domain.heating.TemperatureDevice;
import li.klass.fhem.resources.ResourceIdMapper;

@SupportsWidget(TemperatureWidgetView.class)
public class OregonDevice extends FhemDevice<OregonDevice> implements TemperatureDevice {

    @ShowField(description = ResourceIdMapper.humidity, showInOverview = true)
    @XmllistAttribute("humidity")
    private String humidity;

    @WidgetTemperatureField
    @ShowField(description = ResourceIdMapper.temperature, showInOverview = true)
    @XmllistAttribute("temperature")
    private String temperature;

    @ShowField(description = ResourceIdMapper.forecast, showInOverview = true)
    @XmllistAttribute("forecast")
    private String forecast;

    @ShowField(description = ResourceIdMapper.dewpoint)
    @XmllistAttribute("dewpoint")
    private String dewpoint;

    @ShowField(description = ResourceIdMapper.pressure)
    @XmllistAttribute("pressure")
    private String pressure;

    @ShowField(description = ResourceIdMapper.battery)
    @XmllistAttribute("battery")
    private String battery;

    @ShowField(description = ResourceIdMapper.rainRate, showInOverview = true)
    @XmllistAttribute("rain_rate")
    private String rainRate;

    @ShowField(description = ResourceIdMapper.rainTotal, showInOverview = true)
    @XmllistAttribute("rain_total")
    private String rainTotal;

    @ShowField(description = ResourceIdMapper.windAvgSpeed, showInOverview = true)
    @XmllistAttribute("wind_avspeed")
    private String windAvgSpeed;

    @ShowField(description = ResourceIdMapper.windDirection, showInOverview = true)
    @XmllistAttribute("wind_dir")
    private String windDirection;

    @ShowField(description = ResourceIdMapper.windSpeed, showInOverview = true)
    @XmllistAttribute("wind_speed")
    private String windSpeed;

    @ShowField(description = ResourceIdMapper.uvValue, showInOverview = true)
    @XmllistAttribute("uv_val")
    private String uvValue;

    @ShowField(description = ResourceIdMapper.uvRisk, showInOverview = true)
    @XmllistAttribute("uv_risk")
    private String uvRisk;

    @XmllistAttribute("time")
    public void setTime(String value) {
        setMeasured(value);
    }

    public String getHumidity() {
        return humidity;
    }

    public String getTemperature() {
        return temperature;
    }

    public String getForecast() {
        return forecast;
    }

    public String getDewpoint() {
        return dewpoint;
    }

    public String getPressure() {
        return pressure;
    }

    public String getBattery() {
        return battery;
    }

    public String getRainRate() {
        return rainRate;
    }

    public String getRainTotal() {
        return rainTotal;
    }

    public String getWindAvgSpeed() {
        return windAvgSpeed;
    }

    public String getWindDirection() {
        return windDirection;
    }

    public String getWindSpeed() {
        return windSpeed;
    }

    public String getUvValue() {
        return uvValue;
    }

    public String getUvRisk() {
        return uvRisk;
    }

    @Override
    public DeviceFunctionality getDeviceGroup() {
        return DeviceFunctionality.WEATHER;
    }

    @Override
    public boolean isSensorDevice() {
        return true;
    }
}
