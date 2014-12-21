package li.klass.fhem.domain;

import li.klass.fhem.domain.core.DeviceFunctionality;
import li.klass.fhem.domain.core.ToggleableDevice;
import li.klass.fhem.util.ValueDescriptionUtil;
import li.klass.fhem.util.ValueExtractUtil;

import static li.klass.fhem.util.NumberSystemUtil.hexToDecimal;

@SuppressWarnings("unused")
public class SWAPDevice extends ToggleableDevice<SWAPDevice> {

    private int rgb = 0;

    @Override
    public String formatTargetState(String targetState) {
        if (targetState.endsWith("°C")) {
            double temperature = ValueExtractUtil.extractLeadingDouble(targetState);
            return ValueDescriptionUtil.appendTemperature(temperature);
        }
        return super.formatTargetState(targetState);
    }

    public void read0B_RGBLEVEL(String value) {
        if (value != null && value.matches("[0-9A-F]{8}")) {
            rgb = hexToDecimal(value.substring(2));
        }
    }

    public void readRGB(String value) {
        rgb = hexToDecimal(value);
    }

    public int getRgb() {
        return rgb;
    }

    public boolean supportsRGB() {
        return getSetList().contains("rgb");
    }

    @Override
    public DeviceFunctionality getDeviceGroup() {
        return DeviceFunctionality.TEMPERATURE;
    }
}
