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

package li.klass.fhem.service.graph.gplot;

import com.google.common.base.Charsets;
import com.google.common.collect.Range;
import com.google.common.io.Resources;
import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

import li.klass.fhem.testutil.MockitoRule;

import static li.klass.fhem.service.graph.gplot.GPlotSeries.Axis;
import static li.klass.fhem.service.graph.gplot.GPlotSeries.Builder;
import static li.klass.fhem.service.graph.gplot.GPlotSeries.LineType;
import static li.klass.fhem.service.graph.gplot.GPlotSeries.SeriesColor;
import static li.klass.fhem.service.graph.gplot.GPlotSeries.SeriesType;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(DataProviderRunner.class)
public class GPlotParserTest {
    @Rule
    public MockitoRule mockitoRule = new MockitoRule();

    @InjectMocks
    GPlotParser gPlotParser;

    @Test
    public void should_parse_standard_GPlot_File() throws Exception {
        // given
        String content = readGPlot("fht.gplot");

        // when
        GPlotDefinition definition = gPlotParser.parse(content);

        // then
        GPlotAxis leftAxis = definition.getLeftAxis();
        assertThat(leftAxis.getLabel()).isEqualTo("Actuator (%)");
        assertThat(leftAxis.getSeries()).containsExactly(new Builder()
                        .withTitle("Actuator (%)")
                        .withColor(SeriesColor.GREEN)
                        .withLineType(LineType.LINES)
                        .withFileLogDef("4:actuator.*[0-9]+%:0:int")
                        .withAxis(Axis.LEFT)
                        .build()
        );

        GPlotAxis rightAxis = definition.getRightAxis();
        assertThat(rightAxis.getLabel()).isEqualTo("Temperature in C");
        assertThat(rightAxis.getSeries()).containsExactly(new Builder()
                        .withTitle("Measured temperature")
                        .withLineType(LineType.LINES)
                        .withFileLogDef("4:measured:0:")
                        .withAxis(Axis.RIGHT)
                        .build()
        );
    }

    @Test
    public void should_parse_GPlot_File_without_using() throws Exception {
        // given
        String content = readGPlot("ks550_all.gplot");

        // when
        GPlotDefinition definition = gPlotParser.parse(content);

        // then
        GPlotAxis leftAxis = definition.getLeftAxis();
        assertThat(leftAxis.getLabel()).isEqualTo("<L1>");
        assertThat(leftAxis.getSeries()).containsExactly(
                new Builder()
                        .withTitle("T")
                        .withLineType(LineType.LINES)
                        .withFileLogDef("4:::")
                        .withAxis(Axis.LEFT)
                        .build(),
                new Builder()
                        .withTitle("H")
                        .withLineType(LineType.LINES)
                        .withFileLogDef("6:::")
                        .withAxis(Axis.LEFT)
                        .build(),
                new Builder()
                        .withTitle("W")
                        .withLineType(LineType.LINES)
                        .withFileLogDef("8:::")
                        .withAxis(Axis.LEFT)
                        .build(),
                new Builder()
                        .withTitle("R/h")
                        .withLineType(LineType.LINES)
                        .withFileLogDef("10::0:delta-h")
                        .withAxis(Axis.LEFT)
                        .build(),
                new Builder()
                        .withTitle("R/d")
                        .withLineType(LineType.LINES)
                        .withFileLogDef("10::0:delta-d")
                        .withAxis(Axis.LEFT)
                        .build(),
                new Builder()
                        .withTitle("IR")
                        .withLineType(LineType.LINES)
                        .withFileLogDef("12::0:$fld[11]=~\"32768\"?1:0")
                        .withAxis(Axis.LEFT)
                        .build(),
                new Builder()
                        .withTitle("WD")
                        .withLineType(LineType.LINES)
                        .withFileLogDef("14::0:")
                        .withAxis(Axis.LEFT)
                        .build(),
                new Builder()
                        .withTitle("WDR")
                        .withLineType(LineType.LINES)
                        .withFileLogDef("16::0:")
                        .withAxis(Axis.LEFT)
                        .build(),
                new Builder()
                        .withTitle("S")
                        .withLineType(LineType.LINES)
                        .withFileLogDef("18::0:delta-h")
                        .withAxis(Axis.LEFT)
                        .build(),
                new Builder()
                        .withTitle("B")
                        .withLineType(LineType.LINES)
                        .withFileLogDef("20::0:")
                        .withAxis(Axis.LEFT)
                        .build()
        );

        GPlotAxis rightAxis = definition.getRightAxis();
        assertThat(rightAxis.getLabel()).isEqualTo("<L2>");
        assertThat(rightAxis.getSeries()).isEmpty();
    }

    @Test
    public void should_parse_oneline_GPlot_File() throws Exception {
        // given
        String content = readGPlot("power4.gplot");

        // when
        GPlotDefinition definition = gPlotParser.parse(content);

        // then
        GPlotAxis leftAxis = definition.getLeftAxis();
        assertThat(leftAxis.getLabel()).isEqualTo("Power (KW)");
        assertThat(leftAxis.getSeries()).containsExactly(
                new Builder()
                        .withTitle("")
                        .withLineType(LineType.LINES)
                        .withFileLogDef("4::0:")
                        .withAxis(Axis.LEFT)
                        .build()
        );

        GPlotAxis rightAxis = definition.getRightAxis();
        assertThat(rightAxis.getLabel()).isEqualTo("Power (KW)");
        assertThat(rightAxis.getSeries()).isEmpty();
    }

    @Test
    public void should_parse_yrange_GPlot_File() throws Exception {
        // given
        String content = readGPlot("fht80tf.gplot");

        // when
        GPlotDefinition definition = gPlotParser.parse(content);

        // then
        GPlotAxis leftAxis = definition.getLeftAxis();
        assertThat(leftAxis.getLabel()).isEmpty();
        assertThat(leftAxis.getSeries()).containsExactly(
                new Builder()
                        .withTitle("Open/Closed")
                        .withLineType(LineType.LINES)
                        .withFileLogDef("4:Window:0:$fld[3]=~\"Open\"?1:0")
                        .withAxis(Axis.LEFT)
                        .build()
        );
        assertThat(leftAxis.getRange().get()).isEqualTo(Range.closed(-0.2, 1.2));

        GPlotAxis rightAxis = definition.getRightAxis();
        assertThat(rightAxis.getSeries()).isEmpty();
    }

    @Test
    public void should_parse_yrange_upper_open_GPlot_File() throws Exception {
        // given
        String content = readGPlot("esa2000.gplot");

        // when
        GPlotDefinition definition = gPlotParser.parse(content);

        // then
        assertThat(definition.getLeftAxis().getRange().get()).isEqualTo(Range.atLeast(0.0));
        assertThat(definition.getRightAxis().getRange().get()).isEqualTo(Range.atLeast(0.0));
    }

    @Test
    public void should_parse_dblog_GPlot_File() throws Exception {
        // given
        String content = readGPlot("SM_DB_Network_eth0.gplot");

        // when
        GPlotDefinition definition = gPlotParser.parse(content);

        // then
        GPlotAxis leftAxis = definition.getLeftAxis();
        assertThat(leftAxis.getLabel()).isEqualTo("Traffic RX / TX");
        assertThat(leftAxis.getSeries()).containsExactly(
                new Builder()
                        .withTitle("RX")
                        .withLineType(LineType.LINES)
                        .withDbLogDef("<SPEC1>:eth0_diff:::$val=~s/^RX..([\\d.]*).*/$1/eg")
                        .withAxis(Axis.LEFT)
                        .build(),
                new Builder()
                        .withTitle("TX")
                        .withLineType(LineType.LINES)
                        .withDbLogDef("<SPEC1>:eth0_diff:::$val=~s/.*TX..([\\d.]*).*/$1/eg")
                        .withColor(SeriesColor.GREEN)
                        .withAxis(Axis.LEFT)
                        .build()
        );

        GPlotAxis rightAxis = definition.getRightAxis();
        assertThat(rightAxis.getLabel()).isEqualTo("Traffic Total");
        assertThat(rightAxis.getSeries()).containsExactly(

                new Builder()
                        .withTitle("Total")
                        .withLineType(LineType.LINES)
                        .withDbLogDef("<SPEC1>:eth0_diff:::$val=~s/.*Total..([\\d.]*).*/$1/eg")
                        .withAxis(Axis.RIGHT)
                        .withColor(SeriesColor.BLUE)
                        .build()
        );
    }

    @Test
    public void should_parse_series_types() throws Exception {
        // given
        String content = readGPlot("temp4rain10.gplot");

        // when
        GPlotDefinition definition = gPlotParser.parse(content);

        // then
        GPlotAxis leftAxis = definition.getLeftAxis();
        assertThat(leftAxis.getLabel()).isEqualTo("Rain (l/m2)");
        assertThat(leftAxis.getSeries()).containsExactly(
                new Builder()
                        .withTitle("Rain/h")
                        .withLineType(LineType.HISTEPS)
                        .withFileLogDef("10:IR\\x3a:0:delta-h")
                        .withColor(SeriesColor.GREEN)
                        .withSeriesType(SeriesType.FILL)
                        .withAxis(Axis.LEFT)
                        .build(),
                new Builder()
                        .withTitle("Rain/day")
                        .withLineType(LineType.HISTEPS)
                        .withFileLogDef("10:IR\\x3a:0:delta-d")
                        .withColor(SeriesColor.BLUE)
                        .withSeriesType(SeriesType.DEFAULT)
                        .withAxis(Axis.LEFT)
                        .build()
        );

        GPlotAxis rightAxis = definition.getRightAxis();
        assertThat(rightAxis.getLabel()).isEqualTo("Temperature in C");
        assertThat(rightAxis.getSeries()).containsExactly(
                new Builder()
                        .withTitle("Temperature")
                        .withLineType(LineType.LINES)
                        .withFileLogDef("4:IR\\x3a:0:")
                        .withColor(SeriesColor.RED)
                        .withSeriesType(SeriesType.DEFAULT)
                        .withAxis(Axis.RIGHT)
                        .build()
        );
    }

    @Test
    public void should_parse_line_width() throws Exception {
        // given
        String content = readGPlot("co20.gplot");

        // when
        GPlotDefinition definition = gPlotParser.parse(content);

        // then
        GPlotAxis axis = definition.getRightAxis();
        assertThat(axis.getSeries()).contains(
                new Builder()
                        .withTitle("Air quality (ppm)")
                        .withLineType(LineType.LINES)
                        .withFileLogDef("4:voc::")
                        .withColor(SeriesColor.WHITE)
                        .withSeriesType(SeriesType.DEFAULT)
                        .withAxis(Axis.RIGHT)
                        .withLineWith(0.2f)
                        .build()
        );
    }

    @DataProvider
    public static Object[][] ALL_GPLOT_FILES_PROVIDER() throws Exception {
        File resourceDirectory = new File(GPlotParser.class.getResource(".").toURI());
        File[] files = resourceDirectory.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                return filename != null && filename.endsWith(".gplot");
            }
        });

        Object[][] out = new Object[files.length][];
        for (int i = 0; i < files.length; i++) {
            out[i] = new Object[]{files[i]};
        }

        return out;
    }

    @Test
    @UseDataProvider("ALL_GPLOT_FILES_PROVIDER")
    public void should_find_at_least_one_series_in_GPlot_Files(File file) throws Exception {
        // given
        String content = Resources.toString(file.toURI().toURL(), Charsets.UTF_8);

        // when
        GPlotDefinition definition = gPlotParser.parse(content);

        // then
        boolean containsSeries = !definition.getLeftAxis().getSeries().isEmpty();
        containsSeries = !definition.getRightAxis().getSeries().isEmpty() | containsSeries;
        assertThat(containsSeries).isTrue();
    }

    private String readGPlot(String fileName) throws IOException {
        return Resources.toString(Resources.getResource(GPlotParser.class, fileName), Charsets.UTF_8);
    }
}