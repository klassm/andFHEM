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
import org.w3c.dom.NamedNodeMap;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class KS300Device extends Device<KS300Device> implements Serializable {

    public static final int COLUMN_SPEC_TEMPERATURE = R.string.temperature;
    public static final int COLUMN_SPEC_HUMIDITY = R.string.humidity;
    public static final int COLUMN_SPEC_WIND = R.string.wind;
    public static final int COLUMN_SPEC_RAIN = R.string.rain;

    private String temperature;
    private String wind;
    private String humidity;
    private String rain;
    private String averageDay;
    private String averageMonth;
    private String isRaining;

    @Override
    public int compareTo(KS300Device ks300Device) {
        return getName().compareTo(ks300Device.getName());
    }

    @Override
    public void onChildItemRead(String tagName, String keyValue, String nodeContent, NamedNodeMap attributes) {
        if (keyValue.equals("TEMPERATURE")) {
            this.temperature = nodeContent;
        } else if (keyValue.equals("WIND")) {
            this.wind = nodeContent;
        } else if (keyValue.equals("HUMIDITY")) {
            this.humidity = nodeContent;
        } else if (keyValue.equals("RAIN")) {
            this.rain = nodeContent;
        } else if (keyValue.equals("AVG_DAY")) {
            this.averageDay = nodeContent;
        } else if (keyValue.equals("AVG_MONTH")) {
            this.averageMonth = nodeContent;
        } else if (keyValue.equals("ISRAINING")) {
            this.isRaining = nodeContent;
        }
    }

    public String getTemperature() {
        return temperature;
    }

    public String getWind() {
        return wind;
    }

    public String getHumidity() {
        return humidity;
    }

    public String getRain() {
        return rain;
    }

    public String getAverageDay() {
        return averageDay;
    }

    public String getAverageMonth() {
        return averageMonth;
    }

    public String getRaining() {
        return isRaining;
    }

    @Override
    public String toString() {
        return "KS300Device{" +
                "temperature='" + temperature + '\'' +
                ", wind='" + wind + '\'' +
                ", humidity='" + humidity + '\'' +
                ", rain='" + rain + '\'' +
                "} " + super.toString();
    }

    @Override
    public Map<Integer, String> getFileLogColumns() {
        Map<Integer, String> columnSpecification = new HashMap<Integer, String>();
        columnSpecification.put(COLUMN_SPEC_TEMPERATURE, "4:IR\\x3a:0:");
        columnSpecification.put(COLUMN_SPEC_HUMIDITY, "6:IR:");
        columnSpecification.put(COLUMN_SPEC_WIND, "8:IR:");
        columnSpecification.put(COLUMN_SPEC_RAIN, "10:IR:");

        return columnSpecification;
    }
}
