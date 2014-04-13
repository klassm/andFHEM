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

import org.w3c.dom.NamedNodeMap;

import li.klass.fhem.domain.core.DeviceFunctionality;
import li.klass.fhem.domain.core.ToggleableDevice;
import li.klass.fhem.domain.genericview.OverviewViewSettings;

@OverviewViewSettings(showState = true, showMeasured = true)
public class FS20ZDRDevice extends ToggleableDevice<FS20ZDRDevice> {
    @Override
    public boolean supportsToggle() {
        return true;
    }

    @Override
    public void onChildItemRead(String tagName, String key, String value, NamedNodeMap attributes) {
        if (tagName.equalsIgnoreCase("STATE") && key.equalsIgnoreCase("STATE")) {
            String measured = attributes.getNamedItem("measured").getNodeValue();
            setMeasured(measured);
        }
    }

    @Override
    public boolean shouldUpdateStateOnDevice(String stateToSet) {
        return (stateToSet.equalsIgnoreCase("on") || stateToSet.equalsIgnoreCase("off"));
    }

    @Override
    public DeviceFunctionality getDeviceGroup() {
        return DeviceFunctionality.REMOTE_CONTROL;
    }
}
