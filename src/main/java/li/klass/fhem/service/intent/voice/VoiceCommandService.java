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

package li.klass.fhem.service.intent.voice;

import android.content.Context;
import com.google.common.base.*;
import com.google.common.base.Optional;
import com.google.common.collect.*;
import li.klass.fhem.domain.LightSceneDevice;
import li.klass.fhem.domain.core.*;
import li.klass.fhem.service.room.RoomListService;

import javax.inject.*;
import java.util.*;

import static com.google.common.collect.FluentIterable.from;

@Singleton
public class VoiceCommandService {

    private static final String COMMAND_START = "schal[kt]e|switch|set";
    private static final String SET_COMMAND_START = "set";
    private Map<String, String> START_REPLACE = ImmutableMap.<String, String>builder()
            .put(COMMAND_START, "set").build();

    private Map<String, String> STATE_REPLACE = ImmutableMap.<String, String>builder()
            .put("an|[n]?ein|1", "on")
            .put("aus", "off").build();

    private static final Map<String, String> SHORTCUTS = ImmutableMap.<String, String>builder()
            .put("starte", "on")
            .put("beginne", "on")
            .put("start", "on")
            .put("begin", "on")
            .put("end", "off")
            .put("beende", "off")
            .put("stoppe", "off")
            .put("stop", "off")
            .build();

    private Set<String> FILL_WORDS_TO_REPLACE = Sets.newHashSet("der", "die", "das", "den", "the", "doch", "bitte", "please");

    private RoomListService roomListService;

    @Inject
    public VoiceCommandService(RoomListService roomListService) {
        this.roomListService = roomListService;
    }

    public Optional<VoiceResult> resultFor(String voiceCommand, Context context) {
        voiceCommand = replaceArticles(voiceCommand.toLowerCase(Locale.getDefault()));

        List<String> parts = Arrays.asList(voiceCommand.split(" "));
        if (parts.isEmpty()) {
            return Optional.absent();
        }

        Optional<VoiceResult> shortcutResult = handleShortcut(parts, context);
        if (shortcutResult.isPresent()) {
            return shortcutResult;
        }

        return handleSetCommand(parts, context);
    }

    private Optional<VoiceResult> handleShortcut(List<String> parts, Context context) {
        Optional<String> shortcut = shortcutCommandFor(parts.get(0));
        if (shortcut.isPresent() && parts.size() > 1) {
            ImmutableList<String> partsToSet = ImmutableList.<String>builder()
                    .add(SET_COMMAND_START)
                    .addAll(parts.subList(1, parts.size()))
                    .add(shortcut.get())
                    .build();
            return handleSetCommand(partsToSet, context);
        }
        return Optional.absent();
    }

    private Optional<String> shortcutCommandFor(String shortcut) {
        return Optional.fromNullable(SHORTCUTS.get(shortcut));
    }

    private Optional<VoiceResult> handleSetCommand(List<String> parts, Context context) {
        String starter = replace(parts.get(0), START_REPLACE);
        if (!starter.equals(SET_COMMAND_START) || parts.size() < 3) return Optional.absent();

        final String deviceName = Joiner.on(" ").join(parts.subList(1, parts.size() - 1));
        final String state = replace(Iterables.getLast(parts), STATE_REPLACE);

        RoomDeviceList devices = roomListService.getAllRoomsDeviceList(Optional.<String>absent(), context);
        List<FhemDevice> deviceMatches = from(devices.getAllDevices()).filter(filterDevicePredicate(deviceName, state)).toList();
        if (deviceMatches.isEmpty()) {
            return Optional.<VoiceResult>of(new VoiceResult.Error(VoiceResult.ErrorType.NO_DEVICE_MATCHED));
        } else if (deviceMatches.size() > 1) {
            return Optional.<VoiceResult>of(new VoiceResult.Error(VoiceResult.ErrorType.MORE_THAN_ONE_DEVICE_MATCHES));
        }

        FhemDevice device = deviceMatches.get(0);
        String targetState = device.getReverseEventMapStateFor(state);
        if (device instanceof LightSceneDevice) {
            targetState = "scene " + targetState;
        }
        return Optional.<VoiceResult>of(new VoiceResult.Success(device.getName(), targetState));
    }

    private String replaceArticles(String command) {
        for (String article : FILL_WORDS_TO_REPLACE) {
            command = command.replaceAll(" " + article + " ", " ");
        }
        return command;
    }

    private Predicate<FhemDevice> filterDevicePredicate(final String spokenDeviceName, final String state) {
        return new Predicate<FhemDevice>() {
            @Override
            public boolean apply(FhemDevice device) {
                assert device != null;

                String spokenName = sanitizeName(spokenDeviceName);

                String stateToLookFor = device.getReverseEventMapStateFor(state);
                String alias = sanitizeName(device.getAlias());
                String pronunciation = sanitizeName(device.getPronunciation());
                String name = sanitizeName(device.getName());

                return (spokenName.equalsIgnoreCase(alias)
                        || spokenName.equalsIgnoreCase(name)
                        || (spokenName.equalsIgnoreCase(pronunciation)))
                        && (device.getSetList().contains(stateToLookFor)
                        || (device instanceof LightSceneDevice && ((LightSceneDevice) device).getScenes().contains(state)));
            }
        };
    }

    private String sanitizeName(String name) {
        return name == null
                ? ""
                : name.replaceAll("[_\\.!? ]", "");
    }

    private String replace(String in, Map<String, String> toReplace) {
        for (Map.Entry<String, String> entry : toReplace.entrySet()) {
            in = in.replaceAll(entry.getKey(), entry.getValue());
        }
        return in;
    }
}
