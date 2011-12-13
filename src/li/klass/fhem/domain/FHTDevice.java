package li.klass.fhem.domain;

import java.io.Serializable;

public class FHTDevice extends Device<FHTDevice> implements Serializable {

    private String actuator;
    private String temperature = "???";

    @Override
    public void onChildItemRead(String keyValue, String nodeContent) {
        System.out.println(keyValue);
        if (keyValue.startsWith("ACTUATOR")) {
            actuator = nodeContent;
        } else if (keyValue.equalsIgnoreCase("measured-temp")) {
            temperature = nodeContent;
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
}
