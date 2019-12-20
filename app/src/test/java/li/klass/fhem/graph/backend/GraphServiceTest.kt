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

package li.klass.fhem.graph.backend

import com.google.common.base.Optional
import com.tngtech.java.junit.dataprovider.DataProvider
import com.tngtech.java.junit.dataprovider.DataProviderRunner
import com.tngtech.java.junit.dataprovider.UseDataProvider
import li.klass.fhem.graph.backend.gplot.GPlotSeries
import li.klass.fhem.testutil.MockitoRule
import li.klass.fhem.testutil.ValueProvider
import li.klass.fhem.update.backend.DeviceListService
import li.klass.fhem.update.backend.command.execution.Command
import li.klass.fhem.update.backend.command.execution.CommandExecutionService
import org.assertj.core.api.Assertions.assertThat
import org.joda.time.DateTime
import org.joda.time.Interval
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.BDDMockito.given
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.mock

@RunWith(DataProviderRunner::class)
class GraphServiceTest {
    @get:Rule
    var mockitoRule = MockitoRule()

    @Mock
    private lateinit var commandExecutionService: CommandExecutionService
    @Mock
    private lateinit var graphIntervalProvider: GraphIntervalProvider
    @Mock
    private lateinit var deviceListService: DeviceListService

    @InjectMocks
    private lateinit var graphService: GraphService

    private val valueProvider = ValueProvider()

    @Test
    fun testFindGraphEntries() {
        val content = "\n" +
                "2013-03-21_16:38:39 5.7\n" +
                "2013-03-21_16:48:49 5.9\n" +
                "2013-03-21_16:53:54 6.2\n" +
                "2013-03-21_17:01:32 5.4\n" +
                "2013-03-21_17:04:04 5.2\n" +
                "#4::\n" +
                "\n" +
                "\n"

        val graphEntries = graphService.findGraphEntries(content)

        assertThat(graphEntries).hasSize(5)
    }

    @Test
    @UseDataProvider("graphEntryProvider")
    fun should_parse_graph_entry(testCase: GraphEntryParseTestCase) {
        val result = graphService.parseEntry(testCase.value)

        assertThat(result).isEqualTo(testCase.expected)
    }

    @Test
    fun should_load_graph_entries() {
        // given
        val logDeviceName = valueProvider.lowercaseString(10)
        val series = mock(GPlotSeries::class.java)
        val spec1 = valueProvider.lowercaseString(10)
        val spec2 = valueProvider.lowercaseString(10)
        val plotfunction = listOf(spec1, spec2)

        val from = valueProvider.dateTime()
        val to = from.plusDays(valueProvider.intValue(10))
        val fromDateFormatted = GraphService.DATE_TIME_FORMATTER.print(from)
        val toDateFormatted = GraphService.DATE_TIME_FORMATTER.print(to)

        val command = String.format(GraphService.COMMAND_TEMPLATE, logDeviceName, fromDateFormatted, toDateFormatted, spec1)
        val response = valueProvider.lowercaseString(20)
        given(commandExecutionService.executeSync(Command(command.replace("<SPEC1>".toRegex(), spec1).replace("<SPEC2>".toRegex(), spec2), Optional.absent()))).willReturn(response)
        val logDefinition = LogDataDefinition(logDeviceName, spec1, series)

        // when
        val result = graphService.loadLogData(logDefinition, null, Interval(from, to), plotfunction)

        // then
        assertThat(result).isEqualToIgnoringCase("\n\r" + response)
    }

    data class GraphEntryParseTestCase(val value: String, val expected: GraphEntry?)

    companion object {
        @Suppress("unused")
        @JvmStatic
        @DataProvider
        fun graphEntryProvider(): List<GraphEntryParseTestCase> {
            val dateTime = DateTime(2013, 3, 21, 16, 38, 39)
            return listOf(
                    GraphEntryParseTestCase("2013-03-21_16:38:39 5.7\n", GraphEntry(dateTime, 5.7f)),
                    GraphEntryParseTestCase("2013-03-21_16:38:39 5.7", GraphEntry(dateTime, 5.7f)),
                    GraphEntryParseTestCase("2013-03-21_16:38:39 -5.7\n", GraphEntry(dateTime, -5.7f)),
                    GraphEntryParseTestCase("2013-03-21_16:38 5.7\n", null),
                    GraphEntryParseTestCase("2013-03-21_16:38:39\n", null)
            )
        }
    }
}
