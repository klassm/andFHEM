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

    /**
     * Variable set by the user attribute onOffDevice in fhem.cfg. If set and the device being a toggleable device,
     * show on / off buttons instead of toggle buttons.
     */
    private boolean onOffDevice = false;

    public abstract boolean isOn();

    public abstract boolean supportsToggle();

    public void readONOFFDEVICE(String value) {
        this.onOffDevice = value.equals("true");
    }

    public boolean isOnOffDevice() {
        return onOffDevice;
    }
}
