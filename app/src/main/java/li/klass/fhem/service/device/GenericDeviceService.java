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
import android.util.Log;
import android.util.Xml;

import org.apache.commons.lang3.ArrayUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import javax.inject.Inject;
import javax.inject.Singleton;

import li.klass.fhem.dagger.ForApplication;
import li.klass.fhem.domain.core.Device;
import li.klass.fhem.domain.core.XmllistAttribute;
import li.klass.fhem.service.CommandExecutionService;
import li.klass.fhem.util.ArrayUtil;
import li.klass.fhem.util.ReflectionUtil;
import li.klass.fhem.util.Tasker;

@Singleton
public class GenericDeviceService {

    @Inject
    CommandExecutionService commandExecutionService;

    @Inject
    @ForApplication
    Context applicationContext;

    public void setState(Device<?> device, String targetState) {
        targetState = device.formatTargetState(targetState);

        commandExecutionService.executeSafely("set " + device.getName() + " " + targetState);

        if (device.shouldUpdateStateOnDevice(targetState)) {
            device.setState(device.formatStateTextToSet(targetState));
        }

        Tasker.sendTaskerNotifyIntent(applicationContext, device.getName(),
                "state", targetState);
    }

    public void setSubState(Device<?> device, String subStateName, String value) {
        commandExecutionService.executeSafely("set " + device.getName() + " " + subStateName + " " + value);
        Tasker.sendTaskerNotifyIntent(applicationContext, device.getName(),
                subStateName, value);

        invokeDeviceUpdateFor(device, subStateName, value);
    }

    private void invokeDeviceUpdateFor(Device<?> device, String subStateName, String value) {

        Class<? extends Device> clazz = device.getClass();
        if (!invokeDeviceUpdateForMethods(device, subStateName, value, clazz)) {
            invokeDeviceUpdateForFields(device, subStateName, value, clazz);
        }
    }

    private boolean invokeDeviceUpdateForMethods(Device<?> device, String subStateName, String value, Class<? extends Device> clazz) {
        for (Method method : clazz.getMethods()) {
            if (invokeDeviceUpdateForMethod(device, subStateName, value, clazz, method)) {
                return true;
            }
        }
        return false;
    }

    private boolean invokeDeviceUpdateForMethod(Device<?> device, String subStateName, String value, Class<? extends Device> clazz, Method method) {
        try {
            if (method.getParameterTypes().length != 1 || !method.getParameterTypes()[0].isAssignableFrom(String.class)) {
                return false;
            }
            if (method.isAnnotationPresent(XmllistAttribute.class)) {
                String[] attributeValues = method.getAnnotation(XmllistAttribute.class).value();
                for (String attributeValue : attributeValues) {
                    if (attributeValue.equalsIgnoreCase(subStateName)) {
                        method.invoke(device, value);
                        return true;
                    }
                }
            } else if (method.getName().equalsIgnoreCase("read" + subStateName)) {
                method.invoke(device, value);
                return true;
            }
        } catch (Exception e) {
            Log.e(GenericDeviceService.class.getName(), "error during invoke of " + method.getName() + " for device " + clazz.getSimpleName(), e);
        }
        return false;
    }

    private boolean invokeDeviceUpdateForFields(Device<?> device, String subStateName, String value, Class<? extends Device> clazz) {
        for (Field field : clazz.getDeclaredFields()) {
            if (invokeDeviceUpdateForField(device, subStateName, value, clazz, field)) {
                return true;
            }
        }
        return false;
    }

    private boolean invokeDeviceUpdateForField(Device<?> device, String subStateName, String value, Class<? extends Device> clazz, Field field) {
        XmllistAttribute annotation = field.getAnnotation(XmllistAttribute.class);
        if (annotation == null || !ArrayUtil.contains(annotation.value(), subStateName))
            return false;

        field.setAccessible(true);
        ReflectionUtil.setFieldValue(device, field, value);
        return true;
    }
}
