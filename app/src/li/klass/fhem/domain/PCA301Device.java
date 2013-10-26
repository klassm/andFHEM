package li.klass.fhem.domain;

import li.klass.fhem.appwidget.annotation.ResourceIdMapper;
import li.klass.fhem.domain.core.ToggleableDevice;
import li.klass.fhem.domain.genericview.ShowField;
import li.klass.fhem.util.ValueDescriptionUtil;

public class PCA301Device extends ToggleableDevice<PCA301Device> {

    @ShowField(description = ResourceIdMapper.energyConsumption)
    private String consumption;
    @ShowField(description = ResourceIdMapper.energyPower)
    private String power;

    public void readCONSUMPTION(String value) {
        consumption = ValueDescriptionUtil.append(value, "kWh");
    }

    public void readPOWER(String value) {
        power = ValueDescriptionUtil.append(value, "W");
    }

    @Override
    public boolean supportsToggle() {
        return true;
    }

    public String getConsumption() {
        return consumption;
    }

    public String getPower() {
        return power;
    }
}
