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

import android.util.Log;

import org.jetbrains.annotations.NotNull;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.w3c.dom.NamedNodeMap;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import li.klass.fhem.R;
import li.klass.fhem.appwidget.annotation.SupportsWidget;
import li.klass.fhem.appwidget.annotation.WidgetMediumLine1;
import li.klass.fhem.appwidget.annotation.WidgetMediumLine2;
import li.klass.fhem.appwidget.annotation.WidgetMediumLine3;
import li.klass.fhem.appwidget.annotation.WidgetTemperatureAdditionalField;
import li.klass.fhem.appwidget.annotation.WidgetTemperatureField;
import li.klass.fhem.appwidget.view.widget.medium.MediumInformationWidgetView;
import li.klass.fhem.appwidget.view.widget.medium.TemperatureWidgetView;
import li.klass.fhem.domain.core.Device;
import li.klass.fhem.domain.core.DeviceChart;
import li.klass.fhem.domain.core.DeviceFunctionality;
import li.klass.fhem.service.graph.description.ChartSeriesDescription;
import li.klass.fhem.util.ValueDescriptionUtil;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.newTreeSet;
import static li.klass.fhem.service.graph.description.SeriesType.HUMIDITY;
import static li.klass.fhem.service.graph.description.SeriesType.TEMPERATURE;

@SupportsWidget({TemperatureWidgetView.class, MediumInformationWidgetView.class})
@SuppressWarnings("unused")
public class WeatherDevice extends Device<WeatherDevice> {
    public static final String IMAGE_URL_PREFIX = "http://andfhem.klass.li/images/weatherIcons/";
    private static final DateTimeFormatter PARSE_DATE_FORMAT = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");

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

    private Map<Integer, WeatherDeviceForecast> forecastMap = newHashMap();

    @Override
    public void onChildItemRead(String tagName, String key, String value, NamedNodeMap attributes) {
        if (key.startsWith("FC")) {
            parseForecast(key, value, attributes.getNamedItem("measured").getNodeValue());
        }
    }

    private void parseForecast(String keyValue, String nodeContent, String measured) {
        try {
            int underscorePosition = keyValue.indexOf("_");
            String name = keyValue.substring(underscorePosition + 1);

            int prefix = Integer.valueOf(keyValue.substring(2, underscorePosition));
            if (((Integer) 1).equals(prefix)) return;

            if (!forecastMap.containsKey(prefix)) {
                DateTime measuredDate = PARSE_DATE_FORMAT.parseDateTime(measured);
                DateTime forecastDate = measuredDate.plusDays(prefix - 1);
                String forecastTimeString = WeatherDeviceForecast.FORECAST_DATE_FORMAT.print(forecastDate);

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
        } catch (Exception e) {
            Log.e(WeatherDevice.class.getName(), "cannot parse forecast", e);
        }
    }

    public void readCONDITION(String value, NamedNodeMap attributes) {
        this.condition = value;
        String measured = attributes.getNamedItem("measured").getNodeValue();
        setMeasured(measured);
    }

    public void readHUMIDITY(String value) {
        this.humidity = ValueDescriptionUtil.appendPercent(value);
    }

    public void readICON(String value) {
        this.icon = value;
    }

    public void readTEMP_C(String value) {
        this.temperature = ValueDescriptionUtil.appendTemperature(value);
    }

    public void readWIND_CONDITION(String value) {
        this.wind = value.replaceAll("Wind: ", "").trim();
    }

    public String getCondition() {
        return condition;
    }

    public String getHumidity() {
        return humidity;
    }

    public String getIcon() {
        return parseIcon(icon);
    }

    public static String parseIcon(String icon) {
        if (icon == null) return null;
        if (!icon.endsWith(".png") && icon.lastIndexOf(".") != -1) {
            icon = icon.substring(0, icon.lastIndexOf(".")) + ".png";
        }

        if (!icon.contains("/")) return icon;

        int lastSlashIndex = icon.lastIndexOf("/");
        return icon.substring(lastSlashIndex + 1);
    }

    public String getTemperature() {
        return temperature;
    }

    public String getWind() {
        return wind;
    }

    public List<WeatherDeviceForecast> getForecasts() {
        TreeSet<Integer> keys = newTreeSet();
        keys.addAll(forecastMap.keySet());

        ArrayList<WeatherDeviceForecast> result = newArrayList();
        for (Integer key : keys) {
            result.add(forecastMap.get(key));
        }
        return result;
    }

    @Override
    protected void fillDeviceCharts(List<DeviceChart> chartSeries) {
        super.fillDeviceCharts(chartSeries);

        addDeviceChartIfNotNull(new DeviceChart(R.string.temperatureHumidityGraph,
                new ChartSeriesDescription.Builder()
                        .withColumnName(R.string.temperature)
                        .withFileLogSpec("4:temperature:")
                        .withDbLogSpec("temperature::int1")
                        .withSeriesType(TEMPERATURE)
                        .withShowRegression(true)
                        .withYAxisMinMaxValue(getLogDevices().get(0).getYAxisMinMaxValueFor("temperature", 0, 30))
                        .build(),
                new ChartSeriesDescription.Builder()
                        .withColumnName(R.string.humidity).withFileLogSpec("4:humidity:0:")
                        .withDbLogSpec("humidity::int")
                        .withSeriesType(HUMIDITY)
                        .withYAxisMinMaxValue(getLogDevices().get(0).getYAxisMinMaxValueFor("humidity", 0, 100))
                        .build()
        ), temperature, humidity);
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

    public static class WeatherDeviceForecast implements Comparable<WeatherDeviceForecast>, Serializable {
        private static final DateTimeFormatter FORECAST_DATE_FORMAT = DateTimeFormat.forPattern("yyyy-MM-dd");

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
        public int compareTo(@NotNull WeatherDeviceForecast weatherDeviceForecast) {
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

        public String getUrl() {
            return WeatherDevice.IMAGE_URL_PREFIX + getIcon() + ".png";
        }

        public String getIcon() {
            return parseIcon(icon);
        }
    }
}
