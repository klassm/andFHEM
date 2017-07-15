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
import li.klass.fhem.domain.genericview.ShowField;
import li.klass.fhem.domain.multimedia.VolumeDevice;
import li.klass.fhem.resources.ResourceIdMapper;

import static li.klass.fhem.util.ValueExtractUtil.extractLeadingFloat;
import static li.klass.fhem.util.ValueExtractUtil.onOffToTrueFalse;

public class PioneerAvrZoneDevice extends ToggleableDevice implements VolumeDevice {

    @XmllistAttribute("input")
    private String input;

    @XmllistAttribute("mute")
    private String mute;

    @XmllistAttribute("power")
    private String power;

    @XmllistAttribute("volume")
    @ShowField(description = ResourceIdMapper.musicVolume, showInOverview = true)
    private String volume;

    @Override
    public DeviceFunctionality getDeviceGroup() {
        return DeviceFunctionality.REMOTE_CONTROL;
    }

    public String getInput() {
        return input;
    }

    public String getMute() {
        return mute;
    }

    @Override
    public boolean isMuted() {
        return onOffToTrueFalse(mute);
    }

    public String getPower() {
        return power;
    }

    public String getVolume() {
        return volume;
    }

    @Override
    public float getVolumeAsFloat() {
        return extractLeadingFloat(volume);
    }

    @Override
    public void setState(String state) {
        if (state.matches("on|off")) {
            power = state;
        }
        super.setState(state);
    }
}
