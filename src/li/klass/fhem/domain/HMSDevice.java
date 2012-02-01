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
import li.klass.fhem.util.ValueUtil;
import org.w3c.dom.NamedNodeMap;

import java.util.HashMap;
import java.util.Map;

public class HMSDevice extends Device<HMSDevice> {
    private String temperature;
    private String battery;
    private String humidity;
    
    public static final Integer COLUMN_SPEC_TEMPERATURE = R.string.temperature;
    public static final Integer COLUMN_SPEC_HUMIDITY = R.string.humidity;

    @Override
    public void onChildItemRead(String tagName, String keyValue, String nodeContent, NamedNodeMap attributes) {
        if (keyValue.equals("TEMPERATURE")) {
            temperature = ValueUtil.formatTemperature(nodeContent);
        } else if (keyValue.equals("BATTERY")) {
            battery = nodeContent;
        } else if (keyValue.equals("HUMIDITY")) {
            humidity = nodeContent;
        }
    }

    public String getTemperature() {
        return temperature;
    }

    public String getBattery() {
        return battery;
    }

    public String getHumidity() {
        return humidity;
    }

    @Override
    public Map<Integer, String> getFileLogColumns() {
        Map<Integer, String> columnSpecification = new HashMap<Integer, String>();
        columnSpecification.put(COLUMN_SPEC_TEMPERATURE, "4:T\\x3a:0:");
        columnSpecification.put(COLUMN_SPEC_HUMIDITY, "6:H\\x3a:0:");

        return columnSpecification;
    }
}
