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

import android.util.Log;
import li.klass.fhem.domain.core.Device;
import li.klass.fhem.domain.floorplan.Coordinate;
import li.klass.fhem.domain.floorplan.FloorplanPosition;
import li.klass.fhem.service.CommandExecutionService;

public class FloorplanService {
    public static final FloorplanService INSTANCE = new FloorplanService();

    private FloorplanService() {
    }

    public void setDeviceLocation(String floorplanName, Device<?> device, Coordinate newCoordinate) {

        int x = Math.round(newCoordinate.x);
        int y = Math.round(newCoordinate.y);

        FloorplanPosition oldPosition = device.getFloorplanPositionFor(floorplanName);

        String command = "attr " + device.getName() + " fp_" + floorplanName + " " + y + "," + x + "," + oldPosition.viewType + ",";
        Log.e(FloorplanService.class.getName(), command);
        CommandExecutionService.INSTANCE.executeSafely(command);

        device.setCoordinateFor(floorplanName, newCoordinate);
    }
}
