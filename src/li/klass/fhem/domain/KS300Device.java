package li.klass.fhem.domain;

import android.util.Log;
import org.w3c.dom.NamedNodeMap;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class KS300Device extends Device<KS300Device> implements Serializable {

    private String temperature = "";
    private String wind = "";
    private String humidity = "";
    private String rain = "";

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
            Log.e(KS300Device.class.getName(), keyValue + " - " + nodeContent + " !!! set wind for device " + name + " ; " + nodeContent + " : " + getWind());
        } else if (keyValue.equals("HUMIDITY")) {
            this.humidity = nodeContent;
        } else if (keyValue.equals("RAIN")) {
            this.rain = nodeContent;
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
    public Map<String, String> getFileLogColumns() {
        Map<String, String> columnSpecification = new HashMap<String, String>();
        columnSpecification.put("temperature", "4:IR:");

        return columnSpecification;
    }
}
