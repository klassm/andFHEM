package li.klass.fhem.domain;

import org.w3c.dom.NamedNodeMap;

public class OwtempDevice extends Device<OwtempDevice> {
    private String temperature;
    private String warnings;

    @Override
    public void onChildItemRead(String keyValue, String nodeContent, NamedNodeMap attributes) {
        if (keyValue.equals("TEMPERATURE")) {
            this.temperature = nodeContent;
        } else if (keyValue.equals("WARNINGS")) {
            this.warnings = nodeContent;
            measured = attributes.getNamedItem("measured").getTextContent();
        }
    }

    @Override
    public DeviceType getDeviceType() {
        return DeviceType.OWTEMP;
    }

    public String getTemperature() {
        return temperature;
    }

    public String getWarnings() {
        return warnings;
    }
}
