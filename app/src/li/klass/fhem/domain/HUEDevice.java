package li.klass.fhem.domain;

import li.klass.fhem.appwidget.annotation.ResourceIdMapper;
import li.klass.fhem.domain.core.DimmableDevice;
import li.klass.fhem.domain.genericview.DetailOverviewViewSettings;
import li.klass.fhem.domain.genericview.ShowField;
import li.klass.fhem.util.ValueDescriptionUtil;
import li.klass.fhem.util.ValueExtractUtil;

@SuppressWarnings("unused")
@DetailOverviewViewSettings(showState = true)
public class HUEDevice extends DimmableDevice<HUEDevice> {

    public enum SubType {
        COLORDIMMER, DIMMER, SWITCH
    }

    private SubType subType;

    @ShowField(description = ResourceIdMapper.model)
    private String model;

    private Integer hue;
    private Integer brightness;
    private Integer saturation;
    private int level;

    public void readSUBTYPE(String value) {
        try {
            subType = SubType.valueOf(value.toUpperCase());
        } catch (Exception e) {
            subType = null;
        }
    }

    public void readMODEL(String value) {
        model = value;
    }

    public void readBRI(String value) {
        brightness = ValueExtractUtil.extractLeadingInt(value);
    }

    public void readHUE(String value) {
        hue = ValueExtractUtil.extractLeadingInt(value);
    }

    public void readSAT(String value) {
        saturation = ValueExtractUtil.extractLeadingInt(value);
    }

    public void readLEVEL(String value) {
        level = ValueExtractUtil.extractLeadingInt(value);
    }

    public void readSTATE(String value) {
        setState(value);
    }

    @Override
    public void setState(String state) {
        if (state.startsWith("pct")) {
            level = getPositionForDimState(state);
            super.setState(ValueDescriptionUtil.appendPercent(level));
        } else {
            super.setState(state);
        }
    }

    @Override
    public int getDimUpperBound() {
        return 100;
    }

    @Override
    public String getDimStateForPosition(int position) {
        return "pct " + position;
    }

    @Override
    public int getPositionForDimState(String dimState) {
        String value = dimState.replace("pct ", "").trim();
        return ValueExtractUtil.extractLeadingInt(value);
    }

    @Override
    public boolean supportsDim() {
        return subType == SubType.COLORDIMMER || subType == SubType.DIMMER;
    }

    @Override
    public boolean supportsToggle() {
        return supportsDim() || subType == SubType.SWITCH;
    }

    @Override
    public boolean isSupported() {
        return subType != null;
    }

    public SubType getSubType() {
        return subType;
    }

    public int getBrightness() {
        return brightness;
    }

    @ShowField(description = ResourceIdMapper.brightness)
    public String getBrightnessDesc() {
        return brightness + "";
    }

    public String getModel() {
        return model;
    }

    public int getHue() {
        return hue;
    }

    @ShowField(description = ResourceIdMapper.hue)
    public String getHueDesc() {
        return hue + "";
    }

    public int getSaturation() {
        return saturation;
    }

    @ShowField(description = ResourceIdMapper.saturation)
    public String getSaturationDesc() {
        return saturation + "";
    }

    @Override
    public String getDimStateFieldValue() {
        return level + "";
    }
}
