/*
 * AndFHEM - Open Source Android application to control a FHEM home automation
 * server.
 *
 * Copyright (c) 2012, Matthias Klass or third-party contributors as
 * indicated by the @author tags or express copyright attribution
 * statements applied by the authors.  All third-party contributions are
 * distributed under license by Red Hat Inc.
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU GENERAL PUBLICLICENSE, as published by the Free Software Foundation.
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
 */

package li.klass.fhem.domain;

import org.w3c.dom.NamedNodeMap;

public abstract class ToggleableDevice<T extends Device> extends Device<T> {

    /**
     * Variable set by the user attribute onOffDevice in fhem.cfg. If set and the device being a toggleable device,
     * show on / off buttons instead of toggle buttons.
     */
    private boolean onOffDevice = false;

    public abstract boolean isOn();
    public abstract boolean supportsToggle();

    @Override
    protected void onChildItemRead(String tagName, String keyValue, String nodeContent, NamedNodeMap attributes) {
        if (keyValue.equalsIgnoreCase("ONOFFDEVICE")) {
            this.onOffDevice = Boolean.valueOf(nodeContent);
        }
    }

    public boolean isOnOffDevice() {
        return onOffDevice;
    }
}
