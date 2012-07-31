/*
 * AndFHEM - Open Source Android application to control a FHEM home automation
 *  server.
 *
 *  Copyright (c) 2012, Matthias Klass or third-party contributors as
 *  indicated by the @author tags or express copyright attribution
 *  statements applied by the authors.  All third-party contributions are
 *  distributed under license by Red Hat Inc.
 *
 *  This copyrighted material is made available to anyone wishing to use, modify,
 *  copy, or redistribute it subject to the terms and conditions of the GNU GENERAL PUBLICLICENSE, as published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *  or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU GENERAL PUBLIC LICENSE
 *  for more details.
 *
 *  You should have received a copy of the GNU GENERAL PUBLIC LICENSE
 *  along with this distribution; if not, write to:
 *    Free Software Foundation, Inc.
 *    51 Franklin Street, Fifth Floor
 */

package li.klass.fhem.domain;

import li.klass.fhem.R;
import li.klass.fhem.domain.core.ToggleableDevice;
import li.klass.fhem.domain.genericview.ShowField;
import org.w3c.dom.NamedNodeMap;

public class TRXLightDevice extends ToggleableDevice<TRXLightDevice> {

    @ShowField(description = R.string.type)
    private String type;

    public void setTRX_LIGHT_TYPE(String value) {
        this.type = value;
    }

    public void readSTATE(String tagName, NamedNodeMap attributes, String value) {
        if (tagName.equals("STATE")) {
            this.measured = attributes.getNamedItem("measured").getNodeValue();
        }
    }

    @Override
    public boolean isOn() {
        return getState().equalsIgnoreCase("on");
    }

    @Override
    public boolean supportsToggle() {
        return true;
    }

    @SuppressWarnings("unused")
    public String getType() {
        return type;
    }
}
