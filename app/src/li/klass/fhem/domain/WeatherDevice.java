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

import android.util.Log;
import li.klass.fhem.R;
import li.klass.fhem.appwidget.annotation.*;
import li.klass.fhem.appwidget.view.widget.medium.MediumInformationWidgetView;
import li.klass.fhem.appwidget.view.widget.medium.TemperatureWidgetView;
import li.klass.fhem.domain.core.Device;
import li.klass.fhem.domain.core.DeviceChart;
import li.klass.fhem.domain.genericview.FloorplanViewSettings;
import li.klass.fhem.service.graph.description.ChartSeriesDescription;
import li.klass.fhem.util.ValueDescriptionUtil;
import org.w3c.dom.NamedNodeMap;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@FloorplanViewSettings(showState = true)
@SupportsWidget({TemperatureWidgetView.class, MediumInformationWidgetView.class})
public class WeatherDevice extends Device<WeatherDevice> {
    public static class WeatherDeviceForecast implements Comparable<WeatherDeviceForecast>, Serializable {
        private static final SimpleDateFormat forecastDateFormat = new SimpleDateFormat("yyyy-MM-dd");

        private String date;

        private String condition;
        private String dayOfWeek;
        private String highTemperature;
        private String lowTemperature;
        private String icon;

        WeatherDeviceForecast(String date) {
            this.date = date;
        }

        @Override
        public int compareTo(WeatherDeviceForecast weatherDeviceForecast) {
            return date.compareTo(weatherDeviceForecast.date);
        }

        public String getDate() {
            return date;
        }

        public String getCondition() {
            return condition;
        }

        public String getDayOfWeek() {
            return dayOfWeek;
        }

        public String getHighTemperature() {
            return highTemperature;
        }

        public String getLowTemperature() {
            return lowTemperature;
        }

        public String getIcon() {
            return icon;
        }
    }

    public static final String IMAGE_URL_PREFIX = "http://www.google.de";

    private static final SimpleDateFormat parseDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @WidgetMediumLine3
    private String condition;

    @WidgetTemperatureAdditionalField
    @WidgetMediumLine2
    private String humidity;

    private String icon;

    @WidgetTemperatureField
    @WidgetMediumLine1
    private String temperature;

    private String wind;

    private Map<String, WeatherDeviceForecast> forecastMap = new HashMap<String, WeatherDeviceForecast>();

    @Override
    public void onChildItemRead(String tagName, String key, String value, NamedNodeMap attributes) {
        if (key.startsWith("FC")) {
            parseForecast(key, value);
        }
    }

    public void readCONDITION(String value, NamedNodeMap attributes)  {
        this.condition = value;
        this.measured = attributes.getNamedItem("measured").getTextContent();
    }

    public void readHUMIDITY(String value)  {
        this.humidity = ValueDescriptionUtil.appendPercent(value);
    }

    public void readICON(String value)  {
        this.icon = value;
    }

    public void readTEMP_C(String value)  {
        this.temperature = ValueDescriptionUtil.appendTemperature(value);
    }

    public void readWIND_CONDITION(String value)  {
        this.wind = value.replaceAll("Wind: ", "").trim();
    }

    private void parseForecast(String keyValue, String nodeContent) {
        try {
            int underscorePosition = keyValue.indexOf("_");
            String name = keyValue.substring(underscorePosition + 1);

            String prefix = keyValue.substring(2, underscorePosition);

            if (! forecastMap.containsKey(prefix)) {
                Calendar forecastDate = Calendar.getInstance();

                Date measuredDate = parseDateFormat.parse(measured);
                forecastDate.setTime(measuredDate);
                forecastDate.add(Calendar.DAY_OF_YEAR, Integer.valueOf(prefix));
                String forecastTimeString = WeatherDeviceForecast.forecastDateFormat.format(forecastDate.getTime());

                forecastMap.put(prefix, new WeatherDeviceForecast(forecastTimeString));
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
        } catch (ParseException e) {
            Log.e(WeatherDevice.class.getName(), "cannot parse forecast", e);
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
        Collection<WeatherDeviceForecast> values = forecastMap.values();
        return new ArrayList<WeatherDeviceForecast>(values);
    }

    @Override
    protected void fillDeviceCharts(List<DeviceChart> chartSeries) {
        addDeviceChartIfNotNull(temperature, new DeviceChart(R.string.temperatureGraph, R.string.yAxisTemperature,
                ChartSeriesDescription.getRegressionValuesInstance(R.string.temperature, "4:temp_c:")));
        addDeviceChartIfNotNull(humidity, new DeviceChart(R.string.humidityGraph, R.string.yAxisHumidity,
                new ChartSeriesDescription(R.string.temperature, "4:humidity:0:")));
    }
}
