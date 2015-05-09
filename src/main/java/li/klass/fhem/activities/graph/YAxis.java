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

import com.google.common.collect.BoundType;
import com.google.common.collect.Range;

import org.jetbrains.annotations.NotNull;
import org.joda.time.DateTime;

import java.util.Collections;
import java.util.List;

import li.klass.fhem.service.graph.GraphEntry;
import li.klass.fhem.service.graph.gplot.GPlotAxis;
import li.klass.fhem.service.graph.gplot.GPlotSeries;

import static com.google.common.collect.Lists.newArrayList;

public class YAxis implements Comparable<YAxis> {
    private final Context context;
    private String name;
    private List<ChartData> charts = newArrayList();

    private double minimumY = 0;
    private double maximumY = 0;

    private DateTime minimumX = null;
    private DateTime maximumX = null;

    public YAxis(Context context, GPlotAxis axis) {
        this.context = context;
        this.name = axis.getLabel();

        Range<Double> range = axis.getRange().or(Range.range(-1d, BoundType.OPEN, 1d, BoundType.OPEN));
        if (range.hasLowerBound()) {
            minimumY = range.lowerEndpoint();
        }
        if (range.hasUpperBound()) {
            maximumY = range.upperEndpoint();
        }
    }

    public void addChart(GPlotSeries series, List<GraphEntry> graphData) {
        ChartData chart = new ChartData(series, graphData, context);

        if (chart.getMaximumY() > maximumY) {
            maximumY = chart.getMaximumY();
        }

        if (chart.getMinimumY() < minimumY) {
            minimumY = chart.getMinimumY();
        }

        if (minimumX == null || chart.getMinimumX().isBefore(minimumX)) {
            minimumX = chart.getMinimumX();
        }

        if (maximumX == null || chart.getMaximumX().isAfter(maximumX)) {
            maximumX = chart.getMaximumX();
        }

        charts.add(chart);
    }

    @Override
    public int compareTo(@NotNull YAxis yAxis) {
        return name.compareTo(yAxis.getName());
    }

    public String getName() {
        return name;
    }

    public void afterSeriesSet() {
        Collections.sort(charts);

        for (ChartData chart : charts) {
            chart.handleMinMax(minimumX, maximumX);
        }
    }

    public List<ChartData> getCharts() {
        return charts;
    }

    public double getMinimumY() {
        return minimumY;
    }

    public double getMaximumY() {
        return maximumY;
    }

    public DateTime getMinimumX() {
        return minimumX;
    }

    public DateTime getMaximumX() {
        return maximumX;
    }
}
