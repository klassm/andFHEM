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

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import li.klass.fhem.domain.core.FhemDevice;
import li.klass.fhem.service.CommandExecutionService;
import li.klass.fhem.service.deviceConfiguration.DeviceConfiguration;
import li.klass.fhem.service.room.RoomListUpdateService;
import li.klass.fhem.util.StateToSet;
import li.klass.fhem.util.Tasker;

import static com.google.common.collect.FluentIterable.from;

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
    RoomListUpdateService roomListUpdateService;

    @Inject
    public GenericDeviceService() {
    }

    public void setState(FhemDevice<?> device, String targetState, Context context) {
        setState(device, targetState, context, true);
    }

    public void setState(FhemDevice<?> device, String targetState, Context context, boolean invokeUpdate) {
        targetState = device.formatTargetState(targetState);

        commandExecutionService.executeSafely("set " + device.getName() + " " + targetState, context);

        if (invokeUpdate) {
            update(device, context);
        }

        Tasker.sendTaskerNotifyIntent(context, device.getName(), "state", targetState);
        Tasker.requestQuery(context);
    }

    public void setSubState(FhemDevice<?> device, String subStateName, String value, Context context, boolean invokeDeviceUpdate) {
        if (device.getDeviceConfiguration().isPresent()) {
            DeviceConfiguration configuration = device.getDeviceConfiguration().get();
            Map<String, String> toReplace = configuration.getCommandReplaceFor(subStateName);
            for (Map.Entry<String, String> entry : toReplace.entrySet()) {
                value = value
                        .replaceAll("([ ,])" + entry.getKey(), "$1" + entry.getValue())
                        .replaceAll("^" + entry.getKey(), entry.getValue());
            }
            Optional<String> subStateReplaceForSubState = configuration.getSubStateReplaceFor(subStateName);
            if (subStateReplaceForSubState.isPresent()) {
                subStateName = subStateReplaceForSubState.get();
            }
        }


        if ("STATE".equalsIgnoreCase(subStateName)) {
            setState(device, value, context);
            return;
        }
        commandExecutionService.executeSafely("set " + device.getName() + " " + subStateName + " " + value, context);
        if (invokeDeviceUpdate) {
            update(device, context);
        }
        Tasker.sendTaskerNotifyIntent(context, device.getName(), subStateName, value);
        Tasker.requestQuery(context);
    }

    public void setSubStates(FhemDevice device, List<StateToSet> statesToSet, Context context) {
        if (device.getXmlListDevice().getType().equalsIgnoreCase("FHT") && statesToSet.size() > 1) {
            Iterable<List<StateToSet>> partitions = Iterables.partition(statesToSet, 8);
            ImmutableList<String> parts = from(partitions).transform(FHT_CONCAT).toList();
            for (String toSet : parts) {
                setState(device, toSet, context, false);
            }
            update(device, context);
        } else {
            for (StateToSet toSet : statesToSet) {
                setSubState(device, toSet.getKey(), toSet.getValue(), context, false);
            }
            update(device, context);
        }
    }

    private boolean update(FhemDevice<?> device, Context context) {
        return roomListUpdateService.updateSingleDevice(device.getName(), context);
    }
}
