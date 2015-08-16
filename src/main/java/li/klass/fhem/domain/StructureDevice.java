package li.klass.fhem.domain;

import li.klass.fhem.domain.core.DeviceFunctionality;
import li.klass.fhem.domain.core.DimmableContinuousStatesDevice;

public class StructureDevice extends DimmableContinuousStatesDevice<StructureDevice> {

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
