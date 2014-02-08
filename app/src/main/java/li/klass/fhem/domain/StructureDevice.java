package li.klass.fhem.domain;

import android.util.Log;

import li.klass.fhem.domain.core.DeviceFunctionality;
import li.klass.fhem.domain.core.DimmableDevice;
import li.klass.fhem.domain.core.ToggleableDevice;
import li.klass.fhem.util.ArrayUtil;
import li.klass.fhem.util.NumberUtil;

public class StructureDevice extends DimmableDevice<StructureDevice> {
    private int dimLowerBound;
    private int dimStep;
    private int dimUpperBound;
    private boolean supportDim;

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
        return DeviceFunctionality.functionalityForDimmable(this);
    }

    @Override
    public int getDimUpperBound() {
        return dimUpperBound;
    }

    @Override
    public int getDimLowerBound() {
        return dimLowerBound;
    }

    @Override
    public int getDimStep() {
        return dimStep;
    }

    @Override
    public String getDimStateForPosition(int position) {
        return "pct " + position;
    }

    @Override
    public int getPositionForDimState(String dimState) {
        dimState = dimState.replace("pct", "").trim();
        if (! NumberUtil.isNumeric(dimState)) return 0;

        try {
            return Integer.valueOf(dimState.trim());
        } catch (Exception e) {
            Log.e(StructureDevice.class.getName(), "cannot parse dimState " + dimState, e);
            return 0;
        }
    }

    @Override
    public boolean supportsDim() {
        return supportDim;
    }

    @Override
    public void afterDeviceXMLRead() {
        super.afterDeviceXMLRead();

        int[] slider = handleSliderTargetState(getAvailableTargetStates());
        if (slider != null) {
            dimLowerBound = slider[0];
            dimStep = slider[1];
            dimUpperBound = slider[2];

            supportDim = true;
        }
    }
}
