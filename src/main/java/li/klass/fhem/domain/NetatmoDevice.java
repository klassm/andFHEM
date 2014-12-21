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

import li.klass.fhem.appwidget.annotation.ResourceIdMapper;
import li.klass.fhem.domain.core.Device;
import li.klass.fhem.domain.core.DeviceFunctionality;
import li.klass.fhem.domain.core.XmllistAttribute;
import li.klass.fhem.domain.genericview.ShowField;

import static li.klass.fhem.util.ValueDescriptionUtil.appendDb;
import static li.klass.fhem.util.ValueDescriptionUtil.appendHPa;
import static li.klass.fhem.util.ValueDescriptionUtil.appendPercent;
import static li.klass.fhem.util.ValueDescriptionUtil.appendPpm;
import static li.klass.fhem.util.ValueDescriptionUtil.appendTemperature;

public class NetatmoDevice extends Device<NetatmoDevice> {
    @ShowField(description = ResourceIdMapper.temperature, showInOverview = true)
    private String temperature;

    @ShowField(description = ResourceIdMapper.humidity, showInOverview = true)
    private String humidity;

    @ShowField(description = ResourceIdMapper.co2, showInOverview = true)
    private String co2;

    @ShowField(description = ResourceIdMapper.noise)
    private String noise;

    @ShowField(description = ResourceIdMapper.pressure)
    private String pressure;

    private String subType;

    public String getTemperature() {
        return temperature;
    }

    @XmllistAttribute("TEMPERATURE")
    public void setTemperature(String temperature) {
        this.temperature = appendTemperature(temperature);
    }

    public String getHumidity() {
        return humidity;
    }

    @XmllistAttribute("HUMIDITY")
    public void setHumidity(String humidity) {
        this.humidity = appendPercent(humidity);
    }

    public String getCo2() {
        return co2;
    }

    @XmllistAttribute("CO2")
    public void setCo2(String co2) {
        this.co2 = appendPpm(co2);
    }

    public String getNoise() {
        return noise;
    }

    @XmllistAttribute("NOISE")
    public void setNoise(String noise) {
        this.noise = appendDb(noise);
    }

    public String getPressure() {
        return pressure;
    }

    @XmllistAttribute("PRESSURE")
    public void setPressure(String pressure) {
        this.pressure = appendHPa(pressure);
    }

    public String getSubType() {
        return subType;
    }

    @XmllistAttribute("SUBTYPE")
    public void setSubType(String subtype) {
        this.subType = subtype;
    }

    @Override
    public DeviceFunctionality getDeviceGroup() {
        return DeviceFunctionality.WEATHER;
    }

    @Override
    public boolean isSupported() {
        return !"ACCOUNT".equalsIgnoreCase(subType);
    }
}
