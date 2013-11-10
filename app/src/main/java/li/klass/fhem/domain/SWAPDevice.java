package li.klass.fhem.domain;

import li.klass.fhem.domain.core.Device;
import li.klass.fhem.domain.core.DeviceFunctionality;
import li.klass.fhem.util.ValueDescriptionUtil;
import li.klass.fhem.util.ValueExtractUtil;

public class SWAPDevice extends Device<SWAPDevice> {

    @Override
    public String formatTargetState(String targetState) {
        if (targetState.endsWith("°C")) {
            double temperature = ValueExtractUtil.extractLeadingDouble(targetState);
            return ValueDescriptionUtil.appendTemperature(temperature);
        }
        return super.formatTargetState(targetState);
    }

    @Override
    public DeviceFunctionality getDeviceFunctionality() {
        return DeviceFunctionality.TEMPERATURE;
    }
}
