package li.klass.fhem.domain;

import android.util.Log;
import org.w3c.dom.Node;

import java.io.Serializable;

public class KS300Device extends Device implements Comparable<KS300Device>, Serializable {

    private String temperature = "";
    private String wind = "";
    private String humidity = "";
    private String rain = "";

    @Override
    public int compareTo(KS300Device ks300Device) {
        return getName().compareTo(ks300Device.getName());
    }

    @Override
    public void onChildItemRead(String keyValue, String nodeContent) {
        Log.e(KS300Device.class.getName(), keyValue + " - " + nodeContent);
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
}
