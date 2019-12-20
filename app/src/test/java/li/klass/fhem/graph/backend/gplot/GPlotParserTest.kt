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

import com.google.common.base.Charsets
import com.google.common.collect.Range
import com.tngtech.java.junit.dataprovider.DataProviderRunner
import li.klass.fhem.graph.backend.gplot.DataProviderSpec.*
import li.klass.fhem.graph.backend.gplot.GPlotSeries.*
import li.klass.fhem.testutil.MockitoRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.InjectMocks
import java.io.IOException

@RunWith(DataProviderRunner::class)
class GPlotParserTest {
    @Rule
    @JvmField
    val mockitoRule = MockitoRule()

    @InjectMocks
    private lateinit var gPlotParser: GPlotParser

    @Test
    fun should_parse_standard_GPlot_File() {
        val content = readGPlot("fht.gplot")

        val definition = gPlotParser.parse(content)

        val leftAxis = definition.leftAxis
        assertThat(leftAxis.label).isEqualTo("Actuator (%)")
        assertThat(leftAxis.series).containsExactly(
                GPlotSeries(viewSpec = ViewSpec(title = "Actuator (%)", color = SeriesColor.GREEN,
                        lineType = LineType.LINES, axis = Axis.LEFT, lineWidth = 1F, seriesType = SeriesType.DEFAULT),
                        dataProvider = DataProvider(FileLog(pattern = "4:actuator.*[0-9]+%:0:int"))))
        val rightAxis = definition.rightAxis
        assertThat(rightAxis.label).isEqualTo("Temperature in C")
        assertThat(rightAxis.series).containsExactly(
                GPlotSeries(viewSpec = ViewSpec(title = "Measured temperature", color = SeriesColor.RED,
                        lineType = LineType.LINES, seriesType = SeriesType.DEFAULT, lineWidth = 1F, axis = Axis.RIGHT),
                        dataProvider = DataProvider(FileLog(pattern = "4:measured:0:"))))
    }

    @Test
    fun should_parse_a_multi_log_device_gplot() {
        val content = readGPlot("multiLogDevices.gplot")

        val definition = gPlotParser.parse(content)

        val leftAxis = definition.leftAxis
        assertThat(leftAxis.series).containsExactly(
                GPlotSeries(viewSpec = ViewSpec(title = "Temperature", color = SeriesColor.RED,
                        lineType = LineType.LINES, axis = Axis.LEFT),
                        dataProvider = DataProvider(customLogDevice = CustomLogDevice(pattern = "4:IR:0:", logDevice = "FileLog_wetterstation"))))
        val rightAxis = definition.rightAxis
        assertThat(rightAxis.series).containsExactly(
                GPlotSeries(viewSpec = ViewSpec(title = "Zisterne", color = SeriesColor.GREEN,
                        lineType = LineType.LINES, seriesType = SeriesType.FILL, axis = Axis.RIGHT),
                        dataProvider = DataProvider(customLogDevice = CustomLogDevice(pattern = "4:zisterne.level\\x3a:0:", logDevice = "FileLog_zisterne"))))
    }

    @Test
    fun should_parse_GPlot_File_without_using() {
        val content = readGPlot("ks550_all.gplot")

        val definition = gPlotParser.parse(content)

        val leftAxis = definition.leftAxis
        assertThat(leftAxis.label).isEqualTo("<L1>")
        assertThat(leftAxis.series).containsExactly(
                GPlotSeries(
                        viewSpec = ViewSpec(title = "T", lineType = LineType.LINES, axis = Axis.LEFT, color = SeriesColor.RED),
                        dataProvider = DataProvider(FileLog(pattern = "4:::"))
                ),
                GPlotSeries(
                        viewSpec = ViewSpec(title = "H", lineType = LineType.LINES, axis = Axis.LEFT, color = SeriesColor.GREEN),
                        dataProvider = DataProvider(FileLog(pattern = "6:::"))
                ),
                GPlotSeries(
                        viewSpec = ViewSpec(title = "W", lineType = LineType.LINES, axis = Axis.LEFT, color = SeriesColor.BLUE),
                        dataProvider = DataProvider(FileLog(pattern = "8:::"))
                ),
                GPlotSeries(
                        viewSpec = ViewSpec(title = "R/h", lineType = LineType.LINES, axis = Axis.LEFT, color = SeriesColor.MAGENTA),
                        dataProvider = DataProvider(FileLog(pattern = "10::0:delta-h"))
                ),
                GPlotSeries(
                        viewSpec = ViewSpec(title = "R/d", lineType = LineType.LINES, axis = Axis.LEFT, color = SeriesColor.BROWN),
                        dataProvider = DataProvider(FileLog(pattern = "10::0:delta-d"))
                ),
                GPlotSeries(
                        viewSpec = ViewSpec(title = "IR", lineType = LineType.LINES, color = SeriesColor.WHITE, axis = Axis.LEFT),
                        dataProvider = DataProvider(FileLog(pattern = "12::0:\$fld[11]=~\"32768\"?1:0"))
                ),
                GPlotSeries(
                        viewSpec = ViewSpec(title = "WD", lineType = LineType.LINES, axis = Axis.LEFT, color = SeriesColor.OLIVE),
                        dataProvider = DataProvider(FileLog(pattern = "14::0:"))
                ),
                GPlotSeries(
                        viewSpec = ViewSpec(title = "WDR", lineType = LineType.LINES, axis = Axis.LEFT, color = SeriesColor.GRAY),
                        dataProvider = DataProvider(FileLog(pattern = "16::0:"))
                ),
                GPlotSeries(
                        viewSpec = ViewSpec(title = "S", lineType = LineType.LINES, axis = Axis.LEFT, color = SeriesColor.YELLOW),
                        dataProvider = DataProvider(FileLog(pattern = "18::0:delta-h"))
                ),
                GPlotSeries(viewSpec = ViewSpec(title = "B", lineType = LineType.LINES, axis = Axis.LEFT, color = SeriesColor.RED),
                        dataProvider = DataProvider(FileLog(pattern = "20::0:"))
                )
        )
        val rightAxis = definition.rightAxis
        assertThat(rightAxis.label).isEqualTo("<L2>")
        assertThat(rightAxis.series).isEmpty()
    }

    @Test
    fun should_parse_oneline_GPlot_File() {
        val content = readGPlot("power4.gplot")

        val definition = gPlotParser.parse(content)

        val leftAxis = definition.leftAxis
        assertThat(leftAxis.label).isEqualTo("Power (KW)")
        assertThat(leftAxis.series).containsExactly(
                GPlotSeries(viewSpec = ViewSpec(title = "", lineType = LineType.LINES,
                        axis = Axis.LEFT, color = SeriesColor.RED),
                        dataProvider = DataProvider(FileLog("4::0:"))))
        val rightAxis = definition.rightAxis
        assertThat(rightAxis.label).isEqualTo("Power (KW)")
        assertThat(rightAxis.series).isEmpty()
    }

    @Test
    fun should_parse_yrange_GPlot_File() {
        val content = readGPlot("fht80tf.gplot")

        val definition = gPlotParser.parse(content)

        val leftAxis = definition.leftAxis
        assertThat(leftAxis.label).isEmpty()
        assertThat(leftAxis.series).containsExactly(
                GPlotSeries(ViewSpec(title = "Open/Closed", lineType = LineType.LINES,
                        color = SeriesColor.RED,
                        axis = Axis.LEFT),
                        DataProvider(FileLog("4:Window:0:\$fld[3]=~\"Open\"?1:0"))))
        assertThat(leftAxis.range)
                .isEqualTo(Range.closed(-0.2, 1.2))
        val rightAxis = definition.rightAxis
        assertThat(rightAxis.series).isEmpty()
    }

    @Test
    fun should_parse_yrange_upper_open_GPlot_File() {
        val content = readGPlot("esa2000.gplot")

        val definition = gPlotParser.parse(content)

        assertThat(definition.leftAxis.range)
                .isEqualTo(Range.atLeast(0.0))
        assertThat(definition.rightAxis.range)
                .isEqualTo(Range.atLeast(0.0))
    }

    @Test
    fun should_parse_dblog_GPlot_File() {
        val content = readGPlot("SM_DB_Network_eth0.gplot")

        val definition = gPlotParser.parse(content)

        val leftAxis = definition.leftAxis
        assertThat(leftAxis.label).isEqualTo("Traffic RX / TX")
        assertThat(leftAxis.series).containsExactly(
                GPlotSeries(viewSpec = ViewSpec(title = "RX", lineType = LineType.LINES,
                        axis = Axis.LEFT, color = SeriesColor.RED),
                        dataProvider = DataProvider(dbLog = DbLog("<SPEC1>:eth0_diff:::\$val=~s/^RX..([\\d.]*).*/$1/eg"))),
                GPlotSeries(viewSpec = ViewSpec(title = "TX", lineType = LineType.LINES,
                        color = SeriesColor.GREEN, axis = Axis.LEFT),
                        dataProvider = DataProvider(dbLog = DbLog("<SPEC1>:eth0_diff:::\$val=~s/.*TX..([\\d.]*).*/$1/eg"))))
        val rightAxis = definition.rightAxis
        assertThat(rightAxis.label).isEqualTo("Traffic Total")
        assertThat(rightAxis.series).containsExactly(
                GPlotSeries(viewSpec = ViewSpec(title = "Total", lineType = LineType.LINES,
                        axis = Axis.RIGHT, color = SeriesColor.BLUE),
                        dataProvider = DataProvider(dbLog = DbLog("<SPEC1>:eth0_diff:::\$val=~s/.*Total..([\\d.]*).*/$1/eg"))))
    }

    @Test
    fun should_parse_series_types() {
        val content = readGPlot("temp4rain10.gplot")

        val definition = gPlotParser.parse(content)

        val leftAxis = definition.leftAxis
        assertThat(leftAxis.label).isEqualTo("Rain (l/m2)")
        assertThat(leftAxis.series).containsExactly(
                GPlotSeries(viewSpec = ViewSpec(title = "Rain/h", lineType = LineType.HISTEPS,
                        color = SeriesColor.GREEN,
                        seriesType = SeriesType.FILL, axis = Axis.LEFT),
                        dataProvider = DataProvider(FileLog("10:IR\\x3a:0:delta-h"))),
                GPlotSeries(viewSpec = ViewSpec(title = "Rain/day", lineType = LineType.HISTEPS,
                        color = SeriesColor.BLUE,
                        seriesType = SeriesType.DEFAULT, axis = Axis.LEFT),
                        dataProvider = DataProvider(FileLog("10:IR\\x3a:0:delta-d"))))
        val rightAxis = definition.rightAxis
        assertThat(rightAxis.label).isEqualTo("Temperature in C")
        assertThat(rightAxis.series).containsExactly(
                GPlotSeries(ViewSpec(title = "Temperature", lineType = LineType.LINES,
                        color = SeriesColor.RED,
                        seriesType = SeriesType.DEFAULT, axis = Axis.RIGHT),
                        DataProvider(FileLog("4:IR\\x3a:0:"))))
    }

    @Test
    fun should_parse_line_width() {
        val content = readGPlot("co20.gplot")

        val definition = gPlotParser.parse(content)

        val axis = definition.rightAxis
        assertThat(axis.series).contains(
                GPlotSeries(viewSpec = ViewSpec(title = "Air quality (ppm)", lineType = LineType.LINES,
                        color = SeriesColor.WHITE,
                        seriesType = SeriesType.DEFAULT, axis = Axis.RIGHT, lineWidth = (0.2f)),
                        dataProvider = DataProvider(FileLog("4:voc::"))))
    }

    @Test
    fun should_parse_user_specified_power8ttt_gplot() {
        val content = readGPlot("power8ttt.gplot")

        val definition = gPlotParser.parse(content)

        val leftAxis = definition.leftAxis
        assertThat(leftAxis.label).isEqualTo("Power (KWh)")
        assertThat(leftAxis.range).isNull()
        val rightAxis = definition.rightAxis
        assertThat(rightAxis.label).isEqualTo("Power (KWh)")
        assertThat(rightAxis.range).isNull()
        assertThat(rightAxis.series).containsOnly(
                GPlotSeries(viewSpec = ViewSpec(title = "Stromzähler", lineType = LineType.LINES,
                        color = SeriesColor.RED,
                        seriesType = SeriesType.FILL, axis = Axis.RIGHT, lineWidth = (1f)),
                        dataProvider = DataProvider(customLogDevice = CustomLogDevice(logDevice = "sumLog", pattern = "4:CUL_EM_22.Summe\\x3a:0:"))))
    }

    @Test
    fun should_parse_temp4hum4_gplot() {
        val content = readGPlot("temp4hum4.gplot")

        val definition = gPlotParser.parse(content)

        val leftAxis = definition.leftAxis
        assertThat(leftAxis.label).isEqualTo("Humidity (%)")
        assertThat(leftAxis.range).isNull()
        val rightAxis = definition.rightAxis
        assertThat(rightAxis.label).isEqualTo("Temperature in C")
        assertThat(rightAxis.range).isNull()
        assertThat(rightAxis.series).containsOnly(
                GPlotSeries(viewSpec = ViewSpec(title = "Measured temperature", lineType = LineType.LINES,
                        color = SeriesColor.RED,
                        seriesType = SeriesType.DEFAULT, axis = Axis.RIGHT, lineWidth = (1f)),
                        dataProvider = DataProvider(FileLog("4:temperature:10:"))))
        assertThat(leftAxis.series).containsOnly(
                GPlotSeries(viewSpec = ViewSpec(title = "Humidity (%)", lineType = LineType.LINES,
                        color = SeriesColor.GREEN,
                        seriesType = SeriesType.DEFAULT, axis = Axis.LEFT, lineWidth = 1f),
                        dataProvider = DataProvider(FileLog("4:humidity:50:"))))
    }

    @Test
    fun should_parse_egrep_files() {
        val content = readGPlot("egrep.gplot")

        val definition = gPlotParser.parse(content)

        val leftAxis = definition.leftAxis
        assertThat(leftAxis.label).isEqualTo("Signal in %")
        assertThat(leftAxis.range).isNull()
        val rightAxis = definition.rightAxis
        assertThat(rightAxis.label).isEqualTo("Temperatur in Grad Celsius")
        assertThat(rightAxis.range).isNull()
        assertThat(rightAxis.series).containsOnly(
                GPlotSeries(ViewSpec(title = "Soll-Temperatur (C)", lineType = LineType.LINES,
                        color = SeriesColor.RED,
                        seriesType = SeriesType.DEFAULT, axis = Axis.RIGHT, lineWidth = (2f)),
                        DataProvider(FileLog("4:desiredTemperature:0:"))),
                GPlotSeries(ViewSpec(title = "Ist-Temperatur(ungenau)(C)", lineType = LineType.LINES,
                        color = SeriesColor.GREEN,
                        seriesType = SeriesType.DEFAULT, axis = Axis.RIGHT, lineWidth = (2f)),
                        DataProvider(FileLog("4:temperature:0:"))))
        assertThat(leftAxis.series).containsOnly(
                GPlotSeries(ViewSpec(title = "Ventil (%)", lineType = LineType.LINES,
                        color = SeriesColor.BLUE,
                        seriesType = SeriesType.DEFAULT, axis = Axis.LEFT, lineWidth = (2f)),
                        DataProvider(FileLog("4:valveposition:0:"))))
    }

    @Test
    fun should_parse_mixed_DbLog_and_FileLog_gplot_files() {
        val content = readGPlot("mixedFileLogDbLog.gplot")

        val definition = gPlotParser.parse(content)

        val leftAxis = definition.leftAxis
        assertThat(leftAxis.label).isEqualTo("Humidity (%)")
        assertThat(leftAxis.range).isEqualTo(Range.closed(0.0, 100.0))
        val rightAxis = definition.rightAxis
        assertThat(rightAxis.label).isEqualTo("Temperature in C")
        assertThat(rightAxis.range).isEqualTo(Range.closed(0.0, 40.0))
        assertThat(rightAxis.series).containsOnly(
                GPlotSeries(ViewSpec(title = "Temperature", lineType = LineType.LINES,
                        color = SeriesColor.RED,
                        seriesType = SeriesType.FILL, axis = Axis.RIGHT, lineWidth = 1f),
                        DataProvider(
                                fileLog = FileLog("4:temperature:10:"),
                                dbLog = DbLog("<SPEC1>:temperature:10:")
                        )))
        assertThat(leftAxis.series).containsOnly(
                GPlotSeries(ViewSpec(title = "Humidity", lineType = LineType.LINES,
                        color = SeriesColor.BLUE,
                        seriesType = SeriesType.DEFAULT, axis = Axis.LEFT, lineWidth = 1f),
                        DataProvider(
                                fileLog = FileLog("6:humidity:50:"),
                                dbLog = DbLog("<SPEC1>:humidity:50:")
                        )))
    }

    @Test
    fun should_parse_custom_gplot_file() {
        val content = readGPlot("custom.gplot")

        val definition = gPlotParser.parse(content)

        val leftAxis = definition.leftAxis
        assertThat(leftAxis.label).isEqualTo("Anwesenheit")
        assertThat(leftAxis.range)
                .isEqualTo(Range.closed(0.0, 100.0))
        val rightAxis = definition.rightAxis
        assertThat(rightAxis.label).isEqualTo("Tür/Fenster")
        assertThat(rightAxis.range)
                .isEqualTo(Range.closed(0.0, 1.0))
        assertThat(leftAxis.series).containsOnly(
                GPlotSeries(ViewSpec(title = "Handy1", lineType = LineType.STEPS,
                        color = SeriesColor.GREEN, seriesType = SeriesType.FILL,
                        axis = Axis.LEFT, lineWidth = (1.5f)),
                        DataProvider(customLogDevice = CustomLogDevice("Handy1:state:::\$val=(\$val=~'present'?50:0)", "logdb"))),
                GPlotSeries(ViewSpec(title = "Handy2", lineType = LineType.STEPS,
                        color = SeriesColor.MAGENTA, seriesType = SeriesType.FILL,
                        axis = Axis.LEFT, lineWidth = (1.5f)), DataProvider(customLogDevice = CustomLogDevice("Handy2:state:::\$val=(\$val=~'present'?50:0)", "logdb"))))
        assertThat(rightAxis.series).containsOnly(
                GPlotSeries(ViewSpec(title = "Tür", lineType = LineType.STEPS,
                        color = SeriesColor.BLUE, seriesType = SeriesType.DEFAULT,
                        axis = Axis.RIGHT, lineWidth = (2f)), DataProvider(customLogDevice = CustomLogDevice("whg_tuer:onoff", "logdb"))),
                GPlotSeries(ViewSpec(title = "Fenster Küche", lineType = LineType.POINTS,
                        color = SeriesColor.RED, seriesType = SeriesType.DEFAULT,
                        axis = Axis.RIGHT, lineWidth = (1f)), DataProvider(customLogDevice = CustomLogDevice("k_fenster:opened:::\$val=(\$val=~'opened'?1:0)", "logdb"))),
                GPlotSeries(ViewSpec(title = "Fenster Schlafzimmer", lineType = LineType.POINTS,
                        color = SeriesColor.WHITE, seriesType = SeriesType.DEFAULT,
                        axis = Axis.RIGHT, lineWidth = (1f)), DataProvider(customLogDevice = CustomLogDevice("sz_fenster:opened:::\$val=(\$val=~'opened'?1:0)", "logdb"))))
    }

    @Throws(IOException::class)
    private fun readGPlot(fileName: String) = GPlotParser::class.java.getResourceAsStream(
            fileName).use {
        it?.readBytes()?.toString(Charsets.UTF_8)
    } ?: ""
}