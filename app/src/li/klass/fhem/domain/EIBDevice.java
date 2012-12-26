package li.klass.fhem.domain;

import li.klass.fhem.domain.core.ToggleableDevice;
import li.klass.fhem.domain.genericview.DetailOverviewViewSettings;
import li.klass.fhem.domain.genericview.FloorplanViewSettings;

@DetailOverviewViewSettings(showState = true)
@FloorplanViewSettings
public class EIBDevice extends ToggleableDevice<EIBDevice> {
    @Override
    public boolean isOnByState() {
        String internalState = getInternalState();
        return internalState.equalsIgnoreCase("on") || internalState.equalsIgnoreCase("on-for-timer") ||
                internalState.equalsIgnoreCase("on-till");
    }

    @Override
    public boolean supportsToggle() {
        return true;
    }
}
