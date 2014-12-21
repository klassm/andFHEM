/*
 * AndFHEM - Open Source Android application to control a FHEM home automation
 * server.
 *
 * Copyright (c) 2011, Matthias Klass or third-party contributors as
 * indicated by the @author tags or express copyright attribution
 * statements applied by the authors.  All third-party contributions are
 * distributed under license by Red Hat Inc.
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU GENERAL PUBLIC LICENSE, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU GENERAL PUBLIC LICENSE
 * for more details.
 *
 * You should have received a copy of the GNU GENERAL PUBLIC LICENSE
 * along with this distribution; if not, write to:
 *   Free Software Foundation, Inc.
 *   51 Franklin Street, Fifth Floor
 *   Boston, MA  02110-1301  USA
 */

package li.klass.fhem.domain;

import java.util.Locale;

import li.klass.fhem.appwidget.annotation.ResourceIdMapper;
import li.klass.fhem.domain.core.DimmableContinuousStatesDevice;
import li.klass.fhem.domain.genericview.OverviewViewSettings;
import li.klass.fhem.domain.genericview.ShowField;
import li.klass.fhem.util.ColorUtil;
import li.klass.fhem.util.NumberSystemUtil;
import li.klass.fhem.util.ValueDescriptionUtil;
import li.klass.fhem.util.ValueExtractUtil;

@SuppressWarnings("unused")
@OverviewViewSettings(showState = true)
public class HUEDevice extends DimmableContinuousStatesDevice<HUEDevice> {

    private double[] xy;
    private int rgb;

    public enum SubType {
        COLORDIMMER, DIMMER, SWITCH
    }

    private SubType subType;

    @ShowField(description = ResourceIdMapper.model, showAfter = "definition")
    private String model;

    private Integer brightness;
    private Integer saturation;
    private int pct;

    public void readSUBTYPE(String value) {
        try {
            subType = SubType.valueOf(value.toUpperCase(Locale.getDefault()));
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
        Integer hue = ValueExtractUtil.extractLeadingInt(value);
    }

    public void readSAT(String value) {
        saturation = ValueExtractUtil.extractLeadingInt(value);
    }

    public void readPCT(String value) {
        pct = ValueExtractUtil.extractLeadingInt(value);
    }

    public void readSTATE(String value) {
        setState(value);
    }

    public void readXY(String value) {
        String[] parts = value.split(",");
        xy = new double[]{Double.valueOf(parts[0]), Double.valueOf(parts[1])};
    }

    public void readRGB(String value) {
        rgb = NumberSystemUtil.hexToDecimal(value);

        ColorUtil.XYColor xyColor = ColorUtil.rgbToXY(rgb);
        brightness = xyColor.brightness;
        xy = xyColor.xy;
    }

    @Override
    public void setState(String state) {
        if (state.equals("off")) pct = 0;
        if (state.equals("on")) pct = getDimUpperBound();

        if (state.startsWith("pct")) {
            pct = getPositionForDimState(state);
            super.setState(ValueDescriptionUtil.appendPercent(pct));
        } else {
            super.setState(state);
        }
    }

    @Override
    public void afterDeviceXMLRead() {
        super.afterDeviceXMLRead();

        if (xy != null) {
            this.rgb = ColorUtil.xyToRgb(xy, brightness);
        }
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
        return super.isSupported() && subType != null;
    }

    public SubType getSubType() {
        return subType;
    }

    public int getBrightness() {
        return brightness;
    }

    @ShowField(description = ResourceIdMapper.brightness)
    public String getBrightnessDesc() {
        return brightness == null ? null : brightness + "";
    }

    public String getModel() {
        return model;
    }

    public void setRgb(int rgb) {
        this.rgb = rgb;
    }

    public int getRgb() {
        return rgb;
    }

    public double[] getXy() {
        return xy;
    }

    @ShowField(description = ResourceIdMapper.color)
    public String getRgbDesc() {
        return ColorUtil.toHexString(rgb, 6);
    }

    public int getSaturation() {
        return saturation;
    }

    @ShowField(description = ResourceIdMapper.saturation)
    public String getSaturationDesc() {
        return saturation == null ? null : saturation + "";
    }

    @Override
    public String getDimStateFieldValue() {
        return pct + "";
    }

    @Override
    public boolean acceptXmlKey(String key) {
        return !"name".equals(key) && super.acceptXmlKey(key);
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
