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

import java.util.Map;

import li.klass.fhem.AndFHEMApplication;
import li.klass.fhem.R;
import li.klass.fhem.domain.core.DeviceFunctionality;
import li.klass.fhem.domain.core.FhemDevice;
import li.klass.fhem.domain.core.XmllistAttribute;
import li.klass.fhem.domain.genericview.ShowField;
import li.klass.fhem.resources.ResourceIdMapper;

import static com.google.common.collect.Maps.newHashMap;

@SuppressWarnings("unused")
public class TwilightDevice extends FhemDevice<TwilightDevice> {
    private static Map<Integer, Integer> lightStringIdMap = newHashMap();

    static {
        lightStringIdMap.put(0, R.string.twilight_light_total_night);
        lightStringIdMap.put(1, R.string.twilight_light_astronomical);
        lightStringIdMap.put(2, R.string.twilight_light_nautical);
        lightStringIdMap.put(3, R.string.twilight_light_civil);
        lightStringIdMap.put(4, R.string.twilight_light_indoor);
        lightStringIdMap.put(5, R.string.twilight_light_weather);
        lightStringIdMap.put(6, R.string.twilight_light_daylight);
    }


    @ShowField(description = ResourceIdMapper.twilight_next_event, showInOverview = true)
    @XmllistAttribute("nextEvent")
    private String nextEvent;

    @ShowField(description = ResourceIdMapper.twilight_next_event_time, showInOverview = true)
    @XmllistAttribute("nextEventTime")
    private String nextEventTime;

    @ShowField(description = ResourceIdMapper.twilight_sunrise)
    @XmllistAttribute("sr")
    private String sunrise;

    @ShowField(description = ResourceIdMapper.twilight_sunrise_astronomical)
    @XmllistAttribute("sr_astro")
    private String sunriseAstronomical;

    @ShowField(description = ResourceIdMapper.twilight_sunrise_civil)
    @XmllistAttribute("sr_civil")
    private String sunriseCivil;

    @ShowField(description = ResourceIdMapper.twilight_sunrise_indoor)
    @XmllistAttribute("sr_indoor")
    private String sunriseIndoor;

    @ShowField(description = ResourceIdMapper.twilight_sunrise_nautical)
    @XmllistAttribute("sr_naut")
    private String sunriseNautical;

    @ShowField(description = ResourceIdMapper.twilight_sunrise_weather)
    @XmllistAttribute("sr_weather")
    private String sunriseWeather;

    @ShowField(description = ResourceIdMapper.twilight_sunset)
    @XmllistAttribute("ss")
    private String sunset;

    @ShowField(description = ResourceIdMapper.twilight_sunset_astronomical)
    @XmllistAttribute("ss_astro")
    private String sunsetAstronomical;

    @ShowField(description = ResourceIdMapper.twilight_sunset_civil)
    @XmllistAttribute("ss_civil")
    private String sunsetCivil;

    @ShowField(description = ResourceIdMapper.twilight_sunset_indoor)
    @XmllistAttribute("ss_indoor")
    private String sunsetIndoor;

    @ShowField(description = ResourceIdMapper.twilight_sunset_nautical)
    @XmllistAttribute("ss_naut")
    private String sunsetNautical;

    @ShowField(description = ResourceIdMapper.twilight_sunset_weather)
    @XmllistAttribute("ss_weather")
    private String sunsetWeather;

    @ShowField(description = ResourceIdMapper.twilight_light)
    private String light;

    @XmllistAttribute("light")
    public void setLight(int value) {
        this.light = AndFHEMApplication.getContext().getString(lightStringIdMap.get(value));
    }

    public String getNextEvent() {
        return nextEvent;
    }

    public String getNextEventTime() {
        return nextEventTime;
    }

    public String getSunrise() {
        return sunrise;
    }

    public String getSunriseAstronomical() {
        return sunriseAstronomical;
    }

    public String getSunriseCivil() {
        return sunriseCivil;
    }

    public String getSunriseIndoor() {
        return sunriseIndoor;
    }

    public String getSunriseNautical() {
        return sunriseNautical;
    }

    public String getSunriseWeather() {
        return sunriseWeather;
    }

    public String getSunset() {
        return sunset;
    }

    public String getSunsetAstronomical() {
        return sunsetAstronomical;
    }

    public String getSunsetCivil() {
        return sunsetCivil;
    }

    public String getSunsetIndoor() {
        return sunsetIndoor;
    }

    public String getSunsetNautical() {
        return sunsetNautical;
    }

    public String getSunsetWeather() {
        return sunsetWeather;
    }

    public String getLight() {
        return light;
    }

    @Override
    public DeviceFunctionality getDeviceGroup() {
        return DeviceFunctionality.FHEM;
    }
}

