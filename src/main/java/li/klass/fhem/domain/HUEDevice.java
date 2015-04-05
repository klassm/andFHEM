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

import android.content.Context;

import java.util.Locale;

import li.klass.fhem.domain.core.DimmableContinuousStatesDevice;
import li.klass.fhem.domain.core.XmllistAttribute;
import li.klass.fhem.domain.genericview.OverviewViewSettings;
import li.klass.fhem.domain.genericview.ShowField;
import li.klass.fhem.resources.ResourceIdMapper;
import li.klass.fhem.util.ColorUtil;
import li.klass.fhem.util.NumberSystemUtil;
import li.klass.fhem.util.ValueDescriptionUtil;

@OverviewViewSettings(showState = true)
public class HUEDevice extends DimmableContinuousStatesDevice<HUEDevice> {

    public enum SubType {
        COLORDIMMER, DIMMER, SWITCH
    }

    private double[] xy;
    private int rgb;
    private SubType subType;

    @ShowField(description = ResourceIdMapper.model, showAfter = "definition")
    @XmllistAttribute("MODEL")
    private String model;

    @XmllistAttribute("BRI")
    private Integer brightness;

    @XmllistAttribute("SAT")
    private Integer saturation;

    @XmllistAttribute("PCT")
    private int pct;

    @XmllistAttribute("SUBTYPE")
    public void setSubtype(String value) {
        try {
            subType = SubType.valueOf(value.toUpperCase(Locale.getDefault()).replaceAll("EXT", ""));
        } catch (Exception e) {
            subType = null;
        }
    }

    @XmllistAttribute("XY")
    public void setXY(String value) {
        String[] parts = value.split(",");
        xy = new double[]{Double.valueOf(parts[0]), Double.valueOf(parts[1])};
    }

    @XmllistAttribute("RGB")
    public void setRgb(String value) {
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
    public void afterDeviceXMLRead(Context context) {
        super.afterDeviceXMLRead(context);

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
