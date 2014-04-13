package li.klass.fhem.domain;

import li.klass.fhem.domain.core.DeviceFunctionality;
import li.klass.fhem.domain.core.DimmableContinuousStatesDevice;

public class StructureDevice extends DimmableContinuousStatesDevice<StructureDevice> {

    @Override
    public boolean isOnByState() {
        return super.isOnByState() || getState().equalsIgnoreCase("on");
    }

    @Override
    public DeviceFunctionality getDeviceGroup() {
        return DeviceFunctionality.functionalityForDimmable(this);
    }

    @Override
    public boolean supportsOnOffDimMapping() {
        return false;
    }

    @Override
    protected String getSetListDimStateAttributeName() {
        return "pct";
    }
}
