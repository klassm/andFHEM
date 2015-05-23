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
import li.klass.fhem.domain.genericview.ShowField;
import li.klass.fhem.resources.ResourceIdMapper;

import static com.google.common.base.Strings.isNullOrEmpty;

public class GPIO4Device extends FhemDevice<GPIO4Device> {

    @ShowField(description = ResourceIdMapper.temperature, showInOverview = true)
    @XmllistAttribute("temperature")
    private String temperature;

    @ShowField(description = ResourceIdMapper.avgDay)
    @XmllistAttribute("T_avg_day")
    private String averageDay;

    @ShowField(description = ResourceIdMapper.avgMonth)
    @XmllistAttribute("T_avg_month")
    private String averageMonth;

    @ShowField(description = ResourceIdMapper.maxDay)
    @XmllistAttribute("T_max_day")
    private String maxDay;

    @ShowField(description = ResourceIdMapper.maxMonth)
    @XmllistAttribute("T_max_month")
    private String maxMonth;

    @ShowField(description = ResourceIdMapper.minDay)
    @XmllistAttribute("T_min_day")
    private String minDay;

    @ShowField(description = ResourceIdMapper.minMonth)
    @XmllistAttribute("T_min_month")
    private String minMonth;

    public String getTemperature() {
        return temperature;
    }

    @Override
    public boolean isSupported() {
        return super.isSupported() && !isNullOrEmpty(temperature);
    }

    public String getAverageMonth() {
        return averageMonth;
    }

    public String getMaxDay() {
        return maxDay;
    }

    public String getMaxMonth() {
        return maxMonth;
    }

    public String getMinDay() {
        return minDay;
    }

    public String getMinMonth() {
        return minMonth;
    }

    public String getAverageDay() {
        return averageDay;
    }

    @Override
    public DeviceFunctionality getDeviceGroup() {
        return DeviceFunctionality.TEMPERATURE;
    }

    @Override
    public boolean isSensorDevice() {
        return true;
    }
}
