package li.klass.fhem.domain;

import li.klass.fhem.domain.core.Device;
import li.klass.fhem.domain.core.DeviceFunctionality;
import li.klass.fhem.domain.genericview.OverviewViewSettings;

@OverviewViewSettings(showState = true)
public class SonosDevice extends Device<SonosDevice> {
    @Override
    public DeviceFunctionality getDeviceGroup() {
        return DeviceFunctionality.FHEM;
    }
}
