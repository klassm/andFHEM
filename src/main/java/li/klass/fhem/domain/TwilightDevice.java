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
import li.klass.fhem.appwidget.annotation.ResourceIdMapper;
import li.klass.fhem.domain.core.Device;
import li.klass.fhem.domain.core.DeviceFunctionality;
import li.klass.fhem.domain.genericview.ShowField;

import static com.google.common.collect.Maps.newHashMap;

@SuppressWarnings("unused")
public class TwilightDevice extends Device<TwilightDevice> {
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
    private String nextEvent;
    @ShowField(description = ResourceIdMapper.twilight_next_event_time, showInOverview = true)
    private String nextEventTime;
    @ShowField(description = ResourceIdMapper.twilight_sunrise)
    private String sunrise;
    @ShowField(description = ResourceIdMapper.twilight_sunrise_astronomical)
    private String sunriseAstronomical;
    @ShowField(description = ResourceIdMapper.twilight_sunrise_civil)
    private String sunriseCivil;
    @ShowField(description = ResourceIdMapper.twilight_sunrise_indoor)
    private String sunriseIndoor;
    @ShowField(description = ResourceIdMapper.twilight_sunrise_nautical)
    private String sunriseNautical;
    @ShowField(description = ResourceIdMapper.twilight_sunrise_weather)
    private String sunriseWeather;
    @ShowField(description = ResourceIdMapper.twilight_sunset)
    private String sunset;
    @ShowField(description = ResourceIdMapper.twilight_sunset_astronomical)
    private String sunsetAstronomical;
    @ShowField(description = ResourceIdMapper.twilight_sunset_civil)
    private String sunsetCivil;
    @ShowField(description = ResourceIdMapper.twilight_sunset_indoor)
    private String sunsetIndoor;
    @ShowField(description = ResourceIdMapper.twilight_sunset_nautical)
    private String sunsetNautical;
    @ShowField(description = ResourceIdMapper.twilight_sunset_weather)
    private String sunsetWeather;
    @ShowField(description = ResourceIdMapper.twilight_light)
    private String light;

    public void readNEXTEVENT(String value) {
        nextEvent = value;
    }

    public void readNEXTEVENTTIME(String value) {
        nextEventTime = value;
    }

    public void readLIGHT(String value) {
        Integer light = Integer.parseInt(value);
        this.light = AndFHEMApplication.getContext().getString(lightStringIdMap.get(light));
    }

    public void readSR(String value) {
        sunrise = value;
    }

    public void readSR_ASTRO(String value) {
        sunriseAstronomical = value;
    }

    public void readSR_CIVIL(String value) {
        sunriseCivil = value;
    }

    public void readSR_INDOOR(String value) {
        sunriseIndoor = value;
    }

    public void readSR_NAUT(String value) {
        sunriseNautical = value;
    }

    public void readSR_WEATHER(String value) {
        sunriseWeather = value;
    }

    public void readSS(String value) {
        sunset = value;
    }

    public void readSS_ASTRO(String value) {
        sunsetAstronomical = value;
    }

    public void readSS_CIVIL(String value) {
        sunsetCivil = value;
    }

    public void readSS_INDOOR(String value) {
        sunsetIndoor = value;
    }

    public void readSS_NAUT(String value) {
        sunsetNautical = value;
    }

    public void readSS_WEATHER(String value) {
        sunsetWeather = value;
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

