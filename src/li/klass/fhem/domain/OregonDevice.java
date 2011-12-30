package li.klass.fhem.domain;

import li.klass.fhem.R;
import org.w3c.dom.NamedNodeMap;

import java.util.HashMap;
import java.util.Map;

public class OregonDevice extends Device<OregonDevice> {
    
    private String humidity;
    private String temperature;
    private String forecast;
    private String dewpoint;
    private String pressure;
    private String battery;

    public static final Integer COLUMN_SPEC_TEMPERATURE = R.string.temperature;
    public static final Integer COLUMN_SPEC_HUMIDITY = R.string.humidity;
    public static final Integer COLUMN_SPEC_PRESSURE = R.string.pressure;

    
    @Override
    protected void onChildItemRead(String keyValue, String nodeContent, NamedNodeMap attributes) {
        if (keyValue.equals("TEMPERATURE")) {
            this.temperature = nodeContent + " (Celsius)";
        } else if (keyValue.equals("HUMIDITY")) {
            this.humidity = nodeContent + " (%)";
        } else if (keyValue.equals("FORECAST")) {
            this.forecast = nodeContent;
        } else if (keyValue.equals("DEWPOINT")) {
            this.dewpoint = nodeContent + " (Celsius)";
        } else if (keyValue.equals("PRESSURE")) {
            this.pressure = nodeContent + " (hPa)";
        } else if (keyValue.equals("BATTERY")) {
            this.battery = nodeContent;
        }
    }

    @Override
    public DeviceType getDeviceType() {
        return DeviceType.OREGON;
    }

    public String getHumidity() {
        return humidity;
    }

    public String getTemperature() {
        return temperature;
    }

    public String getForecast() {
        return forecast;
    }

    public String getDewpoint() {
        return dewpoint;
    }

    public String getPressure() {
        return pressure;
    }

    public String getBattery() {
        return battery;
    }

    @Override
    public Map<Integer, String> getFileLogColumns() {
        Map<Integer, String> columnSpecification = new HashMap<Integer, String>();
        columnSpecification.put(COLUMN_SPEC_TEMPERATURE, "4:temperature:0:");
        columnSpecification.put(COLUMN_SPEC_HUMIDITY, "4:humidity:0:");
        columnSpecification.put(COLUMN_SPEC_PRESSURE, "4:pressure:0:");

        return columnSpecification;
    }
}
