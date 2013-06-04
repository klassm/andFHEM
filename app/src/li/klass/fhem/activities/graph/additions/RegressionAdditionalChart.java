package li.klass.fhem.activities.graph.additions;

import li.klass.fhem.R;
import li.klass.fhem.activities.graph.ChartData;
import li.klass.fhem.activities.graph.ViewableChartSeries;
import li.klass.fhem.service.graph.GraphEntry;

import java.util.List;

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
            xSum += entry.getDate().getTime();
            ySum += entry.getValue();
        }

        float xAvg = xSum / entries.size();
        float yAvg = ySum / entries.size();

        float b1Numerator = 0;
        float b1Denominator = 0;

        for (GraphEntry entry : entries) {
            b1Numerator += (entry.getValue() - yAvg) * (entry.getDate().getTime() - xAvg);
            b1Denominator += Math.pow(entry.getDate().getTime() - xAvg, 2);
        }

        float b1 = b1Numerator / b1Denominator;
        float b0 = yAvg - b1 * xAvg;


        for (GraphEntry entry : entries) {
            float y = b0 + b1 * entry.getDate().getTime();
            data.add(new GraphEntry(entry.getDate(), y));
        }
    }

    @Override
    public ViewableChartSeries.ChartType getChartType() {
        return ViewableChartSeries.ChartType.REGRESSION;
    }
}
