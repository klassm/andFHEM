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

import android.content.Context;
import android.util.Log;

import org.jetbrains.annotations.NotNull;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

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
import li.klass.fhem.domain.core.DeviceChart;
import li.klass.fhem.domain.core.DeviceFunctionality;
import li.klass.fhem.domain.core.FhemDevice;
import li.klass.fhem.domain.core.XmllistAttribute;
import li.klass.fhem.service.graph.description.ChartSeriesDescription;
import li.klass.fhem.service.room.xmllist.DeviceNode;
import li.klass.fhem.util.ValueDescriptionUtil;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.newTreeSet;
import static li.klass.fhem.service.graph.description.SeriesType.HUMIDITY;
import static li.klass.fhem.service.graph.description.SeriesType.TEMPERATURE;

@SupportsWidget({TemperatureWidgetView.class, MediumInformationWidgetView.class})
public class WeatherDevice extends FhemDevice<WeatherDevice> {
    public static final String IMAGE_URL_PREFIX = "http://andfhem.klass.li/images/weatherIcons/";
    private static final DateTimeFormatter PARSE_DATE_FORMAT = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");

    @WidgetMediumLine3
    private String condition;

    @WidgetTemperatureAdditionalField
    @WidgetMediumLine2
    @XmllistAttribute("humidity")
    private String humidity;

    @XmllistAttribute("icon")
    private String icon;

    @WidgetTemperatureField
    @WidgetMediumLine1
    @XmllistAttribute("temp_c")
    private String temperature;

    @XmllistAttribute("wind_condition")
    private String wind;

    @XmllistAttribute("visibility")
    private String visibilityConditions;

    @XmllistAttribute("wind_chill")
    private String windChill;

    private Map<Integer, WeatherDeviceForecast> forecastMap = newHashMap();

    @Override
    public void onChildItemRead(DeviceNode.DeviceNodeType type, String key, String value, DeviceNode node) {
        if (key.startsWith("fc")) {
            parseForecast(key, value, node.getMeasured());
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

    @XmllistAttribute("condition")
    public void setCondition(String value, DeviceNode node) {
        this.condition = value;
        setMeasured(node.getMeasured());
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

    public String getWindChill() {
        return windChill;
    }

    public String getVisibilityConditions() {
        return visibilityConditions;
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
    protected void fillDeviceCharts(List<DeviceChart> chartSeries, Context context) {
        super.fillDeviceCharts(chartSeries, context);

        addDeviceChartIfNotNull(new DeviceChart(R.string.temperatureHumidityGraph,
                new ChartSeriesDescription.Builder()
                        .withColumnName(R.string.temperature, context)
                        .withFileLogSpec("4:temperature:")
                        .withDbLogSpec("temperature::int1")
                        .withSeriesType(TEMPERATURE)
                        .withShowRegression(true)
                        .build(),
                new ChartSeriesDescription.Builder()
                        .withColumnName(R.string.humidity, context).withFileLogSpec("4:humidity:0:")
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
