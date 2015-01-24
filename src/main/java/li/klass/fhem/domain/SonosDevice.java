package li.klass.fhem.domain;

import li.klass.fhem.domain.core.DeviceFunctionality;
import li.klass.fhem.domain.core.FhemDevice;
import li.klass.fhem.domain.genericview.OverviewViewSettings;

@OverviewViewSettings(showState = true)
public class SonosDevice extends FhemDevice<SonosDevice> {
    @Override
    public DeviceFunctionality getDeviceGroup() {
        return DeviceFunctionality.FHEM;
    }
}
