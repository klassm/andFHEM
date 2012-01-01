package li.klass.fhem.domain;

import li.klass.fhem.R;
import org.w3c.dom.NamedNodeMap;

import java.util.HashMap;
import java.util.Map;

public class USBWXDevice extends Device<USBWXDevice> {
    
    private String humidity;
    private String temperature;
    private String dewpoint;

    public static final Integer COLUMN_SPEC_TEMPERATURE = R.string.temperature;
    public static final Integer COLUMN_SPEC_HUMIDITY = R.string.humidity;

    
    @Override
    protected void onChildItemRead(String keyValue, String nodeContent, NamedNodeMap attributes) {
        if (keyValue.equals("TEMPERATURE")) {
            this.temperature = nodeContent + " (Celsius)";
        } else if (keyValue.equals("HUMIDITY")) {
            this.humidity = nodeContent + " (%)";
        } else if (keyValue.equals("DEWPOINT")) {
            this.dewpoint = nodeContent + " (Celsius)";
        } else if (keyValue.equals("TIME")) {
            measured = nodeContent;
        }
    }

    @Override
    public DeviceType getDeviceType() {
        return DeviceType.USBWX;
    }

    public String getHumidity() {
        return humidity;
    }

    public String getTemperature() {
        return temperature;
    }

    public String getDewpoint() {
        return dewpoint;
    }

    @Override
    public Map<Integer, String> getFileLogColumns() {
        Map<Integer, String> columnSpecification = new HashMap<Integer, String>();
        columnSpecification.put(COLUMN_SPEC_TEMPERATURE, "4:temperature:0:");
        columnSpecification.put(COLUMN_SPEC_HUMIDITY, "4:humidity:0:");

        return columnSpecification;
    }
}
