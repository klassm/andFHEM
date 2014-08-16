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

import android.util.Log;

import java.lang.reflect.Method;

import li.klass.fhem.AndFHEMApplication;
import li.klass.fhem.domain.core.Device;
import li.klass.fhem.domain.core.XmllistAttribute;
import li.klass.fhem.service.CommandExecutionService;
import li.klass.fhem.util.Tasker;

public class GenericDeviceService {
    public static final GenericDeviceService INSTANCE = new GenericDeviceService();

    private GenericDeviceService() {
    }

    public void setState(Device<?> device, String targetState) {
        targetState = device.formatTargetState(targetState);

        CommandExecutionService.INSTANCE.executeSafely("set " + device.getName() + " " + targetState);

        if (device.shouldUpdateStateOnDevice(targetState)) {
            device.setState(device.formatStateTextToSet(targetState));
        }

        Tasker.sendTaskerNotifyIntent(AndFHEMApplication.getContext(), device.getName(),
                "state", targetState);
    }

    public void setSubState(Device<?> device, String subStateName, String value) {
        CommandExecutionService.INSTANCE.executeSafely("set " + device.getName() + " " + subStateName + " " + value);
        Tasker.sendTaskerNotifyIntent(AndFHEMApplication.getContext(), device.getName(),
                subStateName, value);

        invokeDeviceUpdateFor(device, subStateName, value);
    }

    private void invokeDeviceUpdateFor(Device<?> device, String subStateName, String value) {

        Class<? extends Device> clazz = device.getClass();
        for (Method method : clazz.getMethods()) {
            try {
                if (method.getParameterTypes().length == 1 && method.getParameterTypes()[0].isAssignableFrom(String.class)) {
                    if (method.isAnnotationPresent(XmllistAttribute.class)) {
                        if (method.getAnnotation(XmllistAttribute.class).value().equalsIgnoreCase(subStateName)) {
                            method.invoke(device, value);
                        }
                    } else if (method.getName().equalsIgnoreCase("read" + subStateName)) {
                        method.invoke(device, value);
                    }
                }
            } catch (Exception e) {
                Log.e(GenericDeviceService.class.getName(), "error during invoke of " + method.getName() + " for device " + clazz.getSimpleName(), e);
            }
        }

    }
}
