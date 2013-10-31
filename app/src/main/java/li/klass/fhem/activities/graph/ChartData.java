package li.klass.fhem.activities.graph;

import li.klass.fhem.activities.graph.additions.AdditionalChart;
import li.klass.fhem.activities.graph.additions.RegressionAdditionalChart;
import li.klass.fhem.activities.graph.additions.SumAdditionalChart;
import li.klass.fhem.service.graph.GraphEntry;
import li.klass.fhem.service.graph.description.ChartSeriesDescription;
import li.klass.fhem.service.graph.description.SeriesType;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class ChartData implements Comparable<ChartData>, Iterable<ViewableChartSeries> {

    private ChartSeriesDescription seriesDescription;
    private List<GraphEntry> graphData;
    private List<AdditionalChart> additionalCharts = new ArrayList<AdditionalChart>();

    private double minimum = Double.MAX_VALUE;
    private double maximum = Double.MIN_VALUE;

    public ChartData(ChartSeriesDescription seriesDescription, List<GraphEntry> graphData) {
        this.seriesDescription = seriesDescription;
        this.graphData = handleShowDiscreteValues(graphData, seriesDescription);

        calculateMinMax();
        calculateAdditionalCharts();
    }

    public ChartSeriesDescription getSeriesDescription() {
        return seriesDescription;
    }

    public List<GraphEntry> getGraphData() {
        return graphData;
    }

    private List<GraphEntry> handleShowDiscreteValues(List<GraphEntry> data, ChartSeriesDescription chartSeriesDescription) {
        if (!chartSeriesDescription.isShowDiscreteValues()) return data;

        float previousValue = -1;
        List<GraphEntry> newData = new ArrayList<GraphEntry>();

        for (GraphEntry entry : data) {
            Date date = entry.getDate();
            float value = entry.getValue();

            if (previousValue == -1) {
                previousValue = value;
            }

            newData.add(new GraphEntry(new Date(date.getTime() - 1), previousValue));
            newData.add(new GraphEntry(date, value));
            newData.add(new GraphEntry(new Date(date.getTime() + 1), value));

            previousValue = value;
        }

        return newData;
    }

    private void calculateMinMax() {
        for (GraphEntry graphEntry : graphData) {
            float value = graphEntry.getValue();
            if (value < minimum) {
                minimum = value;
            }

            if (value > maximum) {
                maximum = value;
            }
        }
    }

    private void calculateAdditionalCharts() {
        if (seriesDescription.isShowRegression()) {
            additionalCharts.add(new RegressionAdditionalChart(this));
        }

        if (seriesDescription.isShowSum()) {
            additionalCharts.add(new SumAdditionalChart(this));
        }
    }

    public double getMinimumY() {
        return minimum;
    }

    public double getMaximumY() {
        return maximum;
    }

    public Date getMinimumX() {
        if (graphData.size() == 0) return null;
        return graphData.get(0).getDate();
    }

    public Date getMaximumX() {
        if (graphData.size() == 0) return null;
        return graphData.get(graphData.size() - 1).getDate();
    }

    @Override
    public int compareTo(ChartData chartData) {
        return seriesDescription.getColumnName().compareTo(chartData.getSeriesDescription().getColumnName());
    }

    public int getNumberOfContainedSeries() {
        return additionalCharts.size() + 1;
    }

    @Override
    public Iterator<ViewableChartSeries> iterator() {
        return new Iterator<ViewableChartSeries>() {
            private int current = 0;

            @Override
            public boolean hasNext() {
                return current < getNumberOfContainedSeries();
            }

            @Override
            public ViewableChartSeries next() {
                ViewableChartSeries result;
                if (current == 0) {
                    result = new ViewableChartSeries(seriesDescription.getColumnName(), graphData, ViewableChartSeries.ChartType.NORMAL, seriesDescription.getSeriesType());
                } else {
                    AdditionalChart chart = additionalCharts.get(current - 1);

                    SeriesType seriesType = chart.getOriginData().getSeriesDescription().getSeriesType();
                    result = new ViewableChartSeries(chart.getName(), chart.getData(), chart.getChartType(), seriesType);
                }

                current++;

                return result;
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException("removing is not supported");
            }
        };
    }

    public void handleMinMax(Date minimumX, Date maximumX, double minimumY, double maximumY) {
        if (seriesDescription.isShowDiscreteValues()) {
            GraphEntry first = graphData.get(0);
            GraphEntry last = graphData.get(graphData.size() - 1);

            graphData.add(0, new GraphEntry(minimumX, first.getValue()));
            graphData.add(new GraphEntry(maximumX, last.getValue()));
        }
    }
}
