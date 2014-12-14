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

import java.util.Arrays;
import java.util.List;

import li.klass.fhem.appwidget.annotation.ResourceIdMapper;
import li.klass.fhem.domain.core.DeviceFunctionality;
import li.klass.fhem.domain.core.ToggleableDevice;
import li.klass.fhem.domain.genericview.OverviewViewSettings;
import li.klass.fhem.domain.genericview.ShowField;
import li.klass.fhem.util.ValueExtractUtil;

@SuppressWarnings("unused")
@OverviewViewSettings(showState = true, showMeasured = true)
public class YamahaAVRDevice extends ToggleableDevice<YamahaAVRDevice> {

    private int volume;
    private boolean muted;

    private String input;

    public static final List<String> AVAILABLE_INPUTS = Arrays.asList("audio1", "audio2", "av5", "av6", "bluetooth",
            "netradio", "napster", "pc", "phono", "tuner", "uaw", "usb", "v-aux", "ipod", "ipod_usb");

    @Override
    public boolean supportsToggle() {
        return true;
    }

    public void readVOLUME_LEVEL(String value) {
        volume = ValueExtractUtil.extractLeadingInt(value);
    }

    public void readVOLUME(String value) {
        readVOLUME_LEVEL(value);
    }

    @ShowField(description = ResourceIdMapper.musicVolume)
    public String getVolumeDesc() {
        return volume + "";
    }

    public void readMUTE(String value) {
        muted = ValueExtractUtil.onOffToTrueFalse(value);
    }

    public void readINPUT(String value) {
        input = value;
    }

    public int getVolume() {
        return volume;
    }

    public boolean isMuted() {
        return muted;
    }

    public String getInput() {
        return input;
    }

    public int getSelectedInputPosition() {
        return AVAILABLE_INPUTS.indexOf(input);
    }

    @Override
    public DeviceFunctionality getDeviceGroup() {
        return DeviceFunctionality.REMOTE_CONTROL;
    }
}
