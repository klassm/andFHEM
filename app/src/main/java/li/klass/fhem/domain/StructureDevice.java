package li.klass.fhem.domain;

import li.klass.fhem.domain.core.DeviceFunctionality;
import li.klass.fhem.domain.core.ToggleableDevice;
import li.klass.fhem.util.ArrayUtil;

public class StructureDevice extends ToggleableDevice<StructureDevice> {
    @Override
    public boolean supportsToggle() {
        return ArrayUtil.contains(getAvailableTargetStates(), "on", "off");
    }

    @Override
    public boolean isOnByState() {
        return super.isOnByState() || getState().equalsIgnoreCase("on");
    }

    @Override
    public DeviceFunctionality getDeviceFunctionality() {
        return DeviceFunctionality.FHEM;
    }
}
