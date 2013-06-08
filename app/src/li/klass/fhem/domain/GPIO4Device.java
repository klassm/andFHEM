package li.klass.fhem.domain;

import li.klass.fhem.appwidget.annotation.ResourceIdMapper;
import li.klass.fhem.domain.core.Device;
import li.klass.fhem.domain.genericview.ShowField;
import li.klass.fhem.util.ValueDescriptionUtil;

@SuppressWarnings("unused")
public class GPIO4Device extends Device<GPIO4Device> {

    private SubType subType = null;

    @ShowField(description = ResourceIdMapper.temperature, showInOverview = true)
    private String temperature;

    private enum SubType {
        TEMPERATURE
    }

    public void readMODEL(String value) {
        if (value.equals("DS1820")) {
            subType = SubType.TEMPERATURE;
        }
    }

    public void readTEMPERATURE(String value) {
        temperature = ValueDescriptionUtil.appendTemperature(value);
    }

    public String getTemperature() {
        return temperature;
    }

    @Override
    public boolean isSupported() {
        return subType != null;
    }
}
