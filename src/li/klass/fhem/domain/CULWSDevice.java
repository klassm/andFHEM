package li.klass.fhem.domain;

import li.klass.fhem.R;
import org.w3c.dom.NamedNodeMap;

import java.util.HashMap;
import java.util.Map;

public class CULWSDevice extends Device<CULWSDevice> {
    
    private String humidity;
    private String temperature;

    public static final Integer COLUMN_SPEC_TEMPERATURE = R.string.temperature;

    @Override
    public void onChildItemRead(String keyValue, String nodeContent, NamedNodeMap attributes) {
        if (keyValue.equals("HUMIDITY")) {
            humidity = nodeContent + " (%)";
        } else if (keyValue.equals("COLUMN_SPEC_TEMPERATURE")) {
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

    @Override
    public Map<Integer, String> getFileLogColumns() {
        Map<Integer, String> columnSpecification = new HashMap<Integer, String>();
        columnSpecification.put(COLUMN_SPEC_TEMPERATURE, "4::0:");

        return columnSpecification;
    }
}
