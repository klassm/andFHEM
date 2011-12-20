package li.klass.fhem.graph;

import com.androidplot.series.XYSeries;
import li.klass.fhem.data.provider.graph.GraphEntry;

import java.util.List;

public class HourXYSeries implements XYSeries {

    private List<GraphEntry> entries;

    public HourXYSeries(List<GraphEntry> entries) {
        this.entries = entries;
    }
    
    @Override
    public Number getX(int i) {
        return entries.get(i).getHour();
    }

    @Override
    public Number getY(int i) {
        return entries.get(i).getValue();
    }

    @Override
    public String getTitle() {
        return "SomeTitle";
    }

    @Override
    public int size() {
        return entries.size();
    }
}
