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
package li.klass.fhem.service.intent

import android.content.Context
import android.content.SharedPreferences
import com.tngtech.java.junit.dataprovider.DataProvider
import com.tngtech.java.junit.dataprovider.DataProviderRunner
import com.tngtech.java.junit.dataprovider.DataProviders.testForEach
import com.tngtech.java.junit.dataprovider.UseDataProvider
import io.mockk.Called
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import li.klass.fhem.connection.backend.ConnectionService
import li.klass.fhem.testutil.MockRule
import li.klass.fhem.update.backend.command.execution.CommandExecutionService
import li.klass.fhem.util.preferences.SharedPreferencesService
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(DataProviderRunner::class)
class SendCommandServiceTest {
    @MockK
    lateinit var connectionService: ConnectionService

    @MockK
    lateinit var commandExecutionService: CommandExecutionService

    @MockK
    lateinit var sharedPreferencesService: SharedPreferencesService

    @MockK
    lateinit var sharedPreferences: SharedPreferences

    @MockK
    lateinit var editor: SharedPreferences.Editor

    @MockK
    lateinit var context: Context

    @InjectMockKs
    lateinit var intentService: SendCommandService

    @Rule
    @JvmField
    var mockRule: MockRule = MockRule()

    @Test
    @UseDataProvider("recentCommandsProvider")
    fun should_get_recent_commands(testCase: TestCase) {
        every { sharedPreferencesService.getPreferences(SendCommandService.PREFERENCES_NAME) } returns sharedPreferences
        every { sharedPreferences.getString(SendCommandService.COMMANDS_PROPERTY, null) } returns testCase.jsonInput

        val result = intentService.getRecentCommands()

        assertThat(result).containsExactlyElementsOf(testCase.expectedCommands)
        verify { listOf(commandExecutionService, connectionService) wasNot Called }
    }

    companion object {
        @DataProvider
        @JvmStatic
        fun recentCommandsProvider(): Array<Array<Any>> {
            return testForEach(
                    TestCase(
                            String.format("{%s: %s}", SendCommandService.COMMANDS_JSON_PROPERTY, "['a', 'b', 'c']"), listOf("a", "b", "c")
                    ),
                    TestCase(
                            null, emptyList<String>()
                    ),
                    TestCase(
                            "{", emptyList<String>()
                    )
            )
        }
    }

    data class TestCase(val jsonInput: String?, val expectedCommands: List<String?>)
}