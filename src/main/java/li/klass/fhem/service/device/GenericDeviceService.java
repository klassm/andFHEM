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

import android.content.*;
import android.support.annotation.NonNull;
import com.google.common.base.*;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import li.klass.fhem.domain.core.FhemDevice;
import li.klass.fhem.service.CommandExecutionService;
import li.klass.fhem.service.deviceConfiguration.DeviceConfiguration;
import li.klass.fhem.service.intent.RoomListUpdateIntentService;
import li.klass.fhem.service.room.RoomListUpdateService;
import li.klass.fhem.util.*;

import javax.inject.*;
import java.util.*;

import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Iterables.partition;
import static li.klass.fhem.constants.Actions.DO_REMOTE_UPDATE;
import static li.klass.fhem.constants.BundleExtraKeys.*;
import static li.klass.fhem.service.deviceConfiguration.DeviceConfiguration.TO_DELAY_FOR_UPDATE_AFTER_COMMAND;

@Singleton
public class GenericDeviceService {
    private static final Function<List<StateToSet>, String> FHT_CONCAT = new Function<List<StateToSet>, String>() {
        @Override
        public String apply(List<StateToSet> input) {
            return fhtConcat(input);
        }

        private String fhtConcat(List<StateToSet> input) {
            return from(input).transform(input1 -> input1.getKey() + " " + input1.getValue()).join(Joiner.on(" "));
        }
    };

    @Inject
    CommandExecutionService commandExecutionService;

    @Inject
    RoomListUpdateService roomListUpdateService;

    @Inject
    public GenericDeviceService() {
    }

    public void setState(FhemDevice<?> device, String targetState, Optional<String> connectionId, Context context) {
        setState(device, targetState, connectionId, context, true);
    }

    public void setState(final FhemDevice<?> device, String targetState, Optional<String> connectionId, final Context context, final boolean invokeUpdate) {
        final String toSet = device.formatTargetState(targetState);

        commandExecutionService.executeSafely("set " + device.getName() + " " + toSet, connectionId, context, invokePostCommandActions(device, context, invokeUpdate, "state", toSet, connectionId));
    }

    public void setSubState(FhemDevice<?> device, String subStateName, String value, Optional<String> connectionId, Context context, boolean invokeDeviceUpdate) {
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

        commandExecutionService.executeSafely("set " + device.getName() + " " + subStateName + " " + value, connectionId, context,
                invokePostCommandActions(device, context, invokeDeviceUpdate, subStateName, value, connectionId));
    }

    @NonNull
    private CommandExecutionService.ResultListener invokePostCommandActions(final FhemDevice<?> device, final Context context, final boolean invokeUpdate, final String stateName, final String toSet, Optional<String> connectionId) {
        return new CommandExecutionService.SuccessfulResultListener() {
            @Override
            public void onResult(String result) {

                if (invokeUpdate) {
                    update(device, context, connectionId);
                }

                Tasker.sendTaskerNotifyIntent(context, device.getName(), stateName, toSet);
                Tasker.requestQuery(context);
                device.getXmlListDevice().setState(stateName, toSet);
            }
        };
    }

    public void setSubStates(FhemDevice device, List<StateToSet> statesToSet, Optional<String> connectionId, Context context) {
        if ("FHT".equalsIgnoreCase(device.getXmlListDevice().getType()) && statesToSet.size() > 1) {
            setSubStatesForFHT(device, statesToSet, connectionId, context);
        } else {
            for (StateToSet toSet : statesToSet) {
                setSubState(device, toSet.getKey(), toSet.getValue(), connectionId, context, false);
            }
        }
        update(device, context, connectionId);
    }

    private void setSubStatesForFHT(FhemDevice device, List<StateToSet> statesToSet, Optional<String> connectionId, Context context) {
        Iterable<List<StateToSet>> partitions = partition(statesToSet, 8);
        ImmutableList<String> parts = from(partitions).transform(FHT_CONCAT).toList();
        for (String toSet : parts) {
            setState(device, toSet, connectionId, context, false);
        }
    }

    public void update(FhemDevice<?> device, final Context context, Optional<String> connectionId) {
        Integer delay = device.getDeviceConfiguration().transform(TO_DELAY_FOR_UPDATE_AFTER_COMMAND).or(0);
        context.startService(new Intent(DO_REMOTE_UPDATE)
                .putExtra(DEVICE_NAME, device.getName())
                .putExtra(DELAY, delay)
                .putExtra(CONNECTION_ID, connectionId.orNull())
                .setClass(context, RoomListUpdateIntentService.class));
    }
}
