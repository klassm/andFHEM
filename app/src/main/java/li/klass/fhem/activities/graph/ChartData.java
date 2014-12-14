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

import org.jetbrains.annotations.NotNull;
import org.joda.time.DateTime;

import java.util.Iterator;
import java.util.List;

import li.klass.fhem.activities.graph.additions.AdditionalChart;
import li.klass.fhem.activities.graph.additions.RegressionAdditionalChart;
import li.klass.fhem.activities.graph.additions.SumAdditionalChart;
import li.klass.fhem.service.graph.GraphEntry;
import li.klass.fhem.service.graph.description.ChartSeriesDescription;
import li.klass.fhem.service.graph.description.SeriesType;

import static com.google.common.collect.Lists.newArrayList;

public class ChartData implements Comparable<ChartData>, Iterable<ViewableChartSeries> {

    private final Context context;
    private ChartSeriesDescription seriesDescription;
    private List<GraphEntry> graphData;
    private List<AdditionalChart> additionalCharts = newArrayList();

    private double minimum = Double.MAX_VALUE;
    private double maximum = Double.MIN_VALUE;

    public ChartData(ChartSeriesDescription seriesDescription, List<GraphEntry> graphData, Context context) {
        this.seriesDescription = seriesDescription;
        this.graphData = handleShowDiscreteValues(graphData, seriesDescription);
        this.context = context;

        calculateMinMax();
        calculateAdditionalCharts();
    }

    private List<GraphEntry> handleShowDiscreteValues(List<GraphEntry> data, ChartSeriesDescription chartSeriesDescription) {
        if (!chartSeriesDescription.isShowDiscreteValues()) return data;

        float previousValue = -1;
        List<GraphEntry> newData = newArrayList();

        for (GraphEntry entry : data) {
            DateTime date = entry.getDate();
            float value = entry.getValue();

            if (previousValue == -1) {
                previousValue = value;
            }

            newData.add(new GraphEntry(date.minusMillis(1), previousValue));
            newData.add(new GraphEntry(date, value));
            newData.add(new GraphEntry(date.plusMillis(1), value));

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

    public List<GraphEntry> getGraphData() {
        return graphData;
    }

    public double getMinimumY() {
        return minimum;
    }

    public double getMaximumY() {
        return maximum;
    }

    public DateTime getMinimumX() {
        if (graphData.size() == 0) return null;
        return graphData.get(0).getDate();
    }

    public DateTime getMaximumX() {
        if (graphData.size() == 0) return null;
        return graphData.get(graphData.size() - 1).getDate();
    }

    @Override
    public int compareTo(@NotNull ChartData chartData) {
        return seriesDescription.getColumnName().compareTo(chartData.getSeriesDescription().getColumnName());
    }

    public ChartSeriesDescription getSeriesDescription() {
        return seriesDescription;
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

    public int getNumberOfContainedSeries() {
        return additionalCharts.size() + 1;
    }

    public void handleMinMax(DateTime minimumX, DateTime maximumX) {
        if (seriesDescription.isShowDiscreteValues()) {
            GraphEntry first = graphData.get(0);
            GraphEntry last = graphData.get(graphData.size() - 1);

            graphData.add(0, new GraphEntry(minimumX, first.getValue()));
            graphData.add(new GraphEntry(maximumX, last.getValue()));
        }
    }

    public Context getContext() {
        return context;
    }
}
