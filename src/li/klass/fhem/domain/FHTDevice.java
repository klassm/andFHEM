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
import li.klass.fhem.domain.fht.FHTDayControl;
import li.klass.fhem.domain.fht.FHTMode;
import li.klass.fhem.util.DayUtil;
import li.klass.fhem.util.ValueDescriptionUtil;
import li.klass.fhem.util.ValueExtractUtil;
import li.klass.fhem.util.ValueUtil;
import org.w3c.dom.NamedNodeMap;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class FHTDevice extends Device<FHTDevice> {
    private String actuator;
    private FHTMode mode;
    private double desiredTemp;
    private double dayTemperature;
    private double nightTemperature;
    private double windowOpenTemp;
    private String warnings;
    private String temperature;

    private Map<Integer, FHTDayControl> dayControlMap = new HashMap<Integer, FHTDayControl>();

    public static final Integer COLUMN_SPEC_TEMPERATURE = R.string.temperature;
    public static final Integer COLUMN_SPEC_DESIRED_TEMPERATURE = R.string.desiredTemperature;
    public static final Integer COLUMN_SPEC_ACTUATOR = R.string.actuator;

    public FHTDevice() {
        for (Integer dayId : DayUtil.getSortedDayStringIdList()) {
            dayControlMap.put(dayId, new FHTDayControl(dayId));
        }
    }

    @Override
    public void onChildItemRead(String tagName, String keyValue, String nodeContent, NamedNodeMap nodeAttributes) {
        if (keyValue.startsWith("ACTUATOR") && ! nodeContent.equalsIgnoreCase("pair")) {
            actuator = nodeContent;
        } else if (keyValue.equalsIgnoreCase("MEASURED-TEMP")) {
            temperature = ValueUtil.formatTemperature(nodeContent);
        } else if (keyValue.equals("DESIRED-TEMP")) {
            desiredTemp = ValueExtractUtil.extractLeadingDouble(nodeContent);
        } else if (keyValue.equals("WARNINGS")) {
            warnings = nodeContent;
        } else if (keyValue.equals("MODE")) {
            try {
                this.mode = FHTMode.valueOf(nodeContent.toUpperCase());
            } catch (IllegalArgumentException e) {
                this.mode = FHTMode.UNKNOWN;
            }
        } else if (keyValue.equals("DAY-TEMP")) {
            dayTemperature = ValueExtractUtil.extractLeadingDouble(nodeContent);
        } else if (keyValue.equals("NIGHT-TEMP")) {
            nightTemperature = ValueExtractUtil.extractLeadingDouble(nodeContent);
        } else if (keyValue.equals("WINDOWOPEN-TEMP")) {
            windowOpenTemp = ValueExtractUtil.extractLeadingDouble(nodeContent);
        }else if (keyValue.endsWith("FROM1") || keyValue.endsWith("FROM2") || keyValue.endsWith("TO1") || keyValue.endsWith("TO2")) {
            String shortName = keyValue.substring(0, 3);
            FHTDayControl dayControl = dayControlMap.get(DayUtil.getDayStringIdForShortName(shortName));

            if (keyValue.endsWith("FROM1")) dayControl.setFrom1(nodeContent);
            if (keyValue.endsWith("FROM2")) dayControl.setFrom2(nodeContent);
            if (keyValue.endsWith("TO1")) dayControl.setTo1(nodeContent);
            if (keyValue.endsWith("TO2")) dayControl.setTo2(nodeContent);
        }
    }

    public String getActuator() {
        return actuator;
    }

    @Override
    public String toString() {
        return "FHTDevice{" +
                "actuator='" + actuator + '\'' +
                "} " + super.toString();
    }

    public String getTemperature() {
        return temperature;
    }

    public void setDesiredTemp(double desiredTemp) {
        this.desiredTemp = desiredTemp;
    }

    public String getDesiredTempDesc() {
        return temperatureToString(desiredTemp);
    }

    public double getDesiredTemp() {
        return desiredTemp;
    }

    public String getDayTemperatureDesc() {
        return temperatureToString(dayTemperature);
    }

    public double getDayTemperature() {
        return dayTemperature;
    }

    public void setDayTemperature(double dayTemperature) {
        this.dayTemperature = dayTemperature;
    }

    public String getNightTemperatureDesc() {
        return temperatureToString(nightTemperature);
    }

    public double getNightTemperature() {
        return nightTemperature;
    }

    public void setNightTemperature(double nightTemperature) {
        this.nightTemperature = nightTemperature;
    }

    public String getWindowOpenTempDesc() {
        return temperatureToString(windowOpenTemp);
    }

    public double getWindowOpenTemp() {
        return windowOpenTemp;
    }

    public void setWindowOpenTemp(double windowOpenTemp) {
        this.windowOpenTemp = windowOpenTemp;
    }

    public String getWarnings() {
        return warnings;
    }

    public FHTMode getMode() {
        return mode;
    }

    public void setMode(FHTMode mode) {
        this.mode = mode;
    }

    public Map<Integer, FHTDayControl> getDayControlMap() {
        return Collections.unmodifiableMap(dayControlMap);
    }

    @Override
    public Map<Integer, String> getFileLogColumns() {
        Map<Integer, String> columnSpecification = new HashMap<Integer, String>();
        columnSpecification.put(COLUMN_SPEC_TEMPERATURE, "4:measured:0:");
        columnSpecification.put(COLUMN_SPEC_DESIRED_TEMPERATURE, "4:desired-temp:0:");
        columnSpecification.put(COLUMN_SPEC_ACTUATOR, "4:actuator.*[0-9]+%:0:int");

        return columnSpecification;
    }

    public static String temperatureToString(double temperature) {
        if (temperature == 5.5) {
            return "off";
        } else if (temperature == 30.5) {
            return "on";
        } else {
            return ValueDescriptionUtil.appendTemperature(temperature);
        }
    }

    public boolean hasChangedDayControlMapValues() {
        for (FHTDayControl fhtDayControl : dayControlMap.values()) {
            if (fhtDayControl.hasChangedValues()) {
                return true;
            }
        }
        return false;
    }

    public void resetDayControlMapValues() {
        for (FHTDayControl fhtDayControl : dayControlMap.values()) {
            fhtDayControl.reset();
        }
    }

    public void setChangedDayControlMapValuesAsCurrent() {
        for (FHTDayControl fhtDayControl : dayControlMap.values()) {
            fhtDayControl.setChangedAsCurrent();
        }
    }
}
