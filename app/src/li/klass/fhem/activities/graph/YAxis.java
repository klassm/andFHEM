package li.klass.fhem.activities.graph;

import li.klass.fhem.service.graph.GraphEntry;
import li.klass.fhem.service.graph.description.ChartSeriesDescription;

import java.util.*;

public class YAxis implements Comparable<YAxis>, Iterable<ViewableChartSeries> {
    private String name;
    private List<ChartData> charts = new ArrayList<ChartData>();

    private double minimumY = Double.MAX_VALUE;
    private double maximumY = Double.MIN_VALUE;

    private Date minimumX = null;
    private Date maximumX = null;

    public YAxis(String name) {
        this.name = name;
    }

    public void addChart(ChartSeriesDescription series, List<GraphEntry> graphData) {
        ChartData chart = new ChartData(series, graphData);

        if (chart.getMaximumY() > maximumY) {
            maximumY = chart.getMaximumY();
        }

        if (chart.getMinimumY() < minimumY) {
            minimumY = chart.getMinimumY();
        }

        if (minimumX == null || chart.getMinimumX().before(minimumX)) {
            minimumX = chart.getMinimumX();
        }

        if (maximumX == null || chart.getMaximumX().after(maximumX)) {
            maximumX = chart.getMaximumX();
        }

        charts.add(chart);
    }

    public String getName() {
        return name;
    }

    public List<ChartData> getCharts() {
        return charts;
    }

    @Override
    public int compareTo(YAxis yAxis) {
        return name.compareTo(yAxis.getName());
    }

    public void afterSeriesSet() {
        Collections.sort(charts);
    }

    public int getTotalNumberOfSeries() {
        int total = 0;
        for (ChartData chart : charts) {
            total += chart.getNumberOfContainedSeries();
        }

        return total;
    }

    @Override
    public Iterator<ViewableChartSeries> iterator() {
        return new Iterator<ViewableChartSeries>() {

            private int currentChart = -1;
            private Iterator<ViewableChartSeries> currentIterator;

            @Override
            public boolean hasNext() {
                if (currentIterator == null) {
                    return currentChart < charts.size();
                }

                if (currentIterator.hasNext()) {
                    return true;
                }

                return currentChart + 1 < charts.size();
            }

            @Override
            public ViewableChartSeries next() {
                if ((currentIterator == null || !currentIterator.hasNext())) {
                    currentChart++;
                    currentIterator = charts.get(currentChart).iterator();
                }

                return currentIterator.next();
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException("removing is not supported!");
            }
        };
    }

    public double getMinimumY() {
        return minimumY;
    }

    public double getMaximumY() {
        return maximumY;
    }

    public Date getMinimumX() {
        return minimumX;
    }

    public Date getMaximumX() {
        return maximumX;
    }
}
