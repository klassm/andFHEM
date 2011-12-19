package li.klass.fhem.domain;

import org.w3c.dom.NamedNodeMap;

public class CULWSDevice extends Device<CULWSDevice> {
    
    private String humidity;
    private String temperature;

    @Override
    public void onChildItemRead(String keyValue, String nodeContent, NamedNodeMap attributes) {
        if (keyValue.equals("HUMIDITY")) {
            humidity = nodeContent + " (%)";
        } else if (keyValue.equals("TEMPERATURE")) {
            temperature = nodeContent + " (° Celsius)";
        }
    }

    @Override
    public DeviceType getDeviceType() {
        return DeviceType.CUL_WS;
    }

    public String getHumidity() {
        return humidity;
    }

    public String getTemperature() {
        return temperature;
    }
}
