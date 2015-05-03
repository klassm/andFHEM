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
        assertThat(leftAxis.getSeries()).containsExactly(new GPlotSeries.Builder()
                        .withTitle("Actuator (%)")
                        .withType(GPlotSeries.Type.LINES)
                        .withFileLogDef("4:actuator.*[0-9]+%:0:int")
                        .withAxis(GPlotSeries.Axis.LEFT)
                        .build()
        );

        GPlotAxis rightAxis = definition.getRightAxis();
        assertThat(rightAxis.getLabel()).isEqualTo("Temperature in C");
        assertThat(rightAxis.getSeries()).containsExactly(new GPlotSeries.Builder()
                        .withTitle("Measured temperature")
                        .withType(GPlotSeries.Type.LINES)
                        .withFileLogDef("4:measured:0:")
                        .withAxis(GPlotSeries.Axis.RIGHT)
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
                new GPlotSeries.Builder()
                        .withTitle("T")
                        .withType(GPlotSeries.Type.LINES)
                        .withFileLogDef("4:::")
                        .withAxis(GPlotSeries.Axis.LEFT)
                        .build(),
                new GPlotSeries.Builder()
                        .withTitle("H")
                        .withType(GPlotSeries.Type.LINES)
                        .withFileLogDef("6:::")
                        .withAxis(GPlotSeries.Axis.LEFT)
                        .build(),
                new GPlotSeries.Builder()
                        .withTitle("W")
                        .withType(GPlotSeries.Type.LINES)
                        .withFileLogDef("8:::")
                        .withAxis(GPlotSeries.Axis.LEFT)
                        .build(),
                new GPlotSeries.Builder()
                        .withTitle("R/h")
                        .withType(GPlotSeries.Type.LINES)
                        .withFileLogDef("10::0:delta-h")
                        .withAxis(GPlotSeries.Axis.LEFT)
                        .build(),
                new GPlotSeries.Builder()
                        .withTitle("R/d")
                        .withType(GPlotSeries.Type.LINES)
                        .withFileLogDef("10::0:delta-d")
                        .withAxis(GPlotSeries.Axis.LEFT)
                        .build(),
                new GPlotSeries.Builder()
                        .withTitle("IR")
                        .withType(GPlotSeries.Type.LINES)
                        .withFileLogDef("12::0:$fld[11]=~\"32768\"?1:0")
                        .withAxis(GPlotSeries.Axis.LEFT)
                        .build(),
                new GPlotSeries.Builder()
                        .withTitle("WD")
                        .withType(GPlotSeries.Type.LINES)
                        .withFileLogDef("14::0:")
                        .withAxis(GPlotSeries.Axis.LEFT)
                        .build(),
                new GPlotSeries.Builder()
                        .withTitle("WDR")
                        .withType(GPlotSeries.Type.LINES)
                        .withFileLogDef("16::0:")
                        .withAxis(GPlotSeries.Axis.LEFT)
                        .build(),
                new GPlotSeries.Builder()
                        .withTitle("S")
                        .withType(GPlotSeries.Type.LINES)
                        .withFileLogDef("18::0:delta-h")
                        .withAxis(GPlotSeries.Axis.LEFT)
                        .build(),
                new GPlotSeries.Builder()
                        .withTitle("B")
                        .withType(GPlotSeries.Type.LINES)
                        .withFileLogDef("20::0:")
                        .withAxis(GPlotSeries.Axis.LEFT)
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
                new GPlotSeries.Builder()
                        .withTitle("")
                        .withType(GPlotSeries.Type.LINES)
                        .withFileLogDef("4::0:")
                        .withAxis(GPlotSeries.Axis.LEFT)
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
                new GPlotSeries.Builder()
                        .withTitle("Open/Closed")
                        .withType(GPlotSeries.Type.LINES)
                        .withFileLogDef("4:Window:0:$fld[3]=~\"Open\"?1:0")
                        .withAxis(GPlotSeries.Axis.LEFT)
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
                new GPlotSeries.Builder()
                        .withTitle("RX")
                        .withType(GPlotSeries.Type.LINES)
                        .withDbLogDef("<SPEC1>:eth0_diff:::$val=~s/^RX..([\\d.]*).*/$1/eg")
                        .withAxis(GPlotSeries.Axis.LEFT)
                        .build(),
                new GPlotSeries.Builder()
                        .withTitle("TX")
                        .withType(GPlotSeries.Type.LINES)
                        .withDbLogDef("<SPEC1>:eth0_diff:::$val=~s/.*TX..([\\d.]*).*/$1/eg")
                        .withAxis(GPlotSeries.Axis.LEFT)
                        .build()
        );

        GPlotAxis rightAxis = definition.getRightAxis();
        assertThat(rightAxis.getLabel()).isEqualTo("Traffic Total");
        assertThat(rightAxis.getSeries()).containsExactly(

                new GPlotSeries.Builder()
                        .withTitle("Total")
                        .withType(GPlotSeries.Type.LINES)
                        .withDbLogDef("<SPEC1>:eth0_diff:::$val=~s/.*Total..([\\d.]*).*/$1/eg")
                        .withAxis(GPlotSeries.Axis.RIGHT)
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