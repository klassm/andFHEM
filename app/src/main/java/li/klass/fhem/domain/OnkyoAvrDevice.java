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
import li.klass.fhem.domain.core.ToggleableDevice;
import li.klass.fhem.domain.core.XmllistAttribute;
import li.klass.fhem.domain.genericview.ShowField;
import li.klass.fhem.util.ValueExtractUtil;

public class OnkyoAvrDevice extends ToggleableDevice<OnkyoAvrDevice> {
    @XmllistAttribute("volume")
    @ShowField(description = ResourceIdMapper.musicVolume, showInOverview = true)
    private String volume;

    @XmllistAttribute("mute")
    private String mute;

    @XmllistAttribute("input")
    private String input;

    @XmllistAttribute("sleep")
    private String sleep;

    @Override
    public DeviceFunctionality getDeviceGroup() {
        return DeviceFunctionality.REMOTE_CONTROL;
    }

    public String getVolume() {
        return volume;
    }

    public int getVolumeProgress() {
        return ValueExtractUtil.extractLeadingInt(volume);
    }

    public String getMute() {
        return mute;
    }

    public boolean isMuted() {
        return "on".equalsIgnoreCase(mute);
    }

    public String getInput() {
        return input;
    }

    public String getSleep() {
        return sleep;
    }
}
