package li.klass.fhem.domain;

public class CULWSDevice extends Device<CULWSDevice> {
    
    private String humidity;
    private String temperature;

    @Override
    public void onChildItemRead(String keyValue, String nodeContent) {
        if (keyValue.equals("HUMIDITY")) {
            humidity = nodeContent + " (%)";
        } else if (keyValue.equals("TEMPERATURE")) {
            temperature = nodeContent + " (° Celsius)";
        }
    }

    public String getHumidity() {
        return humidity;
    }

    public String getTemperature() {
        return temperature;
    }
}
