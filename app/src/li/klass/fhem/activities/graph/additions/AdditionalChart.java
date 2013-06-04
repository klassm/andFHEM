package li.klass.fhem.activities.graph.additions;

import li.klass.fhem.AndFHEMApplication;
import li.klass.fhem.activities.graph.ChartData;
import li.klass.fhem.activities.graph.ViewableChartSeries;
import li.klass.fhem.service.graph.GraphEntry;

import java.util.ArrayList;
import java.util.List;

/**
 * Class representing any additional charts for a given {@link ChartData}. This can concern regression or sum charts.
 */
public abstract class AdditionalChart {
    protected final ChartData originData;
    protected List<GraphEntry> data = new ArrayList<GraphEntry>();

    public AdditionalChart(ChartData originData) {
        this.originData = originData;
        calculateData();
    }

    public String getName() {
        String nameSuffix = AndFHEMApplication.getContext().getString(getNameSuffixStringId());
        return originData.getSeriesDescription().getColumnName() + " " + nameSuffix;
    }

    public List<GraphEntry> getData() {
        return data;
    }

    protected abstract int getNameSuffixStringId();

    protected abstract void calculateData();

    public abstract ViewableChartSeries.ChartType getChartType();
}
