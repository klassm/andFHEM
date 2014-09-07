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
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import li.klass.fhem.R;
import li.klass.fhem.infra.AndFHEMRobolectricTestRunner;
import li.klass.fhem.infra.basetest.RobolectricBaseTestCase;
import li.klass.fhem.service.graph.GraphEntry;
import li.klass.fhem.service.graph.description.ChartSeriesDescription;
import li.klass.fhem.service.graph.description.SeriesType;
import li.klass.fhem.testutil.MockitoTestRule;

import static li.klass.fhem.activities.graph.ViewableChartSeries.ChartType.NORMAL;
import static li.klass.fhem.activities.graph.ViewableChartSeries.ChartType.REGRESSION;
import static li.klass.fhem.activities.graph.ViewableChartSeries.ChartType.SUM;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;

@RunWith(AndFHEMRobolectricTestRunner.class)
public class YAxisTest extends RobolectricBaseTestCase {
    private static final List<GraphEntry> DUMMY_DATA = Arrays.asList(new GraphEntry(new DateTime(1), 0.3f));

    @Rule
    public MockitoTestRule mockitoTestRule = new MockitoTestRule();

    @Mock
    private Context context;

    private YAxis yAxis;

    @Before
    public void before() {
        given(context.getString(R.string.regression)).willReturn("regression");
        given(context.getString(R.string.sum)).willReturn("Sum");

        yAxis = new YAxis("someName", context);

        yAxis.addChart(
                new ChartSeriesDescription.Builder()
                        .withColumnName(R.string.temperature)
                        .withFileLogSpec("abc")
                        .withDbLogSpec("def")
                        .withSeriesType(SeriesType.TEMPERATURE)
                        .withShowRegression(true)
                        .build(),
                DUMMY_DATA
        );

        yAxis.addChart(new ChartSeriesDescription.Builder()
                .withColumnName(R.string.humidity)
                .withFileLogSpec("abc1")
                .withDbLogSpec("def")
                .withSumDivisionFactor((double) 1)
                .withShowSum(true)
                .withSeriesType(SeriesType.HUMIDITY)
                .build(), DUMMY_DATA);
    }

    @Test
    public void testNumberOfContainedSeries() {
        assertThat(yAxis.getTotalNumberOfSeries(), is(4));
    }

    @Test
    public void testIterator() {
        Iterator<ViewableChartSeries> iterator = yAxis.iterator();

        assertThat(iterator.hasNext(), is(true));
        assertIteratorValue(iterator, "Temperature", NORMAL);

        assertThat(iterator.hasNext(), is(true));
        assertIteratorValue(iterator, "Temperature regression", REGRESSION);

        assertThat(iterator.hasNext(), is(true));
        assertIteratorValue(iterator, "Humidity", NORMAL);

        assertThat(iterator.hasNext(), is(true));
        assertIteratorValue(iterator, "Humidity Sum", SUM);

        assertThat(iterator.hasNext(), is(false));
    }

    private void assertIteratorValue(Iterator<ViewableChartSeries> iterator, String name, ViewableChartSeries.ChartType type) {
        ViewableChartSeries next = iterator.next();
        assertThat(next.getChartType(), is(type));
        assertThat(next.getName(), is(name));
    }
}
