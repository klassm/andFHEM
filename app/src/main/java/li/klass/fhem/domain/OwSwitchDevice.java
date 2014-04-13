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

import li.klass.fhem.domain.core.Device;
import li.klass.fhem.domain.core.DeviceFunctionality;
import li.klass.fhem.domain.genericview.OverviewViewSettings;
import li.klass.fhem.util.ValueExtractUtil;

@OverviewViewSettings(showState = true, showMeasured = true)
@SuppressWarnings("unused")
public class OwSwitchDevice extends Device<OwSwitchDevice> {

    private boolean onA;
    private boolean onB;

    public void readA(String value) {
        this.onA = ! "OFF".equalsIgnoreCase(value);
    }

    public void readB(String value) {
        onB = ! "OFF".equalsIgnoreCase(value);
    }

    public void readGPIO(String value)  {
        int s = ValueExtractUtil.extractLeadingInt(value);

        //Set values for channels (For 2 channels: 3 = A and B off, 1 = B on 2 = A on 0 = both on)
        switch (s) {
            case 3:
                onA = false;
                onB = false;
                break;
            case 2:
                onA = true;
                onB = false;
                break;
            case 1:
                onA = false;
                onB = true;
                break;
            case 0:
                onA = true;
                onB = true;
                break;
        }
    }

    public boolean isOnA() {
        return onA;
    }

    public boolean isOnB() {
        return onB;
    }

    @Override
    public DeviceFunctionality getDeviceGroup() {
        return DeviceFunctionality.SWITCH;
    }

    public int setStateForA(boolean a) {
        return setStateForAB(a, onB);
    }

    public int setStateForB(boolean b) {
        return setStateForAB(onA, b);
    }

    public static int setStateForAB(boolean a, boolean b) {
        //Set values for channels (For 2 channels: 3 = A and B off, 1 = B on 2 = A on 0 = both on)

        // a and b off
        if (! a && ! b) return 3;

        // a and b on
        if (a && b) return 0;

        // a on and b off
        if (a) return 2;

        // a off and b on
        return 1;
    }
}
