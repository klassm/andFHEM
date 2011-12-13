package li.klass.fhem.domain;

public class HMSDevice extends Device<HMSDevice> {
    private String temperature = "";
    private String battery = "";

    @Override
    public void onChildItemRead(String keyValue, String nodeContent) {
        if (keyValue.equals("TEMPERATURE")) {
            temperature = nodeContent;
        } else if (keyValue.equals("BATTERY")) {
            battery = nodeContent;
        }
    }

    public String getTemperature() {
        return temperature;
    }

    public String getBattery() {
        return battery;
    }
}
