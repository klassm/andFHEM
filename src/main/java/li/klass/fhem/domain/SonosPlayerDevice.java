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
import li.klass.fhem.domain.core.FhemDevice;
import li.klass.fhem.domain.core.XmllistAttribute;
import li.klass.fhem.domain.genericview.ShowField;
import li.klass.fhem.domain.multimedia.VolumeDevice;
import li.klass.fhem.resources.ResourceIdMapper;
import li.klass.fhem.util.ValueExtractUtil;

@SuppressWarnings("unused")
public class SonosPlayerDevice extends FhemDevice implements VolumeDevice {

    @ShowField(description = ResourceIdMapper.musicMute)
    private String mute;

    @ShowField(description = ResourceIdMapper.musicRepeat)
    private String repeat;

    @ShowField(description = ResourceIdMapper.musicShuffle)
    private String shuffle;

    @ShowField(description = ResourceIdMapper.musicVolume)
    @XmllistAttribute("volume")
    private String volume;

    @ShowField(description = ResourceIdMapper.musicAlbum)
    @XmllistAttribute("currentAlbum")
    private String currentAlbum;

    @ShowField(description = ResourceIdMapper.musicSender)
    @XmllistAttribute("currentSender")
    private String currentSender;

    @ShowField(description = ResourceIdMapper.musicTitle)
    @XmllistAttribute("currentTitle")
    private String currentTitle;

    @ShowField(description = ResourceIdMapper.musicDuration)
    @XmllistAttribute("currentTrackDuration")
    private String currentTrackDuration;

    @XmllistAttribute("numberOfTracks")
    private String numberOfTracks;

    @ShowField(description = ResourceIdMapper.musicInfo, showInOverview = true, showInDetail = false)
    @XmllistAttribute("infoSummarize1")
    private String infoSummarize1;

    @XmllistAttribute("infoSummarize2")
    private String infoSummarize2;

    @XmllistAttribute("infoSummarize3")
    private String infoSummarize3;

    @XmllistAttribute("mute")
    public void setMute(String value) {
        this.mute = yesNoForNumber(value);
    }

    @XmllistAttribute("repeat")
    public void setRepeat(String value) {
        this.repeat = yesNoForNumber(value);
    }

    @XmllistAttribute("shuffle")
    public void setShuffle(String value) {
        this.shuffle = yesNoForNumber(value);
    }

    public String getMute() {
        return mute;
    }

    public String getRepeat() {
        return repeat;
    }

    public String getShuffle() {
        return shuffle;
    }

    public String getVolume() {
        return volume;
    }

    public String getCurrentAlbum() {
        return currentAlbum;
    }

    public String getCurrentSender() {
        return currentSender;
    }

    public String getCurrentTitle() {
        return currentTitle;
    }

    public String getCurrentTrackDuration() {
        return currentTrackDuration;
    }

    public String getNumberOfTracks() {
        return numberOfTracks;
    }

    public String getInfoSummarize1() {
        return infoSummarize1;
    }

    public String getInfoSummarize2() {
        return infoSummarize2;
    }

    public String getInfoSummarize3() {
        return infoSummarize3;
    }

    private String yesNoForNumber(String number) {
        return number.equals("0") ? "no" : "yes";
    }

    @Override
    public DeviceFunctionality getDeviceGroup() {
        return DeviceFunctionality.REMOTE_CONTROL;
    }

    @Override
    public boolean isMuted() {
        return "yes".equals(mute);
    }

    public float getVolumeAsFloat() {
        return ValueExtractUtil.extractLeadingFloat(volume);
    }
}
