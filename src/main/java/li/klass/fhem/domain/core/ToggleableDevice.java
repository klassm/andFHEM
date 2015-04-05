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

package li.klass.fhem.domain.core;

import java.util.Locale;

import static com.google.common.collect.Sets.newHashSet;
import static li.klass.fhem.domain.core.ToggleableDevice.ButtonHookType.NORMAL;
import static li.klass.fhem.domain.core.ToggleableDevice.ButtonHookType.OFF_DEVICE;
import static li.klass.fhem.domain.core.ToggleableDevice.ButtonHookType.ON_DEVICE;
import static li.klass.fhem.domain.core.ToggleableDevice.ButtonHookType.ON_OFF_DEVICE;
import static li.klass.fhem.domain.core.ToggleableDevice.ButtonHookType.TOGGLE_DEVICE;
import static li.klass.fhem.domain.core.ToggleableDevice.ButtonHookType.WEBCMD_DEVICE;

@SuppressWarnings("unused")
public abstract class ToggleableDevice<T extends FhemDevice<T>> extends FhemDevice<T> {

    private boolean doInvertOnState = false;
    private String onStateName = "on";
    private String offStateName = "off";

    public enum ButtonHookType {
        NORMAL, ON_OFF_DEVICE, ON_DEVICE, OFF_DEVICE, TOGGLE_DEVICE, WEBCMD_DEVICE
    }

    private ButtonHookType buttonHookType = NORMAL;

    public boolean isOnByState() {
        return !isOffByState();
    }

    public boolean isOffByState() {
        String internalState = getToggleStateValue();
        return internalState == null
                || internalState.toLowerCase().contains(getOffStateName().toLowerCase(Locale.getDefault()))
                || internalState.equalsIgnoreCase(eventMapReverse.get(getOffStateName()))
                || internalState.equals("???");
    }

    public boolean isOnRespectingInvertHook() {
        boolean isOn = isOnByState();
        if (doInvertOnState) isOn = !isOn;

        return isOn;
    }

    public boolean supportsToggle() {
        return getSetList().contains("on", "off") ||
                getWebCmd().containsAll(newHashSet("on", "off")) ||
                (eventMap.containsKey("on") && eventMap.containsKey("off"));
    }

    @XmllistAttribute("onOffDevice")
    public void setOnOffDevice(String value) {
        readButtonHookType(value, ON_OFF_DEVICE);
    }

    @XmllistAttribute("onDevice")
    public void setOnDevice(String value) {
        readButtonHookType(value, ON_DEVICE);
    }

    @XmllistAttribute("offDevice")
    public void setOffDevice(String value) {
        readButtonHookType(value, OFF_DEVICE);
    }

    @XmllistAttribute("toggleDevice")
    public void setToggleDevice(String value) {
        readButtonHookType(value, TOGGLE_DEVICE);
    }

    @XmllistAttribute("webCmdDevice")
    public void setWebCmdDevice(String value) {
        readButtonHookType(value, WEBCMD_DEVICE);
    }

    @XmllistAttribute("onStateName")
    public void setOnStateName(String value) {
        onStateName = value;
    }

    @XmllistAttribute("offStateName")
    public void setOffStateName(String value) {
        offStateName = value;
    }

    private void readButtonHookType(String value, ButtonHookType target) {
        if (value.equalsIgnoreCase("true")) {
            buttonHookType = target;
        }
    }

    @XmllistAttribute("invertState")
    public void setInvertState(String value) {
        if (value.equalsIgnoreCase("true")) {
            doInvertOnState = true;
        }
    }

    public ButtonHookType getButtonHookType() {
        return buttonHookType;
    }

    public boolean isSpecialButtonDevice() {
        return buttonHookType != NORMAL;
    }

    public String getOffStateName() {
        return offStateName;
    }

    public String getOnStateName() {
        return onStateName;
    }

    public String getToggleStateValue() {
        return getInternalState();
    }
}
