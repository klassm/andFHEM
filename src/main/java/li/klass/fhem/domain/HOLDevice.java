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

import li.klass.fhem.appwidget.annotation.ResourceIdMapper;
import li.klass.fhem.domain.core.DeviceFunctionality;
import li.klass.fhem.domain.core.ToggleableDevice;
import li.klass.fhem.domain.genericview.OverviewViewSettings;
import li.klass.fhem.domain.genericview.ShowField;
import li.klass.fhem.util.ValueDescriptionUtil;

@OverviewViewSettings(showState = true)
@SuppressWarnings("unused")
public class HOLDevice extends ToggleableDevice<HOLDevice> {

    @ShowField(description = ResourceIdMapper.currentSwitchDevice)
    private String currentSwitchDevice;

    @ShowField(description = ResourceIdMapper.currentSwitchTime)
    private String currentSwitchTime;

    @ShowField(description = ResourceIdMapper.lastSwitchTime)
    private String lastTrigger;

    @ShowField(description = ResourceIdMapper.nextSwitchTime)
    private String nextTrigger;

    public void readCURRENTSWITCHDEVICE(String currentSwitchDevice) {
        this.currentSwitchDevice = currentSwitchDevice;
    }

    public void readCURRENTSWITCHTIME(String currentSwitchTime) {
        this.currentSwitchTime = ValueDescriptionUtil.append(currentSwitchTime, "s");
    }

    public void readLASTTRIGGER(String lastTrigger) {
        this.lastTrigger = lastTrigger;
    }

    public void readNEXTTRIGGER(String nextTrigger) {
        this.nextTrigger = nextTrigger;
    }

    public String getCurrentSwitchDevice() {
        return currentSwitchDevice;
    }

    public String getCurrentSwitchTime() {
        return currentSwitchTime;
    }

    public String getLastTrigger() {
        return lastTrigger;
    }

    public String getNextTrigger() {
        return nextTrigger;
    }

    public boolean isOnByState() {
        if (super.isOnByState()) return true;

        return !getInternalState().equals("off");
    }

    @Override
    public boolean supportsToggle() {
        return true;
    }

    @Override
    public DeviceFunctionality getDeviceGroup() {
        return DeviceFunctionality.FHEM;
    }
}
