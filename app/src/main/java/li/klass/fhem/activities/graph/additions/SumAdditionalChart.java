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
        double hourDiff = (originData.getMaximumX().getTime() - originData.getMinimumX().getTime()) / 1000 / 60 / 60d;
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
