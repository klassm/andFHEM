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

import java.util.Arrays;

import li.klass.fhem.appwidget.annotation.ResourceIdMapper;
import li.klass.fhem.domain.core.DeviceFunctionality;
import li.klass.fhem.domain.core.DimmableDevice;
import li.klass.fhem.domain.genericview.OverviewViewSettings;
import li.klass.fhem.domain.genericview.ShowField;
import li.klass.fhem.util.ArrayUtil;

import static li.klass.fhem.util.NumberSystemUtil.hexToDecimal;

@OverviewViewSettings(showState = true)
@SuppressWarnings("unused")
public class DummyDevice extends DimmableDevice<DummyDevice> {

    private boolean timerDevice = false;
    private Integer dimLowerBound;
    private Integer dimStep;
    private Integer dimUpperBound;

    public void readSTATE(String tagName, String value, NamedNodeMap attributes) {
        String measured = attributes.getNamedItem("measured").getNodeValue();
        setMeasured(measured);
    }

    public void readSTATE(String value) {
        setState(value);
    }

    @Override
    public boolean supportsToggle() {
        return ArrayUtil.contains(getAvailableTargetStates(), "on", "off") ||
                ArrayUtil.contains(getWebCmd(), "on", "off");
    }

    @Override
    public boolean isOnByState() {
        return super.isOnByState() || getState().equalsIgnoreCase("on");
    }

    @Override
    public void afterDeviceXMLRead() {
        super.afterDeviceXMLRead();

        String[] availableTargetStates = getAvailableTargetStates();
        if (availableTargetStates == null) return;

        if (ArrayUtil.contains(availableTargetStates, "time")) {
            timerDevice = true;
        }

        for (int i = 0; i < availableTargetStates.length; i++) {
            String targetState = availableTargetStates[i];

            try {
                if (targetState.equals("slider")) {
                    dimLowerBound = Integer.valueOf(availableTargetStates[i + 1]);
                    dimStep = Integer.valueOf(availableTargetStates[i + 2]);
                    dimUpperBound = Integer.valueOf(availableTargetStates[i + 3]);
                }
            } catch (Exception e) {
                Log.e(DummyDevice.class.getName(), "cannot parse slider in " + Arrays.asList(availableTargetStates), e);
            }
        }
    }

    @Override
    public DeviceFunctionality getDeviceFunctionality() {
        String[] availableTargetStates = getAvailableTargetStates();
        if (ArrayUtil.contains(availableTargetStates, "rgb")) return DeviceFunctionality.SWITCH;

        return DeviceFunctionality.functionalityForDimmable(this);
    }

    public boolean isTimerDevice() {
        return timerDevice;
    }

    @Override
    public int getDimLowerBound() {
        return dimLowerBound;
    }

    @Override
    public int getDimUpperBound() {
        return dimUpperBound;
    }

    @Override
    public int getDimStep() {
        return dimStep;
    }

    @Override
    public String getDimStateForPosition(int position) {
        return position + "";
    }

    @Override
    public int getPositionForDimState(String dimState) {
        try {
            return Integer.valueOf(dimState.trim());
        } catch (Exception e) {
            Log.e(DummyDevice.class.getName(), "cannot parse dimState " + dimState, e);
            return 0;
        }
    }

    @Override
    public boolean supportsDim() {
        return dimLowerBound != null && dimUpperBound != null && dimStep != null;
    }

    @ShowField(description = ResourceIdMapper.color)
    public String getRgbDesc() {
        String rgb = getRgb();
        if (rgb == null) return null;

        return "0x" + rgb;
    }

    private String getRgb() {
        if (! ArrayUtil.contains(getAvailableTargetStates(), "rgb")) return null;
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
