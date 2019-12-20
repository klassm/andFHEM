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
    @Throws(Exception::class)
    fun should_parse_standard_GPlot_File() {
        val content = readGPlot("fht.gplot")

        val definition = gPlotParser.parse(content)

        val leftAxis = definition.leftAxis
        assertThat(leftAxis.label).isEqualTo("Actuator (%)")
        assertThat(leftAxis.series).containsExactly(
                GPlotSeries(title = "Actuator (%)", color = SeriesColor.GREEN,
                            lineType = LineType.LINES, logDef = "4:actuator.*[0-9]+%:0:int",
                            axis = Axis.LEFT, logDevice = null))
        val rightAxis = definition.rightAxis
        assertThat(rightAxis.label).isEqualTo("Temperature in C")
        assertThat(rightAxis.series).containsExactly(
                GPlotSeries(title = "Measured temperature", color = SeriesColor.RED,
                            lineType = LineType.LINES, logDef = "4:measured:0:", axis = Axis.RIGHT,
                            logDevice = null))
    }

    @Test
    @Throws(Exception::class)
    fun should_parse_a_multi_log_device_gplot() {
        val content = readGPlot("multiLogDevices.gplot")

        val definition = gPlotParser.parse(content)

        val leftAxis = definition.leftAxis
        assertThat(leftAxis.series).containsExactly(
                GPlotSeries(title = "Temperature", color = SeriesColor.RED,
                            lineType = LineType.LINES, logDef = "4:IR:0:", axis = Axis.LEFT,
                            logDevice = "FileLog_wetterstation"))
        val rightAxis = definition.rightAxis
        assertThat(rightAxis.series).containsExactly(
                GPlotSeries(title = "Zisterne", color = SeriesColor.GREEN,
                            lineType = LineType.LINES, logDef = "4:zisterne.level\\x3a:0:",
                            seriesType = SeriesType.FILL, axis = Axis.RIGHT,
                            logDevice = "FileLog_zisterne"))
    }

    @Test
    @Throws(Exception::class)
    fun should_parse_GPlot_File_without_using() {
        val content = readGPlot("ks550_all.gplot")

        val definition = gPlotParser.parse(content)

        val leftAxis = definition.leftAxis
        assertThat(leftAxis.label).isEqualTo("<L1>")
        assertThat(leftAxis.series).containsExactly(
                GPlotSeries(title = "T", lineType = LineType.LINES, logDef = "4:::",
                            axis = Axis.LEFT, color = SeriesColor.RED, logDevice = null),
                GPlotSeries(title = "H", lineType = LineType.LINES, logDef = "6:::",
                            axis = Axis.LEFT, color = SeriesColor.GREEN, logDevice = null),
                GPlotSeries(title = "W", lineType = LineType.LINES, logDef = "8:::",
                            axis = Axis.LEFT, color = SeriesColor.BLUE, logDevice = null),
                GPlotSeries(title = "R/h", lineType = LineType.LINES, logDef = "10::0:delta-h",
                            axis = Axis.LEFT, color = SeriesColor.MAGENTA, logDevice = null),
                GPlotSeries(title = "R/d", lineType = LineType.LINES, logDef = "10::0:delta-d",
                            axis = Axis.LEFT, color = SeriesColor.BROWN, logDevice = null),
                GPlotSeries(title = "IR", lineType = LineType.LINES,
                            logDef = "12::0:\$fld[11]=~\"32768\"?1:0", axis = Axis.LEFT,
                            color = SeriesColor.WHITE, logDevice = null),
                GPlotSeries(title = "WD", lineType = LineType.LINES, logDef = "14::0:",
                            axis = Axis.LEFT, color = SeriesColor.OLIVE, logDevice = null),
                GPlotSeries(title = "WDR", lineType = LineType.LINES, logDef = "16::0:",
                            axis = Axis.LEFT, color = SeriesColor.GRAY, logDevice = null),
                GPlotSeries(title = "S", lineType = LineType.LINES, logDef = "18::0:delta-h",
                            axis = Axis.LEFT, color = SeriesColor.YELLOW, logDevice = null),
                GPlotSeries(title = "B", lineType = LineType.LINES, logDef = "20::0:",
                            axis = Axis.LEFT, color = SeriesColor.RED, logDevice = null))
        val rightAxis = definition.rightAxis
        assertThat(rightAxis.label).isEqualTo("<L2>")
        assertThat(rightAxis.series).isEmpty()
    }

    @Test
    @Throws(Exception::class)
    fun should_parse_oneline_GPlot_File() {
        val content = readGPlot("power4.gplot")

        val definition = gPlotParser.parse(content)

        val leftAxis = definition.leftAxis
        assertThat(leftAxis.label).isEqualTo("Power (KW)")
        assertThat(leftAxis.series).containsExactly(
                GPlotSeries(title = "", lineType = LineType.LINES, logDef = "4::0:",
                            axis = Axis.LEFT, color = SeriesColor.RED, logDevice = null))
        val rightAxis = definition.rightAxis
        assertThat(rightAxis.label).isEqualTo("Power (KW)")
        assertThat(rightAxis.series).isEmpty()
    }

    @Test
    @Throws(Exception::class)
    fun should_parse_yrange_GPlot_File() {
        val content = readGPlot("fht80tf.gplot")

        val definition = gPlotParser.parse(content)

        val leftAxis = definition.leftAxis
        assertThat(leftAxis.label).isEmpty()
        assertThat(leftAxis.series).containsExactly(
                GPlotSeries(title = "Open/Closed", lineType = LineType.LINES,
                            color = SeriesColor.RED, logDef = "4:Window:0:\$fld[3]=~\"Open\"?1:0",
                            axis = Axis.LEFT, logDevice = null))
        org.assertj.guava.api.Assertions.assertThat(leftAxis.range.get())
                .isEqualTo(Range.closed(-0.2, 1.2))
        val rightAxis = definition.rightAxis
        assertThat(rightAxis.series).isEmpty()
    }

    @Test
    @Throws(Exception::class)
    fun should_parse_yrange_upper_open_GPlot_File() {
        val content = readGPlot("esa2000.gplot")

        val definition = gPlotParser.parse(content)

        org.assertj.guava.api.Assertions.assertThat(definition.leftAxis.range.get())
                .isEqualTo(Range.atLeast(0.0))
        org.assertj.guava.api.Assertions.assertThat(definition.rightAxis.range.get())
                .isEqualTo(Range.atLeast(0.0))
    }

    @Test
    @Throws(Exception::class)
    fun should_parse_dblog_GPlot_File() {
        val content = readGPlot("SM_DB_Network_eth0.gplot")

        val definition = gPlotParser.parse(content)

        val leftAxis = definition.leftAxis
        assertThat(leftAxis.label).isEqualTo("Traffic RX / TX")
        assertThat(leftAxis.series).containsExactly(
                GPlotSeries(title = "RX", lineType = LineType.LINES,
                            logDef = "<SPEC1>:eth0_diff:::\$val=~s/^RX..([\\d.]*).*/$1/eg",
                            axis = Axis.LEFT, color = SeriesColor.RED, logDevice = null),
                GPlotSeries(title = "TX", lineType = LineType.LINES,
                            logDef = "<SPEC1>:eth0_diff:::\$val=~s/.*TX..([\\d.]*).*/$1/eg",
                            color = SeriesColor.GREEN, axis = Axis.LEFT, logDevice = null))
        val rightAxis = definition.rightAxis
        assertThat(rightAxis.label).isEqualTo("Traffic Total")
        assertThat(rightAxis.series).containsExactly(
                GPlotSeries(title = "Total", lineType = LineType.LINES,
                            logDef = "<SPEC1>:eth0_diff:::\$val=~s/.*Total..([\\d.]*).*/$1/eg",
                            axis = Axis.RIGHT, color = SeriesColor.BLUE, logDevice = null))
    }

    @Test
    @Throws(Exception::class)
    fun should_parse_series_types() {
        val content = readGPlot("temp4rain10.gplot")

        val definition = gPlotParser.parse(content)

        val leftAxis = definition.leftAxis
        assertThat(leftAxis.label).isEqualTo("Rain (l/m2)")
        assertThat(leftAxis.series).containsExactly(
                GPlotSeries(title = "Rain/h", lineType = LineType.HISTEPS,
                            logDef = "10:IR\\x3a:0:delta-h", color = SeriesColor.GREEN,
                            seriesType = SeriesType.FILL, axis = Axis.LEFT, logDevice = null),
                GPlotSeries(title = "Rain/day", lineType = LineType.HISTEPS,
                            logDef = "10:IR\\x3a:0:delta-d", color = SeriesColor.BLUE,
                            seriesType = SeriesType.DEFAULT, axis = Axis.LEFT, logDevice = null))
        val rightAxis = definition.rightAxis
        assertThat(rightAxis.label).isEqualTo("Temperature in C")
        assertThat(rightAxis.series).containsExactly(
                GPlotSeries(title = "Temperature", lineType = LineType.LINES,
                            logDef = "4:IR\\x3a:0:", color = SeriesColor.RED,
                            seriesType = SeriesType.DEFAULT, axis = Axis.RIGHT, logDevice = null))
    }

    @Test
    @Throws(Exception::class)
    fun should_parse_line_width() {
        val content = readGPlot("co20.gplot")

        val definition = gPlotParser.parse(content)

        val axis = definition.rightAxis
        assertThat(axis.series).contains(
                GPlotSeries(title = "Air quality (ppm)", lineType = LineType.LINES,
                            logDef = "4:voc::", color = SeriesColor.WHITE,
                            seriesType = SeriesType.DEFAULT, axis = Axis.RIGHT, lineWidth = (0.2f),
                            logDevice = null))
    }

    @Test
    @Throws(Exception::class)
    fun should_parse_user_specified_power8ttt_gplot() {
        val content = readGPlot("power8ttt.gplot")

        val definition = gPlotParser.parse(content)

        val leftAxis = definition.leftAxis
        assertThat(leftAxis.label).isEqualTo("Power (KWh)")
        assertThat(leftAxis.range.isPresent).isFalse()
        val rightAxis = definition.rightAxis
        assertThat(rightAxis.label).isEqualTo("Power (KWh)")
        assertThat(rightAxis.range.isPresent).isFalse()
        assertThat(rightAxis.series).containsOnly(
                GPlotSeries(title = "Stromzähler", lineType = LineType.LINES,
                            logDef = "4:CUL_EM_22.Summe\\x3a:0:", color = SeriesColor.RED,
                            seriesType = SeriesType.FILL, axis = Axis.RIGHT, lineWidth = (1f),
                            logDevice = "sumLog"))
    }

    @Test
    @Throws(Exception::class)
    fun should_parse_temp4hum4_gplot() {
        val content = readGPlot("temp4hum4.gplot")

        val definition = gPlotParser.parse(content)

        val leftAxis = definition.leftAxis
        assertThat(leftAxis.label).isEqualTo("Humidity (%)")
        assertThat(leftAxis.range.isPresent).isFalse()
        val rightAxis = definition.rightAxis
        assertThat(rightAxis.label).isEqualTo("Temperature in C")
        assertThat(rightAxis.range.isPresent).isFalse()
        assertThat(rightAxis.series).containsOnly(
                GPlotSeries(title = "Measured temperature", lineType = LineType.LINES,
                            logDef = "4:temperature:10:", color = SeriesColor.RED,
                            seriesType = SeriesType.DEFAULT, axis = Axis.RIGHT, lineWidth = (1f),
                            logDevice = null))
        assertThat(leftAxis.series).containsOnly(
                GPlotSeries(title = "Humidity (%)", lineType = LineType.LINES,
                            logDef = "4:humidity:50:", color = SeriesColor.GREEN,
                            seriesType = SeriesType.DEFAULT, axis = Axis.LEFT, lineWidth = (1f),
                            logDevice = null))
    }

    @Test
    @Throws(Exception::class)
    fun should_parse_egrep_files() {
        val content = readGPlot("egrep.gplot")

        val definition = gPlotParser.parse(content)

        val leftAxis = definition.leftAxis
        assertThat(leftAxis.label).isEqualTo("Signal in %")
        assertThat(leftAxis.range.isPresent).isFalse()
        val rightAxis = definition.rightAxis
        assertThat(rightAxis.label).isEqualTo("Temperatur in Grad Celsius")
        assertThat(rightAxis.range.isPresent).isFalse()
        assertThat(rightAxis.series).containsOnly(
                GPlotSeries(title = "Soll-Temperatur (C)", lineType = LineType.LINES,
                            logDef = "4:desiredTemperature:0:", color = SeriesColor.RED,
                            seriesType = SeriesType.DEFAULT, axis = Axis.RIGHT, lineWidth = (2f),
                            logDevice = null),
                GPlotSeries(title = "Ist-Temperatur(ungenau)(C)", lineType = LineType.LINES,
                            logDef = "4:temperature:0:", color = SeriesColor.GREEN,
                            seriesType = SeriesType.DEFAULT, axis = Axis.RIGHT, lineWidth = (2f),
                            logDevice = null))
        assertThat(leftAxis.series).containsOnly(
                GPlotSeries(title = "Ventil (%)", lineType = LineType.LINES,
                            logDef = "4:valveposition:0:", color = SeriesColor.BLUE,
                            seriesType = SeriesType.DEFAULT, axis = Axis.LEFT, lineWidth = (2f),
                            logDevice = null))
    }

    @Test
    @Throws(Exception::class)
    fun should_parse_custom_gplot_file() {
        val content = readGPlot("custom.gplot")

        val definition = gPlotParser.parse(content)

        val leftAxis = definition.leftAxis
        assertThat(leftAxis.label).isEqualTo("Anwesenheit")
        org.assertj.guava.api.Assertions.assertThat(leftAxis.range)
                .contains(Range.closed(0.0, 100.0))
        val rightAxis = definition.rightAxis
        assertThat(rightAxis.label).isEqualTo("Tür/Fenster")
        org.assertj.guava.api.Assertions.assertThat(rightAxis.range)
                .contains(Range.closed(0.0, 1.0))
        assertThat(leftAxis.series).containsOnly(
                GPlotSeries(title = "Handy1", lineType = LineType.STEPS,
                            logDef = "Handy1:state:::\$val=(\$val=~'present'?50:0)",
                            color = SeriesColor.GREEN, seriesType = SeriesType.FILL,
                            axis = Axis.LEFT, lineWidth = (1.5f), logDevice = "logdb"),
                GPlotSeries(title = "Handy2", lineType = LineType.STEPS,
                            logDef = "Handy2:state:::\$val=(\$val=~'present'?50:0)",
                            color = SeriesColor.MAGENTA, seriesType = SeriesType.FILL,
                            axis = Axis.LEFT, lineWidth = (1.5f), logDevice = "logdb"))
        assertThat(rightAxis.series).containsOnly(
                GPlotSeries(title = "Tür", lineType = LineType.STEPS, logDef = "whg_tuer:onoff",
                            color = SeriesColor.BLUE, seriesType = SeriesType.DEFAULT,
                            axis = Axis.RIGHT, lineWidth = (2f), logDevice = "logdb"),
                GPlotSeries(title = "Fenster Küche", lineType = LineType.POINTS,
                            logDef = "k_fenster:opened:::\$val=(\$val=~'opened'?1:0)",
                            color = SeriesColor.RED, seriesType = SeriesType.DEFAULT,
                            axis = Axis.RIGHT, lineWidth = (1f), logDevice = "logdb"),
                GPlotSeries(title = "Fenster Schlafzimmer", lineType = LineType.POINTS,
                            logDef = "sz_fenster:opened:::\$val=(\$val=~'opened'?1:0)",
                            color = SeriesColor.WHITE, seriesType = SeriesType.DEFAULT,
                            axis = Axis.RIGHT, lineWidth = (1f), logDevice = ("logdb")))
    }

    @Throws(IOException::class)
    private fun readGPlot(fileName: String) = GPlotParser::class.java.getResourceAsStream(
            fileName).use {
        it?.readBytes()?.toString(Charsets.UTF_8)
    } ?: ""
}