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

package li.klass.fhem.devices.backend.at

import com.google.common.base.Strings
import li.klass.fhem.util.NumberUtil.toTwoDecimalDigits
import org.apache.commons.lang3.StringUtils.trimToNull
import org.joda.time.LocalTime
import org.joda.time.format.DateTimeFormat
import org.slf4j.LoggerFactory
import java.util.*
import java.util.regex.Pattern
import javax.inject.Inject

class AtDefinitionParser @Inject constructor() {
    fun parse(definition: String): TimerDefinition? {
        val replaced = definition.replace("[\\d]{4}-[\\d]{2}-[\\d]{2}T".toRegex(), "")
        val prefixMatcher = PREFIX_PATTERN.matcher(replaced)
        if (!prefixMatcher.matches()) return null

        val rest = prefixMatcher.group(3).trim()
        val parsedContent = parseDeviceSwitchContent(rest) ?: return null

        val prefix = prefixMatcher.group(1)
        val timerType = extractTimerTypeFrom(prefix)
        val repetition = parsedContent.repetition ?: extractRepetitionFrom(prefix)

        val dateContent = prefixMatcher.group(2)
        val switchTime = parseDateContent(dateContent) ?: return null


        return TimerDefinition(
                switchTime = switchTime,
                repetition = parsedContent.repetition ?: repetition,
                type = timerType,
                targetDeviceName = parsedContent.targetDevice,
                targetState = parsedContent.targetState,
                targetStateAppendix = parsedContent.targetStateAppendix
        )
    }

    private fun parseDateContent(switchTime: String): LocalTime? {
        val validatedTime = if (switchTime.length < "00:00:00".length) {
            switchTime + ":00"
        } else switchTime
        return try {
            val date = DATE_TIME_FORMAT.parseDateTime(validatedTime)
            LocalTime(date.hourOfDay, date.minuteOfHour, date.secondOfMinute)
        } catch (e: Exception) {
            logger.error("parseDateContent(switchTime=$switchTime) - cannot parse: ${e.message}")
            null
        }
    }


    private fun parseDeviceSwitchContent(rest: String): ParsedSwitchContent? {
        val replacedRest = rest.replace("[{}]".toRegex(), "").trim()
        if (replacedRest.startsWith("fhem")) {
            val fhemMatcher = FHEM_PATTERN.matcher(replacedRest)
            if (!fhemMatcher.matches()) return null

            val targetDevice = fhemMatcher.group(1)
            val targetState = fhemMatcher.group(2)
            val targetStateAddtionalInformation = fhemMatcher.group(3)

            val fhemRest = fhemMatcher.group(4).trim { it <= ' ' }.toLowerCase(Locale.getDefault())
            val ifPattern = Pattern.compile("if[ ]?\\(([^)]+)\\)")
            val ifMatcher = ifPattern.matcher(fhemRest)

            val repetition = if (ifMatcher.find()) {
                val ifContent = ifMatcher.group(1)
                val parts = ifContent.split("&&".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

                parts.map { it.trim() }
                        .map { extractRepetitionFromIf(it) }.firstOrNull { it != null }
            } else null

            return ParsedSwitchContent(targetDevice, targetState, targetStateAddtionalInformation, repetition)
        } else {
            val matcher = DEFAULT_PATTERN.matcher(replacedRest)
            if (!matcher.matches()) return null

            val targetDevice = trimToNull(matcher.group(1))
            val targetState = trimToNull(matcher.group(2))
            val targetStateAddtionalInformation = trimToNull(matcher.group(3))

            return ParsedSwitchContent(targetDevice, targetState, targetStateAddtionalInformation, null)
        }
    }

    private fun extractRepetitionFromIf(part: String): AtRepetition? = when {
        part == "\$we" -> AtRepetition.WEEKEND
        part.matches("(NOT|not|!)[ ]?\\\$we".toRegex()) -> AtRepetition.WEEKDAY
        part.matches("\\\$wday[ ]?==[ ]?[0-6]".toRegex()) -> {
            val weekdayOrdinate = Integer.parseInt(part.substring(part.length - 1))
            AtRepetition.getRepetitionForWeekdayOrdinate(weekdayOrdinate)
        }
        else -> null
    }

    private fun extractTimerTypeFrom(prefix: String): TimerType =
            if (prefix.contains("+")) TimerType.RELATIVE else TimerType.ABSOLUTE

    private fun extractRepetitionFrom(prefix: String): AtRepetition =
            if (prefix.contains("*")) AtRepetition.EVERY_DAY else AtRepetition.ONCE

    fun toFHEMDefinition(definition: TimerDefinition): String {
        var command = ""
        if (definition.type == TimerType.RELATIVE) {
            command += "+"
        }
        if (definition.repetition != AtRepetition.ONCE) {
            command += "*"
        }

        command += getFormattedSwitchTime(definition)

        command += " { fhem(\"set ${definition.targetDeviceName} ${definition.targetState}"
        if (definition.targetStateAppendix?.isNotBlank() == true) {
            command += " " + definition.targetStateAppendix.trim()
        }
        command += "\")"

        var ifContent = ""
        when {
            definition.repetition == AtRepetition.WEEKEND -> ifContent = addToIf(ifContent, "\$we")
            definition.repetition == AtRepetition.WEEKDAY -> ifContent = addToIf(ifContent, "!\$we")
            definition.repetition.weekdayOrdinate != -1 -> ifContent = addToIf(ifContent, "\$wday == " + definition.repetition.weekdayOrdinate)
        }

        if (!Strings.isNullOrEmpty(ifContent)) {
            command += " if ($ifContent)"
        }

        command += " }"

        return command.trim { it <= ' ' }
    }

    private fun addToIf(ifContent: String, newPart: String): String = when {
        Strings.isNullOrEmpty(ifContent) -> newPart
        else -> "$ifContent && $newPart"
    }


    private fun getFormattedSwitchTime(definition: TimerDefinition): String {
        val switchTime = definition.switchTime
        return listOf(switchTime.hourOfDay, switchTime.minuteOfHour, switchTime.secondOfMinute)
                .joinToString(separator = ":") { toTwoDecimalDigits(it) }
    }

    data class ParsedSwitchContent(
            val targetDevice: String,
            val targetState: String,
            val targetStateAppendix: String?,
            val repetition: AtRepetition?
    )

    companion object {
        private val FHEM_PATTERN = Pattern.compile("fhem\\(\"set ([\\w\\-,\\\\.]+) ([\\w%-]+)(?: ([0-9.:]+))?\"\\)(.*)")
        private val PREFIX_PATTERN = Pattern.compile("([+*]{0,2})([0-9:]+)(.*)")
        private val DEFAULT_PATTERN = Pattern.compile("set ([\\w-]+) ([\\w\\-,%]+)(?: ([0-9:]+))?")
        private val DATE_TIME_FORMAT = DateTimeFormat.forPattern("HH:mm:ss")

        private val logger = LoggerFactory.getLogger(AtDefinitionParser::class.java)!!
    }
}