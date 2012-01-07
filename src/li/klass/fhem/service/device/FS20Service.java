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

import android.content.Context;
import li.klass.fhem.domain.FS20Device;
import li.klass.fhem.service.CommandExecutionService;
import li.klass.fhem.service.ExecuteOnSuccess;

public class FS20Service {
    public static final FS20Service INSTANCE = new FS20Service();

    private FS20Service() {
    }
    
    public void setState(Context context, FS20Device device, String newState, ExecuteOnSuccess executeOnSuccess) {
        CommandExecutionService.INSTANCE.executeSafely(context, "set " + device.getName() + " " + newState, executeOnSuccess);
        device.setState(newState);
    }

    public void toggleState(Context context, FS20Device fs20Device, ExecuteOnSuccess executeOnSuccess) {
        if (fs20Device.isOn()) {
            setState(context, fs20Device, "off", executeOnSuccess);
        } else {
            setState(context, fs20Device, "on", executeOnSuccess);
        }
    }

    public void dim(Context context, FS20Device fs20Device, int dimProgress, ExecuteOnSuccess executeOnSuccess) {
        if (! fs20Device.isDimDevice()) return;
        int bestMatch = fs20Device.getBestDimMatchFor(dimProgress);

        String newState;
        if (bestMatch == 0)
            newState = "off";
        else {
            newState = "dim" + String.format("%02d", bestMatch) + "%";
        }

        setState(context, fs20Device, newState, executeOnSuccess);
    }
}
