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

import li.klass.fhem.domain.core.DeviceFunctionality;
import li.klass.fhem.domain.core.FhemDevice;
import li.klass.fhem.domain.core.XmllistAttribute;
import li.klass.fhem.domain.genericview.OverviewViewSettings;
import li.klass.fhem.domain.genericview.ShowField;
import li.klass.fhem.domain.heating.TemperatureDevice;
import li.klass.fhem.resources.ResourceIdMapper;

import static li.klass.fhem.util.ValueDescriptionUtil.appendPercent;
import static li.klass.fhem.util.ValueDescriptionUtil.appendTemperature;
import static li.klass.fhem.util.ValueExtractUtil.extractLeadingDouble;
import static li.klass.fhem.util.ValueExtractUtil.extractLeadingInt;

@OverviewViewSettings(showState = true, showMeasured = true)
public class FHEMduinoEnvDevice extends FhemDevice<FHEMduinoEnvDevice> implements TemperatureDevice {
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

}
