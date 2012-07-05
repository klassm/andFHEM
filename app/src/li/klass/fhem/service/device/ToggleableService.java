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

package li.klass.fhem.service.device;

import li.klass.fhem.domain.core.ToggleableDevice;
import li.klass.fhem.service.CommandExecutionService;

public class ToggleableService {
    public static final ToggleableService INSTANCE = new ToggleableService();

    private ToggleableService() {
    }

    /**
     * Toggles the state of a HOL device.
     * @param device concerned device
     */
    public <D extends ToggleableDevice> void toggleState(D device) {
        if (device.isOn()) {
            CommandExecutionService.INSTANCE.executeSafely("set " + device.getName() + " off");
            device.setState("off");
        } else {
            CommandExecutionService.INSTANCE.executeSafely("set " + device.getName() + " on");
            device.setState("on");
        }
    }
}
