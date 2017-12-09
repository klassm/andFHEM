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

package li.klass.fhem.graph.backend;

import android.content.Context;

import com.google.common.base.Optional;
import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;

import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.Arrays;
import java.util.List;

import li.klass.fhem.graph.backend.gplot.GPlotSeries;
import li.klass.fhem.testutil.MockitoRule;
import li.klass.fhem.testutil.ValueProvider;
import li.klass.fhem.update.backend.command.execution.Command;
import li.klass.fhem.update.backend.command.execution.CommandExecutionService;

import static li.klass.fhem.graph.backend.GraphService.DATE_TIME_FORMATTER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@RunWith(DataProviderRunner.class)
public class GraphServiceTest {

    @Rule
    public MockitoRule mockitoRule = new MockitoRule();

    @Mock
    private CommandExecutionService commandExecutionService;

    @InjectMocks
    private GraphService graphService;

    private final ValueProvider valueProvider = new ValueProvider();

    @Test
    public void testFindGraphEntries() {
        String content = "\n" +
                "2013-03-21_16:38:39 5.7\n" +
                "2013-03-21_16:48:49 5.9\n" +
                "2013-03-21_16:53:54 6.2\n" +
                "2013-03-21_17:01:32 5.4\n" +
                "2013-03-21_17:04:04 5.2\n" +
                "#4::\n" +
                "\n" +
                "\n";

        List<GraphEntry> graphEntries = graphService.findGraphEntries(content);

        assertThat(graphEntries).hasSize(5);
    }

    @DataProvider
    public static Object[][] graphEntryProvider() {
        DateTime dateTime = new DateTime(2013, 3, 21, 16, 38, 39);
        return new Object[][] {
                { "2013-03-21_16:38:39 5.7\n", Optional.of(new GraphEntry(dateTime, 5.7f)) },
                { "2013-03-21_16:38:39 5.7", Optional.of(new GraphEntry(dateTime, 5.7f)) },
                { "2013-03-21_16:38:39 -5.7\n", Optional.of(new GraphEntry(dateTime, -5.7f)) },
                { "2013-03-21_16:38 5.7\n", Optional.<GraphEntry>absent() },
                { "2013-03-21_16:38:39\n", Optional.<GraphEntry>absent() },
        };
    }

    @Test
    @UseDataProvider("graphEntryProvider")
    public void should_parse_graph_entry(String entry, Optional<GraphEntry> expected) {
        Optional<GraphEntry> result = graphService.parseEntry(entry);

        assertThat(result).isEqualTo(expected);
    }

    @Test
    public void should_load_graph_entries() {
        // given
        String logDeviceName = valueProvider.lowercaseString(10);
        GPlotSeries series = mock(GPlotSeries.class);
        Context context = mock(Context.class);
        String spec1 = valueProvider.lowercaseString(10);
        String spec2 = valueProvider.lowercaseString(10);
        List<String> plotfunction = Arrays.asList(spec1, spec2);

        DateTime from = valueProvider.dateTime();
        DateTime to = from.plusDays(valueProvider.intValue(10));
        String fromDateFormatted = DATE_TIME_FORMATTER.print(from);
        String toDateFormatted = DATE_TIME_FORMATTER.print(to);

        String command = String.format(GraphService.COMMAND_TEMPLATE, logDeviceName, fromDateFormatted, toDateFormatted, series.getLogDef());
        String response = valueProvider.lowercaseString(20);
        given(commandExecutionService.executeSync(new Command(command.replaceAll("<SPEC1>", spec1).replaceAll("<SPEC2>", spec2), Optional.<String>absent()), context)).willReturn(response);

        // when
        String result = graphService.loadLogData(logDeviceName, Optional.<String>absent(), new Interval(from, to), series, context, plotfunction);

        // then
        assertThat(result).isEqualToIgnoringCase("\n\r" + response);
    }
}
