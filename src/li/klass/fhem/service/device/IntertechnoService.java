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

import li.klass.fhem.domain.IntertechnoDevice;
import li.klass.fhem.service.CommandExecutionService;

public class IntertechnoService {
    public static final IntertechnoService INSTANCE = new IntertechnoService();

    private IntertechnoService() {
    }

    /**
     * Sets a specific state for the device.
     * @param device concerned device
     * @param newState state to set
     */
    public void setState(IntertechnoDevice device, String newState) {
        CommandExecutionService.INSTANCE.executeSafely("set " + device.getName() + " " + newState);
        device.setState(newState);
    }

    /**
     * Toggles the state of a device.
     * @param device concerned device
     */
    public void toggleState(IntertechnoDevice device) {
        if (device.isOn()) {
            setState(device, "off");
        } else {
            setState(device, "on");
        }
    }
}
