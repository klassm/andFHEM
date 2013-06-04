package li.klass.fhem.activities.graph;

import li.klass.fhem.service.graph.GraphEntry;

import java.util.List;

public class ViewableChartSeries {
    public enum ChartType {
        NORMAL, REGRESSION, SUM, DISCRETE
    }

    private String name;
    private List<GraphEntry> data;
    private ChartType chartType;

    public ViewableChartSeries(String name, List<GraphEntry> data, ChartType chartType) {
        this.name = name;
        this.data = data;
        this.chartType = chartType;
    }

    public String getName() {
        return name;
    }

    public List<GraphEntry> getData() {
        return data;
    }

    public ChartType getChartType() {
        return chartType;
    }
}
