package li.klass.fhem.domain;

import android.util.Log;

import li.klass.fhem.domain.core.DeviceFunctionality;
import li.klass.fhem.domain.core.DimmableDevice;
import li.klass.fhem.domain.setlist.SetListSliderValue;
import li.klass.fhem.domain.setlist.SetListValue;
import li.klass.fhem.util.NumberUtil;

public class StructureDevice extends DimmableDevice<StructureDevice> {
    private SetListSliderValue sliderValue = null;

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
        return sliderValue.getStop();
    }

    @Override
    public int getDimLowerBound() {
        return sliderValue.getStart();
    }

    @Override
    public int getDimStep() {
        return sliderValue.getStep();
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
        return sliderValue != null;
    }

    @Override
    public void afterDeviceXMLRead() {
        super.afterDeviceXMLRead();

        SetListValue setListValue = getSetList().get("state");
        if (setListValue instanceof SetListSliderValue) {
            this.sliderValue = (SetListSliderValue) setListValue;
        }
    }
}
