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

import li.klass.fhem.domain.core.DeviceFunctionality;
import li.klass.fhem.domain.core.ToggleableDevice;
import li.klass.fhem.domain.core.XmllistAttribute;
import li.klass.fhem.service.room.xmllist.DeviceNode;

import static li.klass.fhem.util.NumberSystemUtil.hexToDecimal;
import static li.klass.fhem.util.ValueDescriptionUtil.appendTemperature;
import static li.klass.fhem.util.ValueExtractUtil.extractLeadingDouble;

@SuppressWarnings("unused")
public class SWAPDevice extends ToggleableDevice<SWAPDevice> {

    private int rgb = 0;

    @XmllistAttribute("0B_RGBLEVEL")
    public void setRgbLevel(String value) {
        if (value != null && value.matches("[0-9A-F]{8}")) {
            rgb = hexToDecimal(value.substring(2));
        }
    }

    @XmllistAttribute("rgb")
    public void setRgb(String value) {
        rgb = hexToDecimal(value);
    }

    public int getRgb() {
        return rgb;
    }

    public boolean supportsRGB() {
        return getSetList().contains("rgb");
    }

    @Override
    public DeviceFunctionality getDeviceGroup() {
        return DeviceFunctionality.TEMPERATURE;
    }
}
