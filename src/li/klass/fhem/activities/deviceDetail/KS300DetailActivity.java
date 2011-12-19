package li.klass.fhem.activities.deviceDetail;

import android.os.Bundle;
import android.widget.LinearLayout;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LineGraphView;
import li.klass.fhem.R;
import li.klass.fhem.data.FHEMService;
import li.klass.fhem.data.provider.graph.GraphEntry;
import li.klass.fhem.domain.KS300Device;

import java.util.List;
import java.util.Map;

public class KS300DetailActivity extends DeviceDetailActivity<KS300Device> {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Map<String,List<GraphEntry>> graphData = FHEMService.INSTANCE.getGraphData(device);

        if (graphData != null) {
            List<GraphEntry> entries = graphData.get("temperature");
            GraphView.GraphViewData[] data = new GraphView.GraphViewData[entries.size()];

            int i = 0;
            for (GraphEntry entry : entries) {
                data[i++] = new GraphView.GraphViewData(entry.getDate().getTime(), entry.getValue());
            }
            GraphView.GraphViewSeries graphViewSeries = new GraphView.GraphViewSeries("temperature", 0xffff0000, data);
            LineGraphView graphView = new LineGraphView(this, "temperature");
            graphView.addSeries(graphViewSeries);
            graphView.setShowLegend(true);

            LinearLayout graphLayout = (LinearLayout) findViewById(R.id.layout);
            graphLayout.addView(graphView);
        }
    }
}
