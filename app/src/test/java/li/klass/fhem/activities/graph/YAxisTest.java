package li.klass.fhem.activities.graph;

import android.app.Activity;

import li.klass.fhem.AndFHEMApplication;
import li.klass.fhem.R;
import li.klass.fhem.infra.AndFHEMRobolectricTestRunner;
import li.klass.fhem.infra.basetest.RobolectricBaseTestCase;
import li.klass.fhem.service.graph.GraphEntry;
import li.klass.fhem.service.graph.description.ChartSeriesDescription;
import li.klass.fhem.service.graph.description.SeriesType;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import static li.klass.fhem.activities.graph.ViewableChartSeries.ChartType.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@RunWith(AndFHEMRobolectricTestRunner.class)
public class YAxisTest extends RobolectricBaseTestCase {
    private static final List<GraphEntry> dummyData = Arrays.asList(new GraphEntry(new Date(1), 0.3f));

    private YAxis yAxis;

    @Before
    public void before() {
        yAxis = new YAxis("someName");
        yAxis.addChart(ChartSeriesDescription.getRegressionValuesInstance(R.string.temperature, "abc", SeriesType.TEMPERATURE), dummyData);
        yAxis.addChart(ChartSeriesDescription.getSumInstance(R.string.humidity, "abc1", 1, SeriesType.HUMIDITY), dummyData);
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
