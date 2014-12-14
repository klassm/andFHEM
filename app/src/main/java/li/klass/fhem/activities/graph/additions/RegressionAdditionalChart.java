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

import java.util.List;

import li.klass.fhem.R;
import li.klass.fhem.activities.graph.ChartData;
import li.klass.fhem.activities.graph.ViewableChartSeries;
import li.klass.fhem.service.graph.GraphEntry;

public class RegressionAdditionalChart extends AdditionalChart {
    public RegressionAdditionalChart(ChartData originData) {
        super(originData);
    }

    @Override
    protected int getNameSuffixStringId() {
        return R.string.regression;
    }

    @Override
    protected void calculateData() {
        float xSum = 0;
        float ySum = 0;
        List<GraphEntry> entries = originData.getGraphData();
        for (GraphEntry entry : entries) {
            xSum += entry.getDate().getMillis();
            ySum += entry.getValue();
        }

        float xAvg = xSum / entries.size();
        float yAvg = ySum / entries.size();

        float b1Numerator = 0;
        float b1Denominator = 0;

        for (GraphEntry entry : entries) {
            b1Numerator += (entry.getValue() - yAvg) * (entry.getDate().getMillis() - xAvg);
            b1Denominator += Math.pow(entry.getDate().getMillis() - xAvg, 2);
        }

        float b1 = b1Numerator / b1Denominator;
        float b0 = yAvg - b1 * xAvg;


        for (GraphEntry entry : entries) {
            float y = b0 + b1 * entry.getDate().getMillis();
            data.add(new GraphEntry(entry.getDate(), y));
        }
    }

    @Override
    public ViewableChartSeries.ChartType getChartType() {
        return ViewableChartSeries.ChartType.REGRESSION;
    }
}
