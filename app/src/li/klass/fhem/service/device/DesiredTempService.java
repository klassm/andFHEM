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

import li.klass.fhem.domain.DesiredTempDevice;
import li.klass.fhem.service.CommandExecutionService;

public class DesiredTempService {

    public static final DesiredTempService INSTANCE = new DesiredTempService();

    private static final String DESIRED_TEMP_COMMAND = "set %s %s %s";

    private DesiredTempService() {
    }

    /**
     * Sets the desired temperature. The action will only be executed if the new desired temperature is different to
     * the already set one.
     * @param device concerned device
     * @param desiredTemperatureToSet new desired temperature value
     */
    public void setDesiredTemperature(DesiredTempDevice device, double desiredTemperatureToSet) {
        String command = String.format(DESIRED_TEMP_COMMAND, device.getName(), device.getDesiredTempCommandFieldName(), desiredTemperatureToSet);
        if (desiredTemperatureToSet != device.getDesiredTemp()) {
            CommandExecutionService.INSTANCE.executeSafely(command);
            device.setDesiredTemp(desiredTemperatureToSet);
        }
    }
}
