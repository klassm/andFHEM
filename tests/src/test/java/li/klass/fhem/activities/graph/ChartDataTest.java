package li.klass.fhem.activities.graph;

import li.klass.fhem.infra.basetest.RobolectricBaseTestCase;
import li.klass.fhem.service.graph.GraphEntry;
import li.klass.fhem.service.graph.description.ChartSeriesDescription;
import li.klass.fhem.service.graph.description.SeriesType;
import org.junit.Test;

import java.util.*;

import static li.klass.fhem.activities.graph.ViewableChartSeries.ChartType.NORMAL;
import static li.klass.fhem.activities.graph.ViewableChartSeries.ChartType.REGRESSION;
import static li.klass.fhem.service.graph.description.SeriesType.TEMPERATURE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class ChartDataTest extends RobolectricBaseTestCase {

    private static final List<GraphEntry> dummyData = Arrays.asList(new GraphEntry(new Date(1), 0.3f));

    @Test
    public void testNumberOfContainedSeries() {
        ChartData data = new ChartData(ChartSeriesDescription.getRegressionValuesInstance(1, "abc", 0, TEMPERATURE), dummyData);
        assertThat(data.getNumberOfContainedSeries(), is(2));

        data = new ChartData(ChartSeriesDescription.getSumInstance(1, "abc", 1, 0, TEMPERATURE), dummyData);
        assertThat(data.getNumberOfContainedSeries(), is(2));

        data = new ChartData(new ChartSeriesDescription(1, "abc", 0, TEMPERATURE), dummyData);
        assertThat(data.getNumberOfContainedSeries(), is(1));
    }

    @Test
    public void testIterator() {
        ChartData data = new ChartData(ChartSeriesDescription.getRegressionValuesInstance(1, "abc", 0, TEMPERATURE), dummyData);

        Iterator<ViewableChartSeries> iterator = data.iterator();
        assertThat(iterator.hasNext(), is(true));
        assertThat(iterator.next().getChartType(), is(NORMAL));

        assertThat(iterator.hasNext(), is(true));
        assertThat(iterator.next().getChartType(), is(REGRESSION));

        assertThat(iterator.hasNext(), is(false));
    }
}
