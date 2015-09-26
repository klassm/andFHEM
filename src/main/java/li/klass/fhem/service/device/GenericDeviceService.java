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

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import li.klass.fhem.domain.core.FhemDevice;
import li.klass.fhem.domain.core.XmllistAttribute;
import li.klass.fhem.service.CommandExecutionService;
import li.klass.fhem.util.ArrayUtil;
import li.klass.fhem.util.ReflectionUtil;
import li.klass.fhem.util.StateToSet;
import li.klass.fhem.util.Tasker;

import static com.google.common.collect.FluentIterable.from;
import static li.klass.fhem.behavior.dim.ContinuousDimmableBehavior.DIM_ATTRIBUTES;

@Singleton
public class GenericDeviceService {
    public static final Function<List<StateToSet>, String> FHT_CONCAT = new Function<List<StateToSet>, String>() {
        @Override
        public String apply(List<StateToSet> input) {
            return fhtConcat(input);
        }

        private String fhtConcat(List<StateToSet> input) {
            return from(input).transform(new Function<StateToSet, String>() {
                @Override
                public String apply(StateToSet input) {
                    return input.getKey() + " " + input.getValue();
                }
            }).join(Joiner.on(" "));
        }
    };
    @Inject
    CommandExecutionService commandExecutionService;

    @Inject
    public GenericDeviceService() {
    }

    public void setState(FhemDevice<?> device, String targetState, Context context) {
        setState(device, targetState, context, true);
    }

    public void setState(FhemDevice<?> device, String targetState, Context context, boolean invokeUpdate) {
        targetState = device.formatTargetState(targetState);

        commandExecutionService.executeSafely("set " + getInternalName(device) + " " + targetState, context);

        if (!invokeUpdate) {
            return;
        }

        if (device.shouldUpdateStateOnDevice(targetState)) {
            device.setState(device.formatStateTextToSet(targetState));
        }

        Tasker.sendTaskerNotifyIntent(context, device.getName(),
                "state", targetState);
    }

    public void setSubState(FhemDevice<?> device, String subStateName, String value, Context context) {
        commandExecutionService.executeSafely("set " + getInternalName(device) + " " + subStateName + " " + value, context);
        Tasker.sendTaskerNotifyIntent(context, device.getName(),
                subStateName, value);

        if (DIM_ATTRIBUTES.contains(subStateName)) {
            device.setState(value);
        } else {
            invokeDeviceUpdateFor(device, subStateName, value);
        }
    }

    public void setSubStates(FhemDevice device, List<StateToSet> statesToSet, Context context) {
        if (device.getXmlListDevice().getType().equalsIgnoreCase("FHT") && statesToSet.size() > 1) {
            Iterable<List<StateToSet>> partitions = Iterables.partition(statesToSet, 8);
            ImmutableList<String> parts = from(partitions).transform(FHT_CONCAT).toList();
            for (String toSet : parts) {
                setState(device, toSet, context, false);
            }
            for (StateToSet toSet : statesToSet) {
                invokeDeviceUpdateFor(device, toSet.getKey(), toSet.getValue());
            }
        } else {
            for (StateToSet toSet : statesToSet) {
                setSubState(device, toSet.getKey(), toSet.getValue(), context);
            }
        }
    }

    private String getInternalName(FhemDevice<?> device) {
        return device.getXmlListDevice().getInternals().get("NAME").getValue();
    }

    private void invokeDeviceUpdateFor(FhemDevice<?> device, String subStateName, String value) {

        device.getXmlListDevice().setState(subStateName, value);
        Class<? extends FhemDevice> clazz = device.getClass();
        if (!invokeDeviceUpdateForMethods(device, subStateName, value, clazz)) {
            invokeDeviceUpdateForFields(device, subStateName, value, clazz);
        }
    }

    private boolean invokeDeviceUpdateForMethods(FhemDevice<?> device, String subStateName, String value, Class<? extends FhemDevice> clazz) {
        for (Method method : clazz.getMethods()) {
            if (invokeDeviceUpdateForMethod(device, subStateName, value, clazz, method)) {
                return true;
            }
        }
        return false;
    }

    private boolean invokeDeviceUpdateForMethod(FhemDevice<?> device, String subStateName, String value, Class<? extends FhemDevice> clazz, Method method) {
        try {
            if (method.getParameterTypes().length == 0 || !method.getParameterTypes()[0].isAssignableFrom(String.class)) {
                return false;
            }

            if (method.isAnnotationPresent(XmllistAttribute.class)) {
                String[] attributeValues = method.getAnnotation(XmllistAttribute.class).value();
                for (String attributeValue : attributeValues) {
                    if (attributeValue.equalsIgnoreCase(subStateName)) {
                        Object[] params = new Object[method.getParameterTypes().length];
                        params[0] = value;
                        method.invoke(device, params);
                        return true;
                    }
                }
            } else if (method.getName().equalsIgnoreCase("read" + subStateName)
                    && method.getParameterTypes().length == 1) {
                method.invoke(device, value);
                return true;
            }
        } catch (Exception e) {
            Log.e(GenericDeviceService.class.getName(), "error during invoke of " + method.getName() + " for device " + clazz.getSimpleName(), e);
        }
        return false;
    }

    private boolean invokeDeviceUpdateForFields(FhemDevice<?> device, String subStateName, String value, Class<? extends FhemDevice> clazz) {
        for (Field field : clazz.getDeclaredFields()) {
            if (invokeDeviceUpdateForField(device, subStateName, value, field)) {
                return true;
            }
        }
        return false;
    }

    private boolean invokeDeviceUpdateForField(FhemDevice<?> device, String subStateName, String value, Field field) {
        XmllistAttribute annotation = field.getAnnotation(XmllistAttribute.class);
        if (annotation == null || !ArrayUtil.containsIgnoreCase(annotation.value(), subStateName)) {
            return false;
        }

        field.setAccessible(true);
        ReflectionUtil.setFieldValue(device, field, value);
        return true;
    }
}
