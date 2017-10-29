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

package li.klass.fhem.service.intent.voice

import android.content.Context
import com.google.common.base.Joiner
import com.google.common.base.Optional
import com.google.common.base.Predicate
import com.google.common.collect.FluentIterable.from
import com.google.common.collect.ImmutableList
import com.google.common.collect.ImmutableMap
import com.google.common.collect.Iterables
import com.google.common.collect.Sets
import li.klass.fhem.domain.core.FhemDevice
import li.klass.fhem.domain.setlist.typeEntry.GroupSetListEntry
import li.klass.fhem.service.room.RoomListService
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VoiceCommandService @Inject
constructor(private val roomListService: RoomListService) {
    private val START_REPLACE = ImmutableMap.builder<String, String>()
            .put(COMMAND_START, "set").build()

    private val STATE_REPLACE = ImmutableMap.builder<String, String>()
            .put("an|[n]?ein|1", "on")
            .put("aus", "off").build()

    private val FILL_WORDS_TO_REPLACE = Sets.newHashSet("der", "die", "das", "den", "the", "doch", "bitte", "please")

    fun resultFor(voiceCommand: String, context: Context): Optional<VoiceResult> {
        var command = voiceCommand
        command = replaceArticles(command.toLowerCase(Locale.getDefault()))

        val parts = Arrays.asList(*command.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray())
        if (parts.isEmpty()) {
            return Optional.absent()
        }

        val shortcutResult = handleShortcut(parts, context)
        return if (shortcutResult.isPresent) {
            shortcutResult
        } else handleSetCommand(parts, context)

    }

    private fun handleShortcut(parts: List<String>, context: Context): Optional<VoiceResult> {
        val shortcut = shortcutCommandFor(parts[0])
        if (shortcut.isPresent && parts.size > 1) {
            val partsToSet = ImmutableList.builder<String>()
                    .add(SET_COMMAND_START)
                    .addAll(parts.subList(1, parts.size))
                    .add(shortcut.get())
                    .build()
            return handleSetCommand(partsToSet, context)
        }
        return Optional.absent()
    }

    private fun shortcutCommandFor(shortcut: String): Optional<String> =
            Optional.fromNullable(SHORTCUTS[shortcut])

    private fun handleSetCommand(parts: List<String>, context: Context): Optional<VoiceResult> {
        val starter = replace(parts[0], START_REPLACE)
        if (starter != SET_COMMAND_START || parts.size < 3) return Optional.absent()

        val deviceName = Joiner.on(" ").join(parts.subList(1, parts.size - 1))
        val state = replace(Iterables.getLast(parts), STATE_REPLACE)

        val devices = roomListService.getAllRoomsDeviceList(Optional.absent(), context)
        val deviceMatches = from(devices.allDevices).filter(filterDevicePredicate(deviceName, state)).toList()
        if (deviceMatches.isEmpty()) {
            return Optional.of(VoiceResult.Error(VoiceResult.ErrorType.NO_DEVICE_MATCHED))
        } else if (deviceMatches.size > 1) {
            return Optional.of(VoiceResult.Error(VoiceResult.ErrorType.MORE_THAN_ONE_DEVICE_MATCHES))
        }

        val device = deviceMatches[0]
        var targetState = device.getReverseEventMapStateFor(state)
        if (device.xmlListDevice.type == "LightScene") {
            targetState = "scene " + targetState
        }
        return Optional.of(VoiceResult.Success(device.name, targetState))
    }

    private fun replaceArticles(command: String): String {
        var replaced = command
        for (article in FILL_WORDS_TO_REPLACE) {
            replaced = replaced.replace(" $article ".toRegex(), " ")
        }
        return replaced
    }

    private fun filterDevicePredicate(spokenDeviceName: String, state: String): Predicate<FhemDevice> {
        return Predicate { device ->
            assert(device != null)

            val spokenName = sanitizeName(spokenDeviceName)

            val stateToLookFor = device!!.getReverseEventMapStateFor(state)
            val alias = sanitizeName(device.alias)
            val pronunciation = sanitizeName(device.pronunciation)
            val name = sanitizeName(device.name)

            (spokenName.equals(alias, ignoreCase = true)
                    || spokenName.equals(name, ignoreCase = true)
                    || spokenName.equals(pronunciation, ignoreCase = true)) && (device.setList.contains(stateToLookFor) || isLightSceneState(device, state))
        }
    }

    private fun isLightSceneState(device: FhemDevice, state: String): Boolean {
        val sceneEntry = device.setList.get("scene")
        return device.xmlListDevice.type == "LightScene"
                && sceneEntry is GroupSetListEntry && sceneEntry.groupStates.contains(state)
    }

    private fun sanitizeName(name: String?): String = name?.replace("[_\\.!? ]".toRegex(), "") ?: ""

    private fun replace(toReplace: String, replaceMap: Map<String, String>): String {
        var replaced = toReplace
        for ((key, value) in replaceMap) {
            replaced = replaced.replace(key.toRegex(), value)
        }
        return replaced
    }

    companion object {

        private val COMMAND_START = "schal[kt]e|switch|set"
        private val SET_COMMAND_START = "set"

        private val SHORTCUTS = ImmutableMap.builder<String, String>()
                .put("starte", "on")
                .put("beginne", "on")
                .put("start", "on")
                .put("begin", "on")
                .put("end", "off")
                .put("beende", "off")
                .put("stoppe", "off")
                .put("stop", "off")
                .build()
    }
}
