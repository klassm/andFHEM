package li.klass.fhem.domain;

import li.klass.fhem.R;
import org.w3c.dom.NamedNodeMap;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class KS300Device extends Device<KS300Device> implements Serializable {

    public static final int COLUMN_SPEC_TEMPERATURE = R.string.temperature;
    public static final int COLUMN_SPEC_HUMIDITY = R.string.humidity;
    public static final int COLUMN_SPEC_WIND = R.string.wind;
    public static final int COLUMN_SPEC_RAIN = R.string.rain;

    private String temperature = "";
    private String wind = "";
    private String humidity = "";
    private String rain = "";
    private String averageDay = "";
    private String averageMonth = "";
    private String isRaining = "";

    @Override
    public int compareTo(KS300Device ks300Device) {
        return getName().compareTo(ks300Device.getName());
    }

    @Override
    public void onChildItemRead(String keyValue, String nodeContent, NamedNodeMap attributes) {
        if (keyValue.equals("TEMPERATURE")) {
            this.temperature = nodeContent;
        } else if (keyValue.equals("WIND")) {
            this.wind = nodeContent;
        } else if (keyValue.equals("HUMIDITY")) {
            this.humidity = nodeContent;
        } else if (keyValue.equals("RAIN")) {
            this.rain = nodeContent;
        } else if (keyValue.equals("AVG_DAY")) {
            this.averageDay = nodeContent;
        } else if (keyValue.equals("AVG_MONTH")) {
            this.averageMonth = nodeContent;
        } else if (keyValue.equals("ISRAINING")) {
            this.isRaining = nodeContent;
        }
    }

    public String getTemperature() {
        return temperature;
    }

    public String getWind() {
        return wind;
    }

    public String getHumidity() {
        return humidity;
    }

    public String getRain() {
        return rain;
    }

    public String getAverageDay() {
        return averageDay;
    }

    public String getAverageMonth() {
        return averageMonth;
    }

    public String getRaining() {
        return isRaining;
    }

    @Override
    public String toString() {
        return "KS300Device{" +
                "temperature='" + temperature + '\'' +
                ", wind='" + wind + '\'' +
                ", humidity='" + humidity + '\'' +
                ", rain='" + rain + '\'' +
                "} " + super.toString();
    }

    @Override
    public DeviceType getDeviceType() {
        return DeviceType.KS300;
    }

    @Override
    public Map<Integer, String> getFileLogColumns() {
        Map<Integer, String> columnSpecification = new HashMap<Integer, String>();
        columnSpecification.put(COLUMN_SPEC_TEMPERATURE, "4:IR\\x3a:0:");
        columnSpecification.put(COLUMN_SPEC_HUMIDITY, "6:IR:");
        columnSpecification.put(COLUMN_SPEC_WIND, "8:IR:");
        columnSpecification.put(COLUMN_SPEC_RAIN, "10:IR:");

        return columnSpecification;
    }
}
