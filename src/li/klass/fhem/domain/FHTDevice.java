package li.klass.fhem.domain;

import li.klass.fhem.R;
import li.klass.fhem.util.ValueDescriptionUtil;
import li.klass.fhem.util.ValueExtractUtil;
import org.w3c.dom.NamedNodeMap;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class FHTDevice extends Device<FHTDevice> implements Serializable {



    public enum FHTMode {
        AUTO, MANUAL, HOLIDAY, HOLIDAY_SHORT
    }
    private String actuator;
    private FHTMode mode;
    private double desiredTemp;
    private String warnings;
    private String temperature;

    public static final Integer COLUMN_SPEC_TEMPERATURE = R.string.temperature;
    public static final Integer COLUMN_SPEC_ACTUATOR = R.string.actuator;

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
        return desiredTemperatureToString(desiredTemp);
    }

    public double getDesiredTemp() {
        return desiredTemp;
    }

    public String getWarningsDesc() {
        return warnings;
    }

    public FHTMode getMode() {
        return mode;
    }

    public void setMode(FHTMode mode) {
        this.mode = mode;
    }

    @Override
    public Map<Integer, String> getFileLogColumns() {
        Map<Integer, String> columnSpecification = new HashMap<Integer, String>();
        columnSpecification.put(COLUMN_SPEC_TEMPERATURE, "4:measured:0:");
        columnSpecification.put(COLUMN_SPEC_ACTUATOR, "4:actuator.*[0-9]+%:0:int");

        return columnSpecification;
    }
    
    public static String desiredTemperatureToString(double temperature) {
        if (temperature == 5.5) {
            return "off";
        } else if (temperature == 30.5) {
            return "on";
        } else {
            return ValueDescriptionUtil.appendTemperature(temperature);
        }
    }
}
