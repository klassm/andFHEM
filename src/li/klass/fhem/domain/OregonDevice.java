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

import li.klass.fhem.R;
import li.klass.fhem.util.ValueDescriptionUtil;
import org.w3c.dom.NamedNodeMap;

import java.util.HashMap;
import java.util.Map;

public class OregonDevice extends Device<OregonDevice> {
    
    private String humidity;
    private String temperature;
    private String forecast;
    private String dewpoint;
    private String pressure;
    private String battery;
    private String rainRate;
    private String rainTotal;
    private String windAvgSpeed;
    private String windDirection;
    private String windSpeed;
    private String uvValue;
    private String uvRisk;

    public static final Integer COLUMN_SPEC_TEMPERATURE = R.string.temperature;
    public static final Integer COLUMN_SPEC_HUMIDITY = R.string.humidity;
    public static final Integer COLUMN_SPEC_PRESSURE = R.string.pressure;
    public static final Integer COLUMN_SPEC_RAIN_RATE = R.string.rainRate;
    public static final Integer COLUMN_SPEC_RAIN_TOTAL = R.string.rainTotal;
    public static final Integer COLUMN_SPEC_WIND_SPEED = R.string.windSpeed;

    @Override
    protected void onChildItemRead(String tagName, String keyValue, String nodeContent, NamedNodeMap attributes) {
        if (keyValue.equals("TEMPERATURE")) {
            this.temperature = ValueDescriptionUtil.appendTemperature(nodeContent);
        } else if (keyValue.equals("HUMIDITY")) {
            this.humidity = ValueDescriptionUtil.appendPercent(nodeContent);
        } else if (keyValue.equals("FORECAST")) {
            this.forecast = nodeContent;
        } else if (keyValue.equals("DEWPOINT")) {
            this.dewpoint = nodeContent + " (Celsius)";
        } else if (keyValue.equals("PRESSURE")) {
            this.pressure = nodeContent + " (hPa)";
        } else if (keyValue.equals("BATTERY")) {
            this.battery = nodeContent + " (%)";
        } else if (keyValue.equals("RAIN_RATE")) {
            this.rainRate = nodeContent + " (mm/hr)";
        } else if (keyValue.equals("RAIN_TOTAL")) {
            this.rainTotal = nodeContent + " (l/m2)";
        } else if (keyValue.equals("WIND_AVSPEED")) {
            this.windAvgSpeed = nodeContent + " (km/h)";
        } else if (keyValue.equals("WIND_DIR")) {
            this.windDirection = nodeContent;
        } else if (keyValue.equals("WIND_SPEED")) {
            this.windSpeed = nodeContent + " (km/h)";
        } else if (keyValue.equals("UV_VAL")) {
            this.uvValue = nodeContent;
        } else if (keyValue.equals("UV_RISK")) {
            this.uvRisk = nodeContent;
        } else if (keyValue.equals("TIME")) {
            this.measured = nodeContent;
        }
    }

    public String getHumidity() {
        return humidity;
    }

    public String getTemperature() {
        return temperature;
    }

    public String getForecast() {
        return forecast;
    }

    public String getDewpoint() {
        return dewpoint;
    }

    public String getPressure() {
        return pressure;
    }

    public String getBattery() {
        return battery;
    }

    public String getRainRate() {
        return rainRate;
    }

    public String getRainTotal() {
        return rainTotal;
    }

    public String getWindAvgSpeed() {
        return windAvgSpeed;
    }

    public String getWindDirection() {
        return windDirection;
    }

    public String getWindSpeed() {
        return windSpeed;
    }

    public String getUvValue() {
        return uvValue;
    }

    public String getUvRisk() {
        return uvRisk;
    }

    @Override
    public Map<Integer, String> getFileLogColumns() {
        Map<Integer, String> columnSpecification = new HashMap<Integer, String>();
        columnSpecification.put(COLUMN_SPEC_TEMPERATURE, "4:temperature:0:");
        columnSpecification.put(COLUMN_SPEC_HUMIDITY, "4:humidity:0:");
        columnSpecification.put(COLUMN_SPEC_PRESSURE, "4:pressure:0:");
        columnSpecification.put(COLUMN_SPEC_RAIN_RATE, "4:rain_rate:0:");
        columnSpecification.put(COLUMN_SPEC_RAIN_TOTAL, "4:rain_total:0:");
        columnSpecification.put(COLUMN_SPEC_WIND_SPEED, "4:wind_speed:0:");

        return columnSpecification;
    }
}
