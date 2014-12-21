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

import javax.inject.Inject;
import javax.inject.Singleton;

import li.klass.fhem.domain.core.DimmableDevice;

/**
 * Class accumulating dimmable device specific operations. Changes will be executed using FHEM.
 */
@Singleton
public class DimmableDeviceService {

    @Inject
    GenericDeviceService genericDeviceService;

    /**
     * Dims a device.
     *
     * @param device      concerned device
     * @param dimProgress dim state to set. The progress will be matched against the available FS20 dim options.
     */
    public void dim(DimmableDevice device, int dimProgress) {
        if (!device.supportsDim()) return;
        String newState = device.getDimStateForPosition(dimProgress);

        genericDeviceService.setState(device, newState);
    }
}
