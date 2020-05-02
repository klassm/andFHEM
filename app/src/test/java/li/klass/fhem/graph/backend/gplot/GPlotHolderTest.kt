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
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.eq
import com.nhaarman.mockito_kotlin.given
import li.klass.fhem.graph.backend.gplot.GPlotDefinitionTestdataBuilder.defaultGPlotDefinition
import li.klass.fhem.testutil.MockitoRule
import li.klass.fhem.update.backend.command.execution.Command
import li.klass.fhem.update.backend.command.execution.CommandExecutionService
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.ArgumentMatchers
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito

class GPlotHolderTest {
    @get:Rule
    val mockitoRule = MockitoRule()

    @InjectMocks
    private lateinit var gPlotHolder: GPlotHolder

    @Mock
    private lateinit var gPlotParser: GPlotParser

    @Mock
    private lateinit var application: Application

    @Mock
    private lateinit var context: Context

    @Mock
    private val commandExecutionService: CommandExecutionService? = null

    @Before
    @Throws(Exception::class)
    fun setUp() {
        Mockito.`when`(application.applicationContext).thenReturn(context)
    }

    @Test
    fun should_get_default_definition_for_name() {
        // given
        val definition = defaultGPlotDefinition()
        given(gPlotParser.defaultGPlotFiles).willReturn(mapOf("abc" to definition))

        // when
        val foundDefinition = gPlotHolder.definitionFor("abc", false)

        // then
        assertThat(foundDefinition).isEqualTo(definition)
    }

    @Test
    fun should_successfully_lookup_GPlot_file_if_current_map_does_not_yet_contain_corresponding_key() {
        // given
        val definition = defaultGPlotDefinition()
        given(gPlotParser.defaultGPlotFiles).willReturn(emptyMap())
        val gplotRawDefinition = "myValue" + System.currentTimeMillis()
        given(commandExecutionService!!.executeRequest(eq("/gplot/garden.gplot"), any())).willReturn(gplotRawDefinition)
        given(gPlotParser.parseSafe(gplotRawDefinition)).willReturn(definition)

        // when
        val garden = gPlotHolder.definitionFor("garden", false)

        // then
        assertThat(garden).isEqualTo(definition)
    }

    @Test
    fun should_lookup_GPlot_file_without_success_if_current_map_does_not_yet_contain_corresponding_key() {
        // given
        given(gPlotParser.defaultGPlotFiles).willReturn(emptyMap())
        given(commandExecutionService!!.executeRequest(eq("/gplot/garden.gplot"), any())).willReturn(null)

        // when
        val garden = gPlotHolder.definitionFor("garden", false)

        // then
        assertThat(garden).isEqualTo(null)
        Mockito.verify(gPlotParser, Mockito.never()).parseSafe(ArgumentMatchers.anyString())
    }

    @Test
    fun should_lookup_GPlot_file_only_once_if_previous_request_was_successful() {
        // given
        val definition = defaultGPlotDefinition()
        given(gPlotParser.defaultGPlotFiles).willReturn(emptyMap())
        val gplotRawDefinition = "myValue" + System.currentTimeMillis()
        given(commandExecutionService!!.executeRequest(eq("/gplot/garden.gplot"), any())).willReturn(gplotRawDefinition)
        given(gPlotParser.parseSafe(gplotRawDefinition)).willReturn(definition)
        gPlotHolder.definitionFor("garden", false)

        // when
        gPlotHolder.definitionFor("garden", false)

        // then
        Mockito.verify(gPlotParser, Mockito.times(1)).parseSafe(ArgumentMatchers.anyString())
    }

    @Test
    fun should_lookup_GPlot_file_only_once_if_previous_request_was_not_successful() {
        // given
        val definition = defaultGPlotDefinition()
        given(gPlotParser.defaultGPlotFiles).willReturn(emptyMap())
        val gplotRawDefinition = "myValue" + System.currentTimeMillis()
        given(commandExecutionService!!.executeRequest(eq("/gplot/garden.gplot"), any())).willReturn(null)
        given(gPlotParser.parseSafe(gplotRawDefinition)).willReturn(definition)

        // when
        assertThat(gPlotHolder.definitionFor("garden", false) != null).isFalse()

        // then
        Mockito.verify(commandExecutionService, Mockito.times(1)).executeRequest(ArgumentMatchers.anyString(), any())
        Mockito.verify(gPlotParser, Mockito.never()).parseSafe(ArgumentMatchers.anyString())
    }

    @Test
    fun should_handle_config_db() {
        // given
        val definition = defaultGPlotDefinition()
        given(gPlotParser.defaultGPlotFiles).willReturn(emptyMap())
        val gplotRawDefinition = "myValue" + System.currentTimeMillis()
        given(commandExecutionService!!.executeSync(eq(Command("configdb fileshow ./www/gplot/garden.gplot", null)))).willReturn(gplotRawDefinition)
        given(gPlotParser.parseSafe(gplotRawDefinition)).willReturn(definition)

        // when
        val garden = gPlotHolder.definitionFor("garden", true)

        // then
        assertThat(garden).isEqualTo(definition)
    }
}