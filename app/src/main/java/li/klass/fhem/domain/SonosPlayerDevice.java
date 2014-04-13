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

import android.content.Context;

import li.klass.fhem.AndFHEMApplication;
import li.klass.fhem.R;
import li.klass.fhem.appwidget.annotation.ResourceIdMapper;
import li.klass.fhem.domain.core.Device;
import li.klass.fhem.domain.core.DeviceFunctionality;
import li.klass.fhem.domain.genericview.ShowField;

@SuppressWarnings("unused")
public class SonosPlayerDevice extends Device<SonosPlayerDevice> {

    @ShowField(description = ResourceIdMapper.musicMute)
    private String mute;
    @ShowField(description = ResourceIdMapper.musicRepeat)
    private String repeat;
    @ShowField(description = ResourceIdMapper.musicShuffle)
    private String shuffle;
    @ShowField(description = ResourceIdMapper.musicVolume)
    private String volume;
    @ShowField(description = ResourceIdMapper.musicAlbum)
    private String currentAlbum;
    @ShowField(description = ResourceIdMapper.musicSender)
    private String currentSender;
    @ShowField(description = ResourceIdMapper.musicTitle)
    private String currentTitle;
    @ShowField(description = ResourceIdMapper.musicDuration)
    private String currentTrackDuration;
    private String numberOfTracks;
    @ShowField(description = ResourceIdMapper.musicInfo, showInOverview = true, showInDetail = false)
    private String infoSummarize1;
    private String infoSummarize2;
    private String infoSummarize3;


    public void readMUTE(String value) {
        this.mute = yesNoForNumber(value);
    }


    public void readREPEAT(String value) {
        this.repeat = yesNoForNumber(value);
    }


    public void readSHUFFLE(String value) {
        this.shuffle = yesNoForNumber(value);
    }


    public void readVOLUME(String value) {
        this.volume = value;
    }

    public void readCURRENTALBUM(String value) {
        this.currentAlbum = value;
    }


    public void readCURRENTSENDER(String value) {
        this.currentSender = value;
    }


    public void readCURRENTTITLE(String value) {
        this.currentTitle = value;
    }


    public void readCURRENTTRACKDURATION(String value) {
        if (value.equals("0:00:00")) return;

        this.currentTrackDuration = value;
    }


    public void readNUMBEROFTRACKS(String value) {
        this.numberOfTracks = value;
    }


    public void readINFOSUMMARIZE1(String value) {
        this.infoSummarize1 = value;
    }

    public void readINFOSUMMARIZE2(String value) {
        this.infoSummarize2 = value;
    }

    public void readINFOSUMMARIZE3(String value) {
        this.infoSummarize3 = value;
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
        Context context = AndFHEMApplication.getContext();
        if (number.equals("0")) {
            return context.getString(R.string.no);
        } else {
            return context.getString(R.string.yes);
        }
    }

    @Override
    public DeviceFunctionality getDeviceGroup() {
        return DeviceFunctionality.REMOTE_CONTROL;
    }
}
