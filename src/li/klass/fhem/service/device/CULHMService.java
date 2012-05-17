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

package li.klass.fhem.service.device;

import li.klass.fhem.domain.CULHMDevice;
import li.klass.fhem.service.CommandExecutionService;

public class CULHMService {
    public static final CULHMService INSTANCE = new CULHMService();

    private CULHMService() {
    }

    /**
     * Sets a specific state for the CULHM device.
     * @param device concerned device
     * @param newState state to set
     */
    public void setState(CULHMDevice device, String newState) {
        CommandExecutionService.INSTANCE.executeSafely("set " + device.getName() + " " + newState);
        device.setState(newState + " %");
    }

    /**
     * Dims an CUL_HM device.
     * @param device concerned device
     * @param dimProgress dim state to set (0-100)
     */
    public void dim(CULHMDevice device, int dimProgress) {
        if (device.getSubType() != CULHMDevice.SubType.DIMMER) {
            return;
        }
        setState(device, String.valueOf(dimProgress));
        device.setDimProgress(dimProgress);
    }

    /**
     * Toggles the state of an CUL_HM device.
     * @param device concerned device
     */
    public void toggleState(CULHMDevice device) {
        if (device.getSubType() != CULHMDevice.SubType.SWITCH) {
            return;
        }
        if (device.isOn()) {
            setState(device, "off");
        } else {
            setState(device, "on");
        }
    }
}
