package li.klass.fhem.domain;

import li.klass.fhem.R;
import org.w3c.dom.NamedNodeMap;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class FHTDevice extends Device<FHTDevice> implements Serializable {

    private String actuator;
    private String desiredTemp;
    private String warnings;
    private String temperature;

    public static final Integer COLUMN_SPEC_TEMPERATURE = R.string.temperature;
    public static final Integer COLUMN_SPEC_ACTUATOR = R.string.actuator;

    @Override
    public void onChildItemRead(String keyValue, String nodeContent, NamedNodeMap nodeAttributes) {
        if (keyValue.startsWith("ACTUATOR") && ! nodeContent.equalsIgnoreCase("pair")) {
            actuator = nodeContent;
        } else if (keyValue.equalsIgnoreCase("MEASURED-TEMP")) {
            temperature = nodeContent;
        } else if (keyValue.equals("DESIRED-TEMP")) {
            desiredTemp = nodeContent;
        } else if (keyValue.equals("WARNINGS")) {
            warnings = nodeContent;
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

    public String getDesiredTemp() {
        return desiredTemp;
    }

    public String getWarnings() {
        return warnings;
    }

    @Override
    public Map<Integer, String> getFileLogColumns() {
        Map<Integer, String> columnSpecification = new HashMap<Integer, String>();
        columnSpecification.put(COLUMN_SPEC_TEMPERATURE, "4:measured:0:");
        columnSpecification.put(COLUMN_SPEC_ACTUATOR, "4:actuator.*[0-9]+%:0:int");

        return columnSpecification;
    }
}
