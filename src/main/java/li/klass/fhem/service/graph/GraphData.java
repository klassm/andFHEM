package li.klass.fhem.service.graph;

import org.joda.time.Interval;

import java.util.HashMap;
import java.util.List;

import li.klass.fhem.service.graph.gplot.GPlotSeries;

public class GraphData {
    private final HashMap<GPlotSeries, List<GraphEntry>> data;
    private final Interval interval;

    public GraphData(HashMap<GPlotSeries, List<GraphEntry>> data, Interval interval) {
        this.data = data;
        this.interval = interval;
    }

    public Interval getInterval() {
        return interval;
    }

    public HashMap<GPlotSeries, List<GraphEntry>> getData() {

        return data;
    }
}
