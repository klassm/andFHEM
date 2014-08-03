package li.klass.fhem.domain;

import li.klass.fhem.appwidget.annotation.ResourceIdMapper;
import li.klass.fhem.domain.core.DimmableContinuousStatesDevice;
import li.klass.fhem.domain.genericview.ShowField;

import static li.klass.fhem.util.NumberSystemUtil.hexToDecimal;

public class DMXDevice extends DimmableContinuousStatesDevice<DMXDevice> {
    private String pct;

    @ShowField(description = ResourceIdMapper.color)
    private String rgb;

    public void readPCT(String pct) {
        this.pct = pct;
    }

    public void readRGB(String rgb) {
        this.rgb = rgb.toUpperCase();
    }

    public int getRGBColor() {
        if (rgb == null) return 0;
        return hexToDecimal(rgb);
    }

    public String getPct() {
        return pct;
    }

    public String getRgb() {
        return rgb;
    }

    @Override
    public String getDimStateFieldValue() {
        return pct;
    }

    @Override
    public void setState(String state) {
        if (state.startsWith("pct")) {
            readPCT(state.substring("PCT".length()).trim());
        } else {
            super.setState(state);
        }
    }

    @Override
    public boolean shouldUpdateStateOnDevice(String stateToSet) {
        return stateToSet != null && stateToSet.startsWith("pct");
    }

    @Override
    protected String getSetListDimStateAttributeName() {
        return "pct";
    }
}
