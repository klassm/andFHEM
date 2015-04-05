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
import li.klass.fhem.appwidget.annotation.WidgetMediumLine1;
import li.klass.fhem.appwidget.annotation.WidgetMediumLine2;
import li.klass.fhem.appwidget.annotation.WidgetTemperatureAdditionalField;
import li.klass.fhem.appwidget.annotation.WidgetTemperatureField;
import li.klass.fhem.appwidget.view.widget.medium.MediumInformationWidgetView;
import li.klass.fhem.appwidget.view.widget.medium.TemperatureWidgetView;
import li.klass.fhem.domain.core.DeviceFunctionality;
import li.klass.fhem.domain.core.FhemDevice;
import li.klass.fhem.domain.core.XmllistAttribute;
import li.klass.fhem.domain.genericview.ShowField;
import li.klass.fhem.domain.heating.TemperatureDevice;
import li.klass.fhem.resources.ResourceIdMapper;

@SuppressWarnings("unused")
@SupportsWidget({TemperatureWidgetView.class, MediumInformationWidgetView.class})
public class OpenWeatherMapDevice extends FhemDevice<OpenWeatherMapDevice> implements TemperatureDevice {
    @ShowField(description = ResourceIdMapper.temperature, showInOverview = true)
    @WidgetTemperatureField
    @WidgetMediumLine1
    @XmllistAttribute("c_temperature")
    private String temperature;

    @ShowField(description = ResourceIdMapper.humidity, showInOverview = true)
    @WidgetMediumLine2
    @WidgetTemperatureAdditionalField
    @XmllistAttribute("c_humidity")
    private String humidity;

    @ShowField(description = ResourceIdMapper.windDirection, showInOverview = true)
    @XmllistAttribute("c_windDir")
    private String windDirection;

    @ShowField(description = ResourceIdMapper.windSpeed, showInOverview = true)
    @XmllistAttribute("c_windSpeed")
    private String windSpeed;

    @ShowField(description = ResourceIdMapper.temperatureMinimum)
    @XmllistAttribute("c_tempMin")
    private String temperatureMinimum;

    @ShowField(description = ResourceIdMapper.temperatureMaximum)
    @XmllistAttribute("c_tempMax")
    private String temperatureMaximum;

    @ShowField(description = ResourceIdMapper.sunrise)
    @XmllistAttribute("c_sunrise")
    private String sunrise;

    @ShowField(description = ResourceIdMapper.sunset)
    @XmllistAttribute("c_sunset")
    private String sunset;

    public String getTemperature() {
        return temperature;
    }

    public String getHumidity() {
        return humidity;
    }

    public String getWindDirection() {
        return windDirection;
    }

    public String getWindSpeed() {
        return windSpeed;
    }

    public String getTemperatureMinimum() {
        return temperatureMinimum;
    }

    public String getTemperatureMaximum() {
        return temperatureMaximum;
    }

    public String getSunrise() {
        return sunrise;
    }

    public String getSunset() {
        return sunset;
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
