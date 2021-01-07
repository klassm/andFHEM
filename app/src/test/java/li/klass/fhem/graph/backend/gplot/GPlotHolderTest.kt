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
package li.klass.fhem.graph.backend.gplot

import android.app.Application
import android.content.Context
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import li.klass.fhem.graph.backend.gplot.GPlotDefinitionTestdataBuilder.defaultGPlotDefinition
import li.klass.fhem.testutil.MockRule
import li.klass.fhem.update.backend.command.execution.Command
import li.klass.fhem.update.backend.command.execution.CommandExecutionService
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class GPlotHolderTest {
    @get:Rule
    val mockitoRule = MockRule()

    @InjectMockKs
    lateinit var gPlotHolder: GPlotHolder

    @MockK
    lateinit var gPlotParser: GPlotParser

    @MockK
    lateinit var application: Application

    @MockK
    lateinit var context: Context

    @MockK
    lateinit var commandExecutionService: CommandExecutionService

    private val connectionId = "abc"

    @Before
    @Throws(Exception::class)
    fun setUp() {
        every { application.applicationContext } returns context
    }

    @Test
    fun should_get_default_definition_for_name() {
        // given
        val definition = defaultGPlotDefinition()
        every { gPlotParser.defaultGPlotFiles } returns mapOf("abc" to definition)
        every { commandExecutionService.executeRequest(any(), any(), any()) } returns null

        // when
        val foundDefinition = gPlotHolder.definitionFor("abc", false, connectionId)

        // then
        assertThat(foundDefinition).isEqualTo(definition)
    }

    @Test
    fun should_successfully_lookup_GPlot_file_if_current_map_does_not_yet_contain_corresponding_key() {
        // given
        val definition = defaultGPlotDefinition()
        every { gPlotParser.defaultGPlotFiles } returns emptyMap()
        val gplotRawDefinition = "myValue" + System.currentTimeMillis()
        every { commandExecutionService.executeRequest(eq("/gplot/garden.gplot"), any(), eq(connectionId)) } returns gplotRawDefinition
        every { gPlotParser.parseSafe(gplotRawDefinition) } returns definition

        // when
        val garden = gPlotHolder.definitionFor("garden", false, connectionId)

        // then
        assertThat(garden).isEqualTo(definition)
    }

    @Test
    fun should_lookup_GPlot_file_without_success_if_current_map_does_not_yet_contain_corresponding_key() {
        // given
        every { gPlotParser.defaultGPlotFiles } returns emptyMap()
        every { commandExecutionService.executeRequest(eq("/gplot/garden.gplot"), any(), eq(connectionId)) } returns null

        // when
        val garden = gPlotHolder.definitionFor("garden", false, connectionId)

        // then
        assertThat(garden).isEqualTo(null)
        verify(exactly = 0) { gPlotParser.parseSafe(any()) }
    }

    @Test
    fun should_lookup_GPlot_file_only_once_if_previous_request_was_successful() {
        // given
        val definition = defaultGPlotDefinition()
        every { gPlotParser.defaultGPlotFiles } returns emptyMap()
        val gplotRawDefinition = "myValue" + System.currentTimeMillis()
        every { commandExecutionService.executeRequest(eq("/gplot/garden.gplot"), any(), eq(connectionId)) } returns gplotRawDefinition
        every { gPlotParser.parseSafe(gplotRawDefinition) } returns definition
        gPlotHolder.definitionFor("garden", false, connectionId)

        // when
        gPlotHolder.definitionFor("garden", false, connectionId)

        // then
        verify(exactly = 1) { gPlotParser.parseSafe(any()) }
    }

    @Test
    fun should_lookup_GPlot_file_only_once_if_previous_request_was_not_successful() {
        // given
        val definition = defaultGPlotDefinition()
        every { gPlotParser.defaultGPlotFiles } returns emptyMap()
        val gplotRawDefinition = "myValue" + System.currentTimeMillis()
        every { commandExecutionService.executeRequest(eq("/gplot/garden.gplot"), any(), eq(connectionId)) } returns null
        every { gPlotParser.parseSafe(gplotRawDefinition) } returns null

        // when
        assertThat(gPlotHolder.definitionFor("garden", false, connectionId) != null).isFalse()

        // then
        verify(exactly = 1) { commandExecutionService.executeRequest(any(), any(), connectionId) }
        verify(exactly = 0) { gPlotParser.parseSafe(any()) }
    }

    @Test
    fun should_handle_config_db() {
        // given
        val definition = defaultGPlotDefinition()
        every { gPlotParser.defaultGPlotFiles } returns emptyMap()
        val gplotRawDefinition = "myValue" + System.currentTimeMillis()
        every { commandExecutionService.executeSync(eq(Command("configdb fileshow ./www/gplot/garden.gplot", connectionId))) } returns gplotRawDefinition
        every { gPlotParser.parseSafe(gplotRawDefinition) } returns definition

        // when
        val garden = gPlotHolder.definitionFor("garden", true, connectionId)

        // then
        assertThat(garden).isEqualTo(definition)
    }
}