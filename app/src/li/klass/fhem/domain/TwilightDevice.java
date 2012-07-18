/*
 * AndFHEM - Open Source Android application to control a FHEM home automation
 *  server.
 *
 *  Copyright (c) 2012, Matthias Klass or third-party contributors as
 *  indicated by the @author tags or express copyright attribution
 *  statements applied by the authors.  All third-party contributions are
 *  distributed under license by Red Hat Inc.
 *
 *  This copyrighted material is made available to anyone wishing to use, modify,
 *  copy, or redistribute it subject to the terms and conditions of the GNU GENERAL PUBLICLICENSE, as published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *  or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU GENERAL PUBLIC LICENSE
 *  for more details.
 *
 *  You should have received a copy of the GNU GENERAL PUBLIC LICENSE
 *  along with this distribution; if not, write to:
 *    Free Software Foundation, Inc.
 *    51 Franklin Street, Fifth Floor
 */

package li.klass.fhem.domain;

import li.klass.fhem.AndFHEMApplication;
import li.klass.fhem.R;
import li.klass.fhem.domain.core.Device;
import li.klass.fhem.domain.genericview.ShowField;
import org.w3c.dom.NamedNodeMap;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unused")
public class TwilightDevice extends Device<TwilightDevice> {
    @ShowField(description = R.string.twilight_next_event, showInOverview = true, showInFloorplan = true)
    private String nextEvent;
    @ShowField(description = R.string.twilight_next_event_time, showInOverview = true, showInFloorplan = true)
    private String nextEventTime;

    @ShowField(description = R.string.twilight_sunrise)
    private String sunrise;
    @ShowField(description = R.string.twilight_sunrise_astronomical)
    private String sunriseAstronomical;
    @ShowField(description = R.string.twilight_sunrise_civil)
    private String sunriseCivil;
    @ShowField(description = R.string.twilight_sunrise_indoor)
    private String sunriseIndoor;
    @ShowField(description = R.string.twilight_sunrise_nautical)
    private String sunriseNautical;
    @ShowField(description = R.string.twilight_sunrise_weather)
    private String sunriseWeather;

    @ShowField(description = R.string.twilight_sunset)
    private String sunset;
    @ShowField(description = R.string.twilight_sunset_astronomical)
    private String sunsetAstronomical;
    @ShowField(description = R.string.twilight_sunset_civil)
    private String sunsetCivil;
    @ShowField(description = R.string.twilight_sunset_indoor)
    private String sunsetIndoor;
    @ShowField(description = R.string.twilight_sunset_nautical)
    private String sunsetNautical;
    @ShowField(description = R.string.twilight_sunset_weather)
    private String sunsetWeather;

    @ShowField(description = R.string.twilight_light)
    private String light;

    private static Map<Integer, Integer> lightStringIdMap = new HashMap<Integer, Integer>();
    static {
        lightStringIdMap.put(0, R.string.twilight_light_total_night);
        lightStringIdMap.put(1, R.string.twilight_light_astronomical);
        lightStringIdMap.put(2, R.string.twilight_light_nautical);
        lightStringIdMap.put(3, R.string.twilight_light_civil);
        lightStringIdMap.put(4, R.string.twilight_light_indoor);
        lightStringIdMap.put(5, R.string.twilight_light_weather);
        lightStringIdMap.put(6, R.string.twilight_light_daylight);
    }

    @Override
    protected void onChildItemRead(String tagName, String keyValue, String nodeContent, NamedNodeMap attributes) {
        if (keyValue.equalsIgnoreCase("NEXTEVENT")) {
            nextEvent = nodeContent;
        } else if (keyValue.equalsIgnoreCase("NEXTEVENTTIME")) {
            nextEventTime = nodeContent;
        } else if (keyValue.equalsIgnoreCase("LIGHT")) {
            Integer value = Integer.parseInt(nodeContent);
            light = AndFHEMApplication.getContext().getString(lightStringIdMap.get(value));
        } else if (keyValue.equalsIgnoreCase("SR")) {
            sunrise = nodeContent;
        } else if (keyValue.equalsIgnoreCase("SR_ASTRO")) {
            sunriseAstronomical = nodeContent;
        } else if (keyValue.equalsIgnoreCase("SR_CIVIL")) {
            sunriseCivil = nodeContent;
        } else if (keyValue.equalsIgnoreCase("SR_INDOOR")) {
            sunriseIndoor = nodeContent;
        } else if (keyValue.equalsIgnoreCase("SR_NAUTICAL")) {
            sunriseNautical = nodeContent;
        } else if (keyValue.equalsIgnoreCase("SR_WEATHER")) {
            sunriseWeather = nodeContent;
        } else if (keyValue.equalsIgnoreCase("SS")) {
            sunset = nodeContent;
        } else if (keyValue.equalsIgnoreCase("SS_ASTRO")) {
            sunsetAstronomical = nodeContent;
        } else if (keyValue.equalsIgnoreCase("SS_CIVIL")) {
            sunsetCivil = nodeContent;
        } else if (keyValue.equalsIgnoreCase("SS_INDOOR")) {
            sunsetIndoor = nodeContent;
        } else if (keyValue.equalsIgnoreCase("SS_NAUTICAL")) {
            sunsetNautical = nodeContent;
        } else if (keyValue.equalsIgnoreCase("SS_WEATHER")) {
            sunsetWeather = nodeContent;
        }
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
}

