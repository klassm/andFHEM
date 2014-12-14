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
import li.klass.fhem.domain.core.Device;
import li.klass.fhem.domain.core.DeviceFunctionality;
import li.klass.fhem.domain.genericview.ShowField;

public class LGTVDevice extends Device<LGTVDevice> {
    @ShowField(description = ResourceIdMapper.power, showInOverview = true)
    private String power;
    @ShowField(description = ResourceIdMapper.audio, showInOverview = true)
    private String audio;
    @ShowField(description = ResourceIdMapper.input, showInOverview = true)
    private String input;

    public void readPOWER(String value) {
        this.power = value;
    }

    public void readAUDIO(String value) {
        this.audio = value;
    }

    public void readINPUT(String value) {
        this.input = value;
    }

    public String getPower() {
        return power;
    }

    public String getAudio() {
        return audio;
    }

    public String getInput() {
        return input;
    }

    @Override
    public DeviceFunctionality getDeviceGroup() {
        return DeviceFunctionality.REMOTE_CONTROL;
    }
}
