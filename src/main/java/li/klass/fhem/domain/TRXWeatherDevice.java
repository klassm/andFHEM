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
    public DeviceFunctionality getDeviceGroup() {
        return DeviceFunctionality.WEATHER;
    }

    @Override
    public boolean isSensorDevice() {
        return true;
    }
}
