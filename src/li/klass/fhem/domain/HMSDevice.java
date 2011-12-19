package li.klass.fhem.domain;

import org.w3c.dom.NamedNodeMap;

public class HMSDevice extends Device<HMSDevice> {
    private String temperature = "";
    private String battery = "";

    @Override
    public void onChildItemRead(String keyValue, String nodeContent, NamedNodeMap attributes) {
        if (keyValue.equals("TEMPERATURE")) {
            temperature = nodeContent;
        } else if (keyValue.equals("BATTERY")) {
            battery = nodeContent;
        }
    }

    @Override
    public DeviceType getDeviceType() {
        return DeviceType.HMS;
    }

    public String getTemperature() {
        return temperature;
    }

    public String getBattery() {
        return battery;
    }
}
