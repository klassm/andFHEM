package li.klass.fhem.activities.graph;

import org.junit.Test;

import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import li.klass.fhem.R;
import li.klass.fhem.infra.basetest.RobolectricBaseTestCase;
import li.klass.fhem.service.graph.GraphEntry;
import li.klass.fhem.service.graph.description.ChartSeriesDescription;

import static li.klass.fhem.activities.graph.ViewableChartSeries.ChartType.NORMAL;
import static li.klass.fhem.activities.graph.ViewableChartSeries.ChartType.REGRESSION;
import static li.klass.fhem.service.graph.description.SeriesType.TEMPERATURE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class ChartDataTest extends RobolectricBaseTestCase {

    private static final List<GraphEntry> dummyData = Arrays.asList(new GraphEntry(new Date(1), 0.3f));

    @Test
    public void testNumberOfContainedSeries() {
        ChartData data = new ChartData(
                ChartSeriesDescription.getRegressionValuesInstance(R.string.temperature, "abc", TEMPERATURE),
                dummyData
        );
        assertThat(data.getNumberOfContainedSeries(), is(2));

        data = new ChartData(
                ChartSeriesDescription.getSumInstance( R.string.temperature, "abc", 1, TEMPERATURE),
                dummyData
        );
        assertThat(data.getNumberOfContainedSeries(), is(2));

        data = new ChartData(
                new ChartSeriesDescription(R.string.temperature, "abc", TEMPERATURE),
                dummyData
        );
        assertThat(data.getNumberOfContainedSeries(), is(1));
    }

    @Test
    public void testIterator() {
        ChartData data = new ChartData(
                ChartSeriesDescription.getRegressionValuesInstance(R.string.temperature, "abc", TEMPERATURE),
                dummyData
        );

        Iterator<ViewableChartSeries> iterator = data.iterator();
        assertThat(iterator.hasNext(), is(true));
        assertThat(iterator.next().getChartType(), is(NORMAL));

        assertThat(iterator.hasNext(), is(true));
        assertThat(iterator.next().getChartType(), is(REGRESSION));

        assertThat(iterator.hasNext(), is(false));
    }
}
