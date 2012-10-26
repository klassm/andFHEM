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

@SuppressWarnings("unused")
public abstract class ToggleableDevice<T extends Device> extends Device<T> {

    public enum HookType {
        NORMAL, ON_OFF_DEVICE, ON_DEVICE, OFF_DEVICE, TOGGLE_DEVICE
    }

    private HookType hookType = HookType.NORMAL;

    public abstract boolean isOn();

    public abstract boolean supportsToggle();

    public void readONOFFDEVICE(String value) {
        if (value.equalsIgnoreCase("true")) hookType = HookType.ON_OFF_DEVICE;
    }

    public void readONDEVICE(String value) {
        if (value.equalsIgnoreCase("true")) hookType = HookType.ON_DEVICE;
    }

    public void readOFFDEVICE(String value) {
        if (value.equalsIgnoreCase("true")) hookType = HookType.OFF_DEVICE;
    }

    public void readTOGGLEDEVICE(String value) {
        if (value.equalsIgnoreCase("true")) hookType = HookType.TOGGLE_DEVICE;
    }

    public HookType getHookType() {
        return hookType;
    }

    public boolean isSpecialButtonDevice() {
        return hookType != HookType.NORMAL;
    }
}
