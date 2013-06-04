package li.klass.fhem.activities.graph;

import li.klass.fhem.infra.basetest.RobolectricBaseTestCase;
import li.klass.fhem.service.graph.GraphEntry;
import li.klass.fhem.service.graph.description.ChartSeriesDescription;
import org.junit.Test;

import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class ChartDataTest extends RobolectricBaseTestCase {

    private static final List<GraphEntry> dummyData = Arrays.asList(new GraphEntry(new Date(1), 0.3f));

    @Test
    public void testNumberOfContainedSeries() {
        ChartData data = new ChartData(ChartSeriesDescription.getRegressionValuesInstance(1, "abc", 0), dummyData);
        assertThat(data.getNumberOfContainedSeries(), is(2));

        data = new ChartData(ChartSeriesDescription.getSumInstance(1, "abc", 1, 0), dummyData);
        assertThat(data.getNumberOfContainedSeries(), is(2));

        data = new ChartData(new ChartSeriesDescription(1, "abc", 0), dummyData);
        assertThat(data.getNumberOfContainedSeries(), is(1));
    }

    @Test
    public void testIterator() {
        ChartData data = new ChartData(ChartSeriesDescription.getRegressionValuesInstance(1, "abc", 0), dummyData);

        Iterator<ViewableChartSeries> iterator = data.iterator();
        assertThat(iterator.hasNext(), is(true));
        assertThat(iterator.next().getChartType(), is(ViewableChartSeries.ChartType.NORMAL));

        assertThat(iterator.hasNext(), is(true));
        assertThat(iterator.next().getChartType(), is(ViewableChartSeries.ChartType.REGRESSION));

        assertThat(iterator.hasNext(), is(false));
    }
}
