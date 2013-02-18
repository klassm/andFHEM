package li.klass.fhem.domain;

import li.klass.fhem.domain.core.ToggleableDevice;
import li.klass.fhem.domain.genericview.DetailOverviewViewSettings;
import li.klass.fhem.domain.genericview.FloorplanViewSettings;
import li.klass.fhem.util.ArrayUtil;
import li.klass.fhem.util.ValueDescriptionUtil;
import li.klass.fhem.util.ValueExtractUtil;

@DetailOverviewViewSettings(showState = true)
@FloorplanViewSettings
@SuppressWarnings("unused")
public class EIBDevice extends ToggleableDevice<EIBDevice> {

    private String model;

    @Override
    public boolean isOnByState() {
        if (super.isOnByState()) return true;

        String internalState = getInternalState();
        return internalState.equalsIgnoreCase("on") || internalState.equalsIgnoreCase("on-for-timer") ||
                internalState.equalsIgnoreCase("on-till");
    }

    public void readMODEL(String value) {
        model = value;
    }

    @Override
    public void afterXMLRead() {
        super.afterXMLRead();

        if (model == null || model.equals("time") || getInternalState().equalsIgnoreCase("???")) return;

        double value = ValueExtractUtil.extractLeadingDouble(getInternalState());
        String description = "";
        if (model.equalsIgnoreCase("speedsensor")) {
            description = ValueDescriptionUtil.M_S;
        } else if (model.equalsIgnoreCase("tempsensor")) {
            description = ValueDescriptionUtil.C;
        } else if (model.equalsIgnoreCase("brightness") || model.equalsIgnoreCase("lightsensor")) {
            description = ValueDescriptionUtil.LUX;
        }

        setState(ValueDescriptionUtil.append(value, description));
    }

    @Override
    public boolean supportsToggle() {
        return ArrayUtil.contains(getAvailableTargetStates(), "on", "off");
    }
}
