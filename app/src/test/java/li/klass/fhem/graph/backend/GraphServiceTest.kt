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

import android.content.Context
import com.tngtech.java.junit.dataprovider.DataProvider
import com.tngtech.java.junit.dataprovider.DataProviderRunner
import com.tngtech.java.junit.dataprovider.UseDataProvider
import li.klass.fhem.domain.core.FhemDevice
import li.klass.fhem.graph.backend.gplot.*
import li.klass.fhem.graph.backend.gplot.DataProviderSpec.CustomLogDevice
import li.klass.fhem.graph.backend.gplot.DataProviderSpec.FileLog
import li.klass.fhem.graph.backend.gplot.GPlotSeriesTestdataBuilder.defaultGPlotSeries
import li.klass.fhem.testutil.MockitoRule
import li.klass.fhem.testutil.ValueProvider
import li.klass.fhem.update.backend.DeviceListService
import li.klass.fhem.update.backend.command.execution.Command
import li.klass.fhem.update.backend.command.execution.CommandExecutionService
import li.klass.fhem.update.backend.xmllist.DeviceNode
import li.klass.fhem.update.backend.xmllist.XmlListDevice
import org.assertj.core.api.Assertions.assertThat
import org.joda.time.DateTime
import org.joda.time.Interval
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.BDDMockito.given
import org.mockito.InjectMocks
import org.mockito.Mock

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
    @Mock
    private lateinit var context: Context

    @InjectMocks
    private lateinit var graphService: GraphService

    private val valueProvider = ValueProvider()

    private val dummyResponse = "\n" +
            "2013-03-21_16:38:39 5.7\n" +
            "2013-03-21_16:48:49 5.9\n" +
            "2013-03-21_16:53:54 6.2\n" +
            "2013-03-21_17:01:32 5.4\n" +
            "2013-03-21_17:04:04 5.2\n" +
            "#4::\n" +
            "\n" +
            "\n"
    private val dummyResponseDataCount = 5

    private val from = valueProvider.dateTime()
    private val to = from.plusDays(valueProvider.intValue(10))
    private val fromDateFormatted = GraphService.DATE_TIME_FORMATTER.print(from)
    private val toDateFormatted = GraphService.DATE_TIME_FORMATTER.print(to)
    private val interval = Interval(from, to)
    private val pattern = "4::"
    private val connectionId = valueProvider.lowercaseString(20)

    @Before
    fun setUp() {
        given(graphIntervalProvider.getIntervalFor(from, to, null, context, connectionId)).willReturn(interval)
    }

    @Test
    @UseDataProvider("graphEntryProvider")
    fun should_parse_graph_entry(testCase: GraphEntryParseTestCase) {
        val result = graphService.parseEntry(testCase.value)

        assertThat(result).isEqualTo(testCase.expected)
    }

    @Test
    fun should_load_graph_entries_for_custom_devices() {
        // given
        val device = getDeviceFor(name = "bla", type = "BLA")
        val customLogDevice = valueProvider.lowercaseString(10)

        val series = defaultGPlotSeries().copy(
                dataProvider = GraphDataProvider(customLogDevice = CustomLogDevice(pattern, customLogDevice))
        )
        val svgGraphDefinition = graphDefinitionWithSeries(series)
        val command = String.format(GraphService.COMMAND_TEMPLATE, customLogDevice, fromDateFormatted, toDateFormatted, pattern)
        given(deviceListService.getDeviceForName(svgGraphDefinition.logDeviceName, connectionId)).willReturn(device)
        given(commandExecutionService.executeSync(Command(command, connectionId))).willReturn(dummyResponse)

        // when
        val result = graphService.getGraphData(
                device = device,
                connectionId = connectionId,
                svgGraphDefinition = svgGraphDefinition,
                startDate = from,
                endDate = to,
                context = context)

        // then
        assertThat(result.data.keys).containsOnly(series)
        assertThat(result.data[series]).hasSize(dummyResponseDataCount)
    }

    @Test
    fun should_load_graph_entries_for_FileLog_graphs() {
        // given

        val series = defaultGPlotSeries().copy(
                dataProvider = GraphDataProvider(fileLog = FileLog(pattern), dbLog = DataProviderSpec.DbLog("blablub"))
        )
        val svgGraphDefinition = graphDefinitionWithSeries(series)
        val device = getDeviceFor(name = svgGraphDefinition.logDeviceName, type = "FileLog")
        val command = String.format(GraphService.COMMAND_TEMPLATE, svgGraphDefinition.logDeviceName, fromDateFormatted, toDateFormatted, pattern)
        given(deviceListService.getDeviceForName(svgGraphDefinition.logDeviceName, connectionId)).willReturn(device)
        given(commandExecutionService.executeSync(Command(command, connectionId))).willReturn(dummyResponse)

        // when
        val result = graphService.getGraphData(
                device = device,
                connectionId = connectionId,
                svgGraphDefinition = svgGraphDefinition,
                startDate = from,
                endDate = to,
                context = context)

        // then
        assertThat(result.data.keys).containsOnly(series)
        assertThat(result.data[series]).hasSize(dummyResponseDataCount)
    }

    @Test
    fun should_load_graph_entries_for_DbLog_graphs() {
        // given
        val series = defaultGPlotSeries().copy(
                dataProvider = GraphDataProvider(dbLog = DataProviderSpec.DbLog(pattern), fileLog = FileLog("blablub"))
        )
        val svgGraphDefinition = graphDefinitionWithSeries(series)
        val device = getDeviceFor(name = svgGraphDefinition.logDeviceName, type = "DbLog")
        val command = String.format(GraphService.COMMAND_TEMPLATE, svgGraphDefinition.logDeviceName, fromDateFormatted, toDateFormatted, pattern)
        given(deviceListService.getDeviceForName(svgGraphDefinition.logDeviceName, connectionId)).willReturn(device)
        given(commandExecutionService.executeSync(Command(command, connectionId))).willReturn(dummyResponse)

        // when
        val result = graphService.getGraphData(
                device = device,
                connectionId = connectionId,
                svgGraphDefinition = svgGraphDefinition,
                startDate = from,
                endDate = to,
                context = context)

        // then
        assertThat(result.data.keys).containsOnly(series)
        assertThat(result.data[series]).hasSize(dummyResponseDataCount)
    }

    private fun graphDefinitionWithSeries(series: GPlotSeries) = SvgGraphDefinition(
            name = "abc",
            labels = emptyList(),
            logDeviceName = valueProvider.lowercaseString(10),
            plotDefinition = GPlotDefinitionTestdataBuilder.defaultGPlotDefinition().copy(
                    leftAxis = GPlotAxisTestdataCreator.defaultGPlotAxis().copy(
                            series = listOf(series)
                    ),
                    rightAxis = GPlotAxisTestdataCreator.defaultGPlotAxis().copy(series = emptyList())
            ),
            fixedrange = null,
            plotReplace = emptyMap(),
            plotfunction = emptyList(),
            title = "abc"
    )

    private fun getDeviceFor(name: String, type: String) = FhemDevice(XmlListDevice(type).apply {
        internals["NAME"] = DeviceNode(DeviceNode.DeviceNodeType.INT, "NAME", name, null as DateTime?)
    })

    data class GraphEntryParseTestCase(val value: String, val expected: GraphEntry?)

    companion object {
        @Suppress("unused")
        @JvmStatic
        @DataProvider
        fun graphEntryProvider(): List<GraphEntryParseTestCase> {
            val dateTime = DateTime(2013, 3, 21, 16, 38, 39)
            val dateTime2 = DateTime(2013, 1, 1, 0, 0, 0)
            return listOf(
                    GraphEntryParseTestCase("2013-03-21_16:38:39 5.7\n", GraphEntry(dateTime, 5.7f)),
                    GraphEntryParseTestCase("2013-03-21_16:38:39 5.7", GraphEntry(dateTime, 5.7f)),
                    GraphEntryParseTestCase("2013-03-21_16:38:39 -5.7\n", GraphEntry(dateTime, -5.7f)),
                    GraphEntryParseTestCase("2013 5.7\n", GraphEntry(dateTime2, 5.7f)),
                    GraphEntryParseTestCase("2013_03_21_16:38:39 5.7\n", null),
                    GraphEntryParseTestCase("2013-03-21_16:38:39\n", null)
            )
        }
    }

    @Test
    fun getLoadLogDataCommand() {
        val formatter: DateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")
        assertThat(graphService.getLoadLogDataCommand(
                LogDataDefinition("PowerDB", "%SOURCE%:%READING%::",
                        GPlotSeries(viewSpec = ViewSpec(title = "RX", lineType = GPlotSeries.LineType.LINES,
                                axis = GPlotSeries.Axis.LEFT, color = GPlotSeries.SeriesColor.RED),
                                dataProvider = GraphDataProvider(dbLog = DataProviderSpec.DbLog
                                ("<SPEC1>:eth0_diff:::\$val=~s/^RX..([\\d.]*).*/$1/eg")))
                ),
                Interval(
                        formatter.parseDateTime("2020-11-15 21:59:00"),
                        formatter.parseDateTime("2020-12-15 21:59:00")
                ),
                mapOf("SOURCE" to "PowerDay", "READING" to "1.8.1_1.8.2_sum", "UNIT" to "kWh"),
                emptyList(),
        )).isEqualTo("get PowerDB - - 2020-11-15_21:59 2020-12-15_21:59 PowerDay:1.8.1_1.8.2_sum::")
    }
}
