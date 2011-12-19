package li.klass.fhem.domain;

import org.w3c.dom.NamedNodeMap;

import java.io.Serializable;

public class FHTDevice extends Device<FHTDevice> implements Serializable {

    private String actuator;
    private String temperature = "???";

    @Override
    public void onChildItemRead(String keyValue, String nodeContent, NamedNodeMap nodeAttributes) {
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

    @Override
    public DeviceType getDeviceType() {
        return DeviceType.FHT;
    }

    public String getTemperature() {
        return temperature;
    }
}
