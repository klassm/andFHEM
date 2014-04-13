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

@SuppressWarnings("unused")
@OverviewViewSettings(showState = true)
public class IntertechnoDevice extends ToggleableDevice<IntertechnoDevice> {

    @ShowField(description = ResourceIdMapper.model)
    private String model;

    public void readMODEL(String value) {
        this.model = value;
    }

    public boolean isOnByState() {
        if (super.isOnByState()) return true;

        return getState().equals("on");
    }

    @Override
    public boolean supportsToggle() {
        return true;
    }

    public String getModel() {
        return model;
    }

    @Override
    public DeviceFunctionality getDeviceGroup() {
        return DeviceFunctionality.SWITCH;
    }
}
