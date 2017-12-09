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

package li.klass.fhem.devices.at

import com.tngtech.java.junit.dataprovider.DataProvider
import com.tngtech.java.junit.dataprovider.DataProviderRunner
import com.tngtech.java.junit.dataprovider.UseDataProvider
import li.klass.fhem.devices.backend.at.AtDefinitionParser
import li.klass.fhem.devices.backend.at.AtRepetition
import li.klass.fhem.devices.backend.at.AtRepetition.*
import li.klass.fhem.devices.backend.at.TimerType
import li.klass.fhem.devices.backend.at.TimerType.ABSOLUTE
import li.klass.fhem.devices.backend.at.TimerType.RELATIVE
import org.assertj.core.api.Assertions.assertThat
import org.joda.time.LocalTime
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(DataProviderRunner::class)
class AtDefinitionParserTest {

    @Test
    @UseDataProvider("provider")
    fun should_parse_and_assemble_definition(testCase: TestCase) {
        // given
        val parser = AtDefinitionParser()

        // when
        val result = parser.parse(testCase.definition)!!

        // then
        assertThat(result.switchTime).isEqualTo(LocalTime(testCase.hours, testCase.minutes, testCase.seconds))
        assertThat(result.targetState).isEqualTo(testCase.targetState)
        assertThat(result.targetDeviceName).isEqualTo(testCase.targetDevice)
        assertThat(result.targetStateAppendix).isEqualTo(testCase.additionalInformation)
        assertThat(result.repetition).isEqualTo(testCase.repetition)
        assertThat(result.type).isEqualTo(testCase.timerType)

        // when
        val assembledDefinition = parser.toFHEMDefinition(result)

        // then
        assertThat(assembledDefinition).isEqualTo(testCase.assembledDefinition)
    }

    data class TestCase(
            val hours: Int,
            val minutes: Int,
            val seconds: Int,
            val targetDevice: String,
            val targetState: String,
            val additionalInformation: String? = null,
            val repetition: AtRepetition,
            val timerType: TimerType,
            val definition: String,
            val assembledDefinition: String
    )

    companion object {
        @DataProvider
        @JvmStatic
        fun provider(): List<TestCase> {
            return listOf(
                    TestCase(
                            hours = 17,
                            minutes = 0,
                            seconds = 0,
                            targetState = "on",
                            targetDevice = "lamp",
                            additionalInformation = null,
                            repetition = ONCE,
                            timerType = ABSOLUTE,
                            definition = "17:00:00 set lamp on",
                            assembledDefinition = "17:00:00 { fhem(\"set lamp on\") }"
                    ),
                    TestCase(
                            hours = 23,
                            minutes = 0,
                            seconds = 0,
                            targetState = "off",
                            targetDevice = "lamp",
                            additionalInformation = null,
                            repetition = WEEKEND,
                            timerType = ABSOLUTE,
                            definition = "*23:00:00 { fhem(\"set lamp off\") if (\$we) }",
                            assembledDefinition = "*23:00:00 { fhem(\"set lamp off\") if (\$we) }"
                    ),
                    TestCase(
                            hours = 23,
                            minutes = 0,
                            seconds = 0,
                            targetState = "off-for-timer",
                            targetDevice = "lamp",
                            additionalInformation = "200",
                            repetition = WEEKDAY,
                            timerType = RELATIVE,
                            definition = "+*23:00:00 { fhem(\"set lamp off-for-timer 200\") if (not \$we) }",
                            assembledDefinition = "+*23:00:00 { fhem(\"set lamp off-for-timer 200\") if (!\$we) }"
                    ),
                    TestCase(
                            hours = 23,
                            minutes = 0,
                            seconds = 0,
                            targetState = "off-for-timer",
                            targetDevice = "lamp",
                            additionalInformation = "200",
                            repetition = WEEKDAY,
                            timerType = ABSOLUTE,
                            definition = "*23:00:00 { fhem(\"set lamp off-for-timer 200\") if (NOT \$we) }",
                            assembledDefinition = "*23:00:00 { fhem(\"set lamp off-for-timer 200\") if (!\$we) }"
                    ),
                    TestCase(
                            hours = 23,
                            minutes = 0,
                            seconds = 0,
                            targetState = "off-for-timer",
                            targetDevice = "lamp",
                            additionalInformation = "200",
                            repetition = WEEKDAY,
                            timerType = ABSOLUTE,
                            definition = "*23:00:00 { fhem(\"set lamp off-for-timer 200\") if (NOT \$we) }",
                            assembledDefinition = "*23:00:00 { fhem(\"set lamp off-for-timer 200\") if (!\$we) }"
                    ),
                    TestCase(
                            hours = 7,
                            minutes = 15,
                            seconds = 0,
                            targetState = "desired-temp",
                            targetDevice = "Badezimmer",
                            additionalInformation = "00.00",
                            repetition = FRIDAY,
                            timerType = ABSOLUTE,
                            definition = "*07:15:00 { fhem(\"set Badezimmer desired-temp 00.00\") if (\$wday == 5) }",
                            assembledDefinition = "*07:15:00 { fhem(\"set Badezimmer desired-temp 00.00\") if (\$wday == 5) }"
                    ),
                    TestCase(
                            hours = 19,
                            minutes = 45,
                            seconds = 0,
                            targetState = "desired-temp",
                            targetDevice = "EZ.Heizung_Clima",
                            additionalInformation = "24.00",
                            repetition = ONCE,
                            timerType = ABSOLUTE,
                            definition = "19:45:00 { fhem(\"set EZ.Heizung_Clima desired-temp 24.00\") }",
                            assembledDefinition = "19:45:00 { fhem(\"set EZ.Heizung_Clima desired-temp 24.00\") }"
                    ),
                    TestCase(
                            hours = 17,
                            minutes = 0,
                            seconds = 0,
                            targetState = "on",
                            targetDevice = "d",
                            additionalInformation = null,
                            repetition = ONCE,
                            timerType = ABSOLUTE,
                            definition = "2016-10-16T17:00:00 { fhem(\"set d on\") }",
                            assembledDefinition = "17:00:00 { fhem(\"set d on\") }"
                    )
            )

        }
    }
}
