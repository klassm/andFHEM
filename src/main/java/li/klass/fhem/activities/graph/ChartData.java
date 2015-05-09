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

import java.util.List;

import li.klass.fhem.service.graph.GraphEntry;
import li.klass.fhem.service.graph.gplot.GPlotSeries;

import static com.google.common.collect.Lists.newArrayList;

public class ChartData implements Comparable<ChartData> {

    private final Context context;
    private GPlotSeries plotSeries;
    private List<GraphEntry> graphData;

    private double minimum = Double.MAX_VALUE;
    private double maximum = Double.MIN_VALUE;

    public ChartData(GPlotSeries plotSeries, List<GraphEntry> graphData, Context context) {
        this.plotSeries = plotSeries;
        this.graphData = handleShowDiscreteValues(graphData);
        this.context = context;

        calculateMinMax();
    }

    private List<GraphEntry> handleShowDiscreteValues(List<GraphEntry> data) {
        if (!isDiscreteChart()) {
            return data;
        }

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
        return plotSeries.getTitle().compareTo(chartData.getPlotSeries().getTitle());
    }

    public GPlotSeries getPlotSeries() {
        return plotSeries;
    }

    public void handleMinMax(DateTime minimumX, DateTime maximumX) {
        if (isDiscreteChart()) {
            GraphEntry first = graphData.get(0);
            GraphEntry last = graphData.get(graphData.size() - 1);

            graphData.add(0, new GraphEntry(minimumX, first.getValue()));
            graphData.add(new GraphEntry(maximumX, last.getValue()));
        }
    }

    private boolean isDiscreteChart() {
        GPlotSeries.Type type = plotSeries.getType();
        return type == GPlotSeries.Type.STEPS || type == GPlotSeries.Type.FSTEPS || type == GPlotSeries.Type.HISTEPS;
    }

    public Context getContext() {
        return context;
    }
}
