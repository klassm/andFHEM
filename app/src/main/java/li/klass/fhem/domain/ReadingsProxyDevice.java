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

import li.klass.fhem.appwidget.annotation.ResourceIdMapper;
import li.klass.fhem.domain.core.DeviceFunctionality;
import li.klass.fhem.domain.core.DimmableContinuousStatesDevice;
import li.klass.fhem.domain.genericview.ShowField;

import static li.klass.fhem.domain.core.DeviceFunctionality.functionalityForDimmable;
import static li.klass.fhem.util.NumberSystemUtil.hexToDecimal;

@SuppressWarnings("unused")
public class ReadingsProxyDevice extends DimmableContinuousStatesDevice<ReadingsProxyDevice> {

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

    @Override
    public DeviceFunctionality getDeviceGroup() {
        return functionalityForDimmable(this);
    }

    @Override
    public boolean acceptXmlKey(String key) {
        // we only want uppercase RGB values
        return !"rgb".equals(key);
    }
}
