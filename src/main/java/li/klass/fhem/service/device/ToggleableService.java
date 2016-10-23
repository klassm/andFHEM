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

import com.google.common.base.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;

import li.klass.fhem.adapter.devices.hook.DeviceHookProvider;
import li.klass.fhem.adapter.devices.toggle.OnOffBehavior;
import li.klass.fhem.domain.core.ToggleableDevice;
import li.klass.fhem.service.CommandExecutionService;

@Singleton
public class ToggleableService {
    @Inject
    CommandExecutionService commandExecutionService;

    @Inject
    DeviceHookProvider deviceHookProvider;

    @Inject
    OnOffBehavior onOffBehavior;

    @Inject
    public ToggleableService() {
    }

    /**
     * Toggles the state of a toggleable device.
     *
     * @param device  concerned device
     * @param context context
     */
    public <D extends ToggleableDevice> void toggleState(D device, Context context) {
        if (onOffBehavior.isOnByState(device)) {
            commandExecutionService.executeSafely("set " + device.getName() + " " + deviceHookProvider.getOffStateName(device), Optional.<String>absent(), context);
            device.setState(device.getOffStateName());
        } else {
            commandExecutionService.executeSafely("set " + device.getName() + " " + deviceHookProvider.getOnStateName(device), Optional.<String>absent(), context);
            device.setState(device.getOnStateName());
        }
    }
}
