/*
 * AndFHEM - Open Source Android application to control a FHEM home automation
 * server.
 *
 * Copyright (c) 2012, Matthias Klass or third-party contributors as
 * indicated by the @author tags or express copyright attribution
 * statements applied by the authors.  All third-party contributions are
 * distributed under license by Red Hat Inc.
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU GENERAL PUBLICLICENSE, as published by the Free Software Foundation.
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
 */

package li.klass.fhem.domain;

import li.klass.fhem.util.ValueDescriptionUtil;
import org.w3c.dom.NamedNodeMap;

import java.util.*;

public class WeatherDevice extends Device<WeatherDevice> {
    class WeatherDeviceForecast {
        private String prefix;

        private String condition;
        private String dayOfWeek;
        private String highTemperature;
        private String lowTemperature;
        private String icon;

        WeatherDeviceForecast(String prefix) {
            this.prefix = prefix;
        }
    }

    public static final String IMAGE_URL_PREFIX = "http://www.google.de";

    private String condition;
    private String humidity;
    private String icon;
    private String temperature;
    private String wind;
    private Map<String, WeatherDeviceForecast> forecastMap = new HashMap<String, WeatherDeviceForecast>();

    @Override
    protected void onChildItemRead(String tagName, String keyValue, String nodeContent, NamedNodeMap attributes) {
        if (keyValue.equalsIgnoreCase("CONDITION")) {
            this.condition = nodeContent;
        } else if (keyValue.equalsIgnoreCase("HUMIDITY")) {
            this.humidity = ValueDescriptionUtil.appendPercent(nodeContent);
        } else if (keyValue.equalsIgnoreCase("ICON")) {
            this.icon = nodeContent;
        } else if (keyValue.equalsIgnoreCase("TEMP_C")) {
            this.temperature = ValueDescriptionUtil.appendTemperature(nodeContent);
        } else if (keyValue.equalsIgnoreCase("WIND_CONDITION")) {
            this.wind = nodeContent.replaceAll("Wind: ", "").trim();
        } else if (keyValue.startsWith("fc")) {
            int underscorePosition = keyValue.indexOf("_");

            String prefix = keyValue.substring(0, underscorePosition);
            String name = keyValue.substring(underscorePosition + 1);

            if (! forecastMap.containsKey(prefix)) {
                forecastMap.put(prefix, new WeatherDeviceForecast(prefix));
            }

            WeatherDeviceForecast forecast = forecastMap.get(prefix);

            if (name.equalsIgnoreCase("CONDITION")) {
                forecast.condition = nodeContent;
            } else if (name.equalsIgnoreCase("DAY_OF_WEEK")) {
                forecast.dayOfWeek = nodeContent;
            } else if (name.equalsIgnoreCase("HIGH_C")) {
                forecast.highTemperature = ValueDescriptionUtil.appendTemperature(nodeContent);
            } else if (name.equalsIgnoreCase("LOW_C")) {
                forecast.lowTemperature = ValueDescriptionUtil.appendTemperature(nodeContent);
            } else if (name.equalsIgnoreCase("ICON")) {
                forecast.icon = nodeContent;
            }
        }
    }

    public String getCondition() {
        return condition;
    }

    public String getHumidity() {
        return humidity;
    }

    public String getIcon() {
        return icon;
    }

    public String getTemperature() {
        return temperature;
    }

    public String getWind() {
        return wind;
    }

    public List<WeatherDeviceForecast> getForecasts() {
        List<String> keys = Arrays.asList(forecastMap.keySet().toArray(new String[forecastMap.size()]));
        Collections.sort(keys);

        List<WeatherDeviceForecast> result = new ArrayList<WeatherDeviceForecast>();
        for (String key : keys) {
            result.add(forecastMap.get(key));
        }

        return result;
    }
}
