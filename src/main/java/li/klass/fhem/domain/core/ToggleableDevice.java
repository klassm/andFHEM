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
                || internalState.toLowerCase(Locale.getDefault()).contains(getOffStateName().toLowerCase(Locale.getDefault()))
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

    @XmllistAttribute("onStateName")
    public void setOnStateName(String value) {
        onStateName = value;
    }

    @XmllistAttribute("offStateName")
    public void setOffStateName(String value) {
        offStateName = value;
    }

    @XmllistAttribute("invertState")
    public void setInvertState(String value) {
        if (value.equalsIgnoreCase("true")) {
            doInvertOnState = true;
        }
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
