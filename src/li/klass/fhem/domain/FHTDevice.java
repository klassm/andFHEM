package li.klass.fhem.domain;

import li.klass.fhem.R;
import li.klass.fhem.domain.fht.FHTDayControl;
import li.klass.fhem.domain.fht.FHTMode;
import li.klass.fhem.util.DayUtil;
import li.klass.fhem.util.ValueDescriptionUtil;
import li.klass.fhem.util.ValueExtractUtil;
import org.w3c.dom.NamedNodeMap;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class FHTDevice extends Device<FHTDevice> implements Serializable {
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
            temperature = nodeContent;
        } else if (keyValue.equals("DESIRED-TEMP")) {
            desiredTemp = ValueExtractUtil.extractLeadingDouble(nodeContent);
        } else if (keyValue.equals("WARNINGS")) {
            warnings = nodeContent;
        } else if (keyValue.equals("MODE")) {
            this.mode = FHTMode.valueOf(nodeContent.toUpperCase());
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
