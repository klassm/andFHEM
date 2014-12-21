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

package li.klass.fhem.activities.graph.additions;

import li.klass.fhem.R;
import li.klass.fhem.activities.graph.ChartData;
import li.klass.fhem.activities.graph.ViewableChartSeries;
import li.klass.fhem.service.graph.GraphEntry;

public class SumAdditionalChart extends AdditionalChart {

    public SumAdditionalChart(ChartData originData) {
        super(originData);
    }

    @Override
    protected int getNameSuffixStringId() {
        return R.string.sum;
    }

    @Override
    protected void calculateData() {
        double hourDiff = (originData.getMaximumX().getMillis() - originData.getMinimumX().getMillis()) / 1000 / 60 / 60d;
        double divisionFactor = hourDiff * originData.getSeriesDescription().getSumDivisionFactor();

        float ySum = 0;
        for (GraphEntry entry : originData.getGraphData()) {
            ySum += entry.getValue();

            data.add(new GraphEntry(entry.getDate(), (float) (ySum / divisionFactor)));
        }
    }

    @Override
    public ViewableChartSeries.ChartType getChartType() {
        return ViewableChartSeries.ChartType.SUM;
    }
}
