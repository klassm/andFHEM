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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import li.klass.fhem.service.graph.GraphEntry;
import li.klass.fhem.service.graph.description.ChartSeriesDescription;

public class YAxis implements Comparable<YAxis>, Iterable<ViewableChartSeries> {
    private final Context context;
    private String name;
    private List<ChartData> charts = new ArrayList<ChartData>();

    private double minimumY = Double.MAX_VALUE;
    private double maximumY = Double.MIN_VALUE;

    private Date minimumX = null;
    private Date maximumX = null;

    public YAxis(String name, Context context) {
        this.context = context;
        this.name = name;
    }

    public void addChart(ChartSeriesDescription series, List<GraphEntry> graphData) {
        ChartData chart = new ChartData(series, graphData, context);

        double seriesMaxValue = series.getYAxisMaxValue();
        double seriesMinValue = series.getYAxisMinValue();

        if (isSet(seriesMinValue) && seriesMinValue < minimumY) {
            minimumY = seriesMinValue;
        }

        if (isSet(seriesMaxValue) && seriesMaxValue > maximumY) {
            maximumY = seriesMaxValue;
        }

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

    private boolean isSet(double value) {
        return Math.abs(value) > 0.1;
    }

    @Override
    public int compareTo(YAxis yAxis) {
        return name.compareTo(yAxis.getName());
    }

    public String getName() {
        return name;
    }

    public void afterSeriesSet() {
        Collections.sort(charts);

        for (ChartData chart : charts) {
            chart.handleMinMax(minimumX, maximumX, minimumY, maximumY);
        }
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
