package li.klass.fhem.activities.deviceDetail;

import android.os.Bundle;
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
//            mySimpleXYPlot.addSeries(new HourXYSeries(entries), LineAndPointRenderer.class,
//                    new LineAndPointFormatter(Color.rgb(0, 200, 0), Color.rgb(200, 0, 0), Color.rgb(200, 0, 0)));
//            mySimpleXYPlot.getGraphWidget().setTicksPerRangeLabel(12);
//            mySimpleXYPlot.disableAllMarkup();



        }
    }
}
