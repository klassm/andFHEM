package li.klass.fhem.domain;

import li.klass.fhem.domain.core.DeviceFunctionality;
import li.klass.fhem.domain.core.ToggleableDevice;
import li.klass.fhem.util.ValueDescriptionUtil;
import li.klass.fhem.util.ValueExtractUtil;

public class SWAPDevice extends ToggleableDevice<SWAPDevice> {

    @Override
    public String formatTargetState(String targetState) {
        if (targetState.endsWith("°C")) {
            double temperature = ValueExtractUtil.extractLeadingDouble(targetState);
            return ValueDescriptionUtil.appendTemperature(temperature);
        }
        return super.formatTargetState(targetState);
    }

    public boolean supportsRGB() {
        return getSetList().contains("rgb");
    }

    @Override
    public DeviceFunctionality getDeviceFunctionality() {
        return DeviceFunctionality.TEMPERATURE;
    }
}
