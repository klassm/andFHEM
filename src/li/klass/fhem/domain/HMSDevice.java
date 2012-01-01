package li.klass.fhem.domain;

import li.klass.fhem.R;
import org.w3c.dom.NamedNodeMap;

import java.util.HashMap;
import java.util.Map;

public class HMSDevice extends Device<HMSDevice> {
    private String temperature;
    private String battery;
    private String humidity;
    
    public static final Integer COLUMN_SPEC_TEMPERATURE = R.string.temperature;
    public static final Integer COLUMN_SPEC_HUMIDITY = R.string.humidity;

    @Override
    public void onChildItemRead(String keyValue, String nodeContent, NamedNodeMap attributes) {
        if (keyValue.equals("TEMPERATURE")) {
            temperature = nodeContent;
        } else if (keyValue.equals("BATTERY")) {
            battery = nodeContent;
        } else if (keyValue.equals("HUMIDITY")) {
            humidity = nodeContent;
        }
    }

    public String getTemperature() {
        return temperature;
    }

    public String getBattery() {
        return battery;
    }

    public String getHumidity() {
        return humidity;
    }

    @Override
    public Map<Integer, String> getFileLogColumns() {
        Map<Integer, String> columnSpecification = new HashMap<Integer, String>();
        columnSpecification.put(COLUMN_SPEC_TEMPERATURE, "4:T\\x3a:0:");
        columnSpecification.put(COLUMN_SPEC_HUMIDITY, "6:H\\x3a:0:");

        return columnSpecification;
    }
}
