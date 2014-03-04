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

import android.util.Log;

import org.w3c.dom.NamedNodeMap;

import li.klass.fhem.appwidget.annotation.ResourceIdMapper;
import li.klass.fhem.domain.core.DeviceFunctionality;
import li.klass.fhem.domain.core.DimmableDevice;
import li.klass.fhem.domain.genericview.OverviewViewSettings;
import li.klass.fhem.domain.genericview.ShowField;
import li.klass.fhem.domain.setlist.SetListSliderValue;
import li.klass.fhem.domain.setlist.SetListTypedValue;
import li.klass.fhem.domain.setlist.SetListValue;
import li.klass.fhem.util.NumberUtil;

import static li.klass.fhem.util.NumberSystemUtil.hexToDecimal;

@OverviewViewSettings(showState = true)
@SuppressWarnings("unused")
public class DummyDevice extends DimmableDevice<DummyDevice> {

    private boolean timerDevice = false;
    private SetListSliderValue sliderValue = null;

    public void readSTATE(String tagName, String value, NamedNodeMap attributes) {
        String measured = attributes.getNamedItem("measured").getNodeValue();
        setMeasured(measured);
    }

    public void readSTATE(String value) {
        setState(value);
    }

    @Override
    public boolean supportsToggle() {
        return getSetList().contains("on", "off") ||
                getSetList().contains(getWebCmd()) && getSetList().contains("on", "off");
    }

    @Override
    public boolean isOnByState() {
        return super.isOnByState() || getState().equalsIgnoreCase("on");
    }

    @Override
    public void afterDeviceXMLRead() {
        super.afterDeviceXMLRead();

        SetListValue value = getSetList().get("state");

        if (value instanceof SetListTypedValue && ((SetListTypedValue) value).getType().equalsIgnoreCase("time")) {
            timerDevice = true;
        }

        if (value instanceof SetListSliderValue) {
            sliderValue = (SetListSliderValue) value;
        }
    }

    @Override
    public DeviceFunctionality getDeviceFunctionality() {
        if (getSetList().contains("rgb")) {
            return DeviceFunctionality.SWITCH;
        }

        return DeviceFunctionality.functionalityForDimmable(this);
    }

    public boolean isTimerDevice() {
        return timerDevice;
    }

    @Override
    public int getDimLowerBound() {
        return sliderValue.getStart();
    }

    @Override
    public int getDimUpperBound() {
        return sliderValue.getStop();
    }

    @Override
    public int getDimStep() {
        return sliderValue.getStep();
    }

    @Override
    public String getDimStateForPosition(int position) {
        return position + "";
    }

    @Override
    public int getPositionForDimState(String dimState) {
        if (!NumberUtil.isNumeric(dimState)) return 0;
        try {
            return Integer.valueOf(dimState.trim());
        } catch (Exception e) {
            Log.e(DummyDevice.class.getName(), "cannot parse dimState " + dimState, e);
            return 0;
        }
    }

    @Override
    public boolean supportsDim() {
        return sliderValue != null;
    }

    @ShowField(description = ResourceIdMapper.color)
    public String getRgbDesc() {
        String rgb = getRgb();
        if (rgb == null) return null;

        return "0x" + rgb;
    }

    private String getRgb() {
        if (! getSetList().contains("rgb")) return null;
        String state = getInternalState();
        if (!state.startsWith("rgb")) return null;

        return state.substring("rgb".length()).trim();
    }

    public int getRGBColor() {
        String rgb = getRgb();
        if (rgb == null) return 0;
        return hexToDecimal(rgb);
    }

    public void readRGB(String value) {
        setState("rgb " + value);
    }
}
