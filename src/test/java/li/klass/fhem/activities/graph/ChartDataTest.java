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

package li.klass.fhem.activities.graph;

import android.content.Context;

import org.joda.time.DateTime;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;

import java.util.Iterator;
import java.util.List;

import li.klass.fhem.R;
import li.klass.fhem.service.graph.GraphEntry;
import li.klass.fhem.service.graph.description.ChartSeriesDescription;
import li.klass.fhem.testutil.MockitoRule;

import static java.util.Arrays.asList;
import static li.klass.fhem.activities.graph.ViewableChartSeries.ChartType.NORMAL;
import static li.klass.fhem.activities.graph.ViewableChartSeries.ChartType.REGRESSION;
import static li.klass.fhem.service.graph.description.SeriesType.TEMPERATURE;
import static org.assertj.core.api.Assertions.assertThat;

public class ChartDataTest {

    private static final List<GraphEntry> DUMMY_DATA = asList(new GraphEntry(new DateTime(1), 0.3f));

    @Rule
    public MockitoRule mockitoRule = new MockitoRule();

    @Mock
    private Context context;

    @Test
    public void testNumberOfContainedSeries() {
        ChartData data = new ChartData(
                new ChartSeriesDescription.Builder()
                        .withColumnName(R.string.temperature, context)
                        .withFileLogSpec("abc")
                        .withDbLogSpec("def")
                        .withSeriesType(TEMPERATURE)
                        .withShowRegression(true)
                        .build(), DUMMY_DATA,
                context);
        assertThat(data.getNumberOfContainedSeries()).isEqualTo(2);

        data = new ChartData(
                new ChartSeriesDescription.Builder()
                        .withColumnName(R.string.temperature, context)
                        .withFileLogSpec("abc")
                        .withDbLogSpec("def")
                        .withSumDivisionFactor((double) 1)
                        .withShowSum(true)
                        .withSeriesType(TEMPERATURE)
                        .build(),
                DUMMY_DATA,
                context);
        assertThat(data.getNumberOfContainedSeries()).isEqualTo(2);

        data = new ChartData(
                new ChartSeriesDescription.Builder()
                        .withColumnName(R.string.temperature, context)
                        .withFileLogSpec("abc")
                        .withDbLogSpec("def")
                        .withSeriesType(TEMPERATURE)
                        .build(),
                DUMMY_DATA,
                context);
        assertThat(data.getNumberOfContainedSeries()).isEqualTo(1);
    }

    @Test
    public void testIterator() {
        ChartData data = new ChartData(
                new ChartSeriesDescription.Builder()
                        .withColumnName(R.string.temperature, context)
                        .withFileLogSpec("abc")
                        .withDbLogSpec("def")
                        .withSeriesType(TEMPERATURE)
                        .withShowRegression(true)
                        .build(),
                DUMMY_DATA,
                context);

        Iterator<ViewableChartSeries> iterator = data.iterator();
        assertThat(iterator.hasNext()).isTrue();
        assertThat(iterator.next().getChartType()).isEqualTo(NORMAL);

        assertThat(iterator.hasNext()).isTrue();
        assertThat(iterator.next().getChartType()).isEqualTo(REGRESSION);

        assertThat(iterator.hasNext()).isFalse();
    }
}
