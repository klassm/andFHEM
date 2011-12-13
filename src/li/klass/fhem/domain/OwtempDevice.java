package li.klass.fhem.domain;

public class OwtempDevice extends Device<OwtempDevice> {
    private String temperature;
    private String warnings;

    @Override
    public void onChildItemRead(String keyValue, String nodeContent) {
        if (keyValue.equals("TEMPERATURE")) {
            this.temperature = nodeContent;
        } else if (keyValue.equals("WARNINGS")) {
            this.warnings = nodeContent;
        }
    }

    public String getTemperature() {
        return temperature;
    }

    public String getWarnings() {
        return warnings;
    }
}
